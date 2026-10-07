package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

object ScientificEvaluator {

    const val PHI = 1.6180339887498948482

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    /**
     * Evaluates an expression string with given angle mode (degrees vs radians).
     */
    fun evaluate(expression: String, isDegree: Boolean = true): EvalResult {
        if (expression.isBlank()) {
            return EvalResult.Error("Empty expression")
        }

        try {
            val sanitized = sanitize(expression)
            val tokens = tokenize(sanitized)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, isDegree)

            if (result.isNaN()) {
                return EvalResult.Error("Undefined result")
            }
            if (result.isInfinite()) {
                return EvalResult.Error("Cannot divide by zero")
            }

            val formatted = formatNumber(result)
            return EvalResult.Success(result, formatted)
        } catch (e: ArithmeticException) {
            return EvalResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            return EvalResult.Error(e.message ?: "Invalid expression")
        } catch (e: Exception) {
            return EvalResult.Error("Syntax error")
        }
    }

    /**
     * Sanitizes expression: replaces symbols (×, ÷, −, π, √, etc.) with internal tokens.
     */
    private fun sanitize(expr: String): String {
        var s = expr.trim()
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("π", " PI ")
            .replace("φ", " PHI ")
            .replace("√", " sqrt ")
            .replace("∛", " cbrt ")
            .replace("sin⁻¹", " asin ")
            .replace("cos⁻¹", " acos ")
            .replace("tan⁻¹", " atan ")

        // Handle implicit multiplication: e.g. 5( -> 5*(, )2 -> )*2, )sin -> )*sin, 5PI -> 5*PI, etc.
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            sb.append(c)

            if (i + 1 < s.length) {
                val next = s[i + 1]
                val isCurrentDigitOrParen = c.isDigit() || c == ')' || c == '%'
                val isNextLetterOrParen = next == '(' || (next.isLetter() && next != 'E' && next != 'e')
                if (isCurrentDigitOrParen && isNextLetterOrParen) {
                    sb.append('*')
                } else if (c == ')' && next.isDigit()) {
                    sb.append('*')
                }
            }
            i++
        }
        return sb.toString()
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]
            if (c.isWhitespace()) {
                i++
                continue
            }

            // Numbers (including decimals and scientific notation like 1.2e5)
            if (c.isDigit() || c == '.') {
                val num = StringBuilder()
                var hasDot = false
                while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                    if (expr[i] == '.') {
                        if (hasDot) break
                        hasDot = true
                    }
                    num.append(expr[i])
                    i++
                }
                tokens.add(num.toString())
                continue
            }

            // Word tokens (functions & named constants)
            if (c.isLetter()) {
                val word = StringBuilder()
                while (i < len && expr[i].isLetter()) {
                    word.append(expr[i])
                    i++
                }
                tokens.add(word.toString().lowercase())
                continue
            }

            // Single character symbols & operators
            when (c) {
                '+', '-', '*', '/', '^', '%', '!', '(', ')' -> {
                    // Check for unary minus: at start of expression or after operator/open paren
                    if (c == '-') {
                        val prev = tokens.lastOrNull()
                        val isUnary = prev == null || prev in listOf("+", "-", "*", "/", "^", "%", "(")
                        if (isUnary) {
                            tokens.add("neg")
                            i++
                            continue
                        }
                    }
                    tokens.add(c.toString())
                    i++
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private val PRECEDENCE = mapOf(
        "+" to 1, "-" to 1,
        "*" to 2, "/" to 2, "%" to 2,
        "neg" to 3,
        "^" to 4,
        "!" to 5
    )

    private val FUNCTIONS = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "sinh", "cosh", "tanh",
        "ln", "log", "log10", "log2",
        "sqrt", "cbrt", "abs"
    )

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "pi" -> output.add(PI.toString())
                token == "e" -> output.add(E.toString())
                token == "phi" -> output.add(PHI.toString())
                token in FUNCTIONS -> stack.addFirst(token)
                token == "(" -> stack.addFirst(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.first() != "(") {
                        output.add(stack.removeFirst())
                    }
                    if (stack.isNotEmpty() && stack.first() == "(") {
                        stack.removeFirst()
                    }
                    if (stack.isNotEmpty() && stack.first() in FUNCTIONS) {
                        output.add(stack.removeFirst())
                    }
                }
                token in PRECEDENCE.keys -> {
                    val p1 = PRECEDENCE[token] ?: 0
                    while (stack.isNotEmpty()) {
                        val top = stack.first()
                        if (top in PRECEDENCE.keys) {
                            val p2 = PRECEDENCE[top] ?: 0
                            // Right associative operators like '^' and 'neg'
                            val isRightAssoc = token == "^" || token == "neg"
                            if ((!isRightAssoc && p1 <= p2) || (isRightAssoc && p1 < p2)) {
                                output.add(stack.removeFirst())
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    stack.addFirst(token)
                }
                else -> throw IllegalArgumentException("Unknown symbol: $token")
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeFirst()
            if (top == "(" || top == ")") {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>, isDegree: Boolean): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addFirst(num)
                continue
            }

            when (token) {
                "neg" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing operand for -")
                    val a = stack.removeFirst()
                    stack.addFirst(-a)
                }
                "!" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing operand for !")
                    val a = stack.removeFirst()
                    if (a < 0 || a != Math.floor(a)) throw ArithmeticException("Factorial requires positive integer")
                    if (a > 170) throw ArithmeticException("Factorial overflow")
                    stack.addFirst(factorial(a.toInt()))
                }
                "+" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operand")
                    val b = stack.removeFirst()
                    val a = stack.removeFirst()
                    stack.addFirst(a + b)
                }
                "-" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operand")
                    val b = stack.removeFirst()
                    val a = stack.removeFirst()
                    stack.addFirst(a - b)
                }
                "*" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operand")
                    val b = stack.removeFirst()
                    val a = stack.removeFirst()
                    stack.addFirst(a * b)
                }
                "/" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operand")
                    val b = stack.removeFirst()
                    val a = stack.removeFirst()
                    if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
                    stack.addFirst(a / b)
                }
                "%" -> {
                    if (stack.size < 2) {
                        if (stack.size == 1) {
                            // Unary percent
                            val a = stack.removeFirst()
                            stack.addFirst(a / 100.0)
                        } else {
                            throw IllegalArgumentException("Missing operand")
                        }
                    } else {
                        val b = stack.removeFirst()
                        val a = stack.removeFirst()
                        stack.addFirst(a % b)
                    }
                }
                "^" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operand")
                    val b = stack.removeFirst()
                    val a = stack.removeFirst()
                    stack.addFirst(a.pow(b))
                }
                "sin" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    val rad = if (isDegree) Math.toRadians(a) else a
                    val res = sin(rad)
                    stack.addFirst(roundTrigArtifacts(res))
                }
                "cos" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    val rad = if (isDegree) Math.toRadians(a) else a
                    val res = cos(rad)
                    stack.addFirst(roundTrigArtifacts(res))
                }
                "tan" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    if (isDegree && (abs(a % 180) == 90.0)) {
                        throw ArithmeticException("Tangent undefined at 90°")
                    }
                    val rad = if (isDegree) Math.toRadians(a) else a
                    stack.addFirst(roundTrigArtifacts(tan(rad)))
                }
                "asin" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    if (a < -1.0 || a > 1.0) throw ArithmeticException("asin domain is [-1, 1]")
                    val rad = asin(a)
                    stack.addFirst(if (isDegree) Math.toDegrees(rad) else rad)
                }
                "acos" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    if (a < -1.0 || a > 1.0) throw ArithmeticException("acos domain is [-1, 1]")
                    val rad = acos(a)
                    stack.addFirst(if (isDegree) Math.toDegrees(rad) else rad)
                }
                "atan" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    val rad = atan(a)
                    stack.addFirst(if (isDegree) Math.toDegrees(rad) else rad)
                }
                "sinh" -> stack.addFirst(sinh(stack.removeFirstOrNull() ?: 0.0))
                "cosh" -> stack.addFirst(cosh(stack.removeFirstOrNull() ?: 0.0))
                "tanh" -> stack.addFirst(tanh(stack.removeFirstOrNull() ?: 0.0))
                "ln" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    if (a <= 0) throw ArithmeticException("ln domain is x > 0")
                    stack.addFirst(ln(a))
                }
                "log", "log10" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    if (a <= 0) throw ArithmeticException("log domain is x > 0")
                    stack.addFirst(log10(a))
                }
                "log2" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    if (a <= 0) throw ArithmeticException("log domain is x > 0")
                    stack.addFirst(log2(a))
                }
                "sqrt" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                    if (a < 0) throw ArithmeticException("Negative square root")
                    stack.addFirst(sqrt(a))
                }
                "cbrt" -> stack.addFirst(cbrt(stack.removeFirstOrNull() ?: 0.0))
                "abs" -> stack.addFirst(abs(stack.removeFirstOrNull() ?: 0.0))
                else -> throw IllegalArgumentException("Unknown operator: $token")
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Malformed expression")
        }
        return stack.first()
    }

    private fun factorial(n: Int): Double {
        if (n <= 1) return 1.0
        var res = 1.0
        for (i in 2..n) {
            res *= i
        }
        return res
    }

    private fun roundTrigArtifacts(v: Double): Double {
        // e.g. sin(pi) is 1.22e-16 in IEEE-754 -> round to 0
        return if (abs(v) < 1e-15) 0.0 else if (abs(v - 1.0) < 1e-15) 1.0 else if (abs(v + 1.0) < 1e-15) -1.0 else v
    }

    /**
     * Formats a double neatly for calculator display without ugly floating precision issues.
     */
    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Check if value is very large or very small -> use scientific notation
        val absVal = abs(value)
        if (absVal != 0.0 && (absVal >= 1e12 || absVal < 1e-7)) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val sciFormat = DecimalFormat("0.######E0", symbols)
            return sciFormat.format(value).replace("E", "e")
        }

        // Clean precision to avoid IEEE 754 float glitches (e.g. 0.1 + 0.2 = 0.30000000000000004)
        return try {
            val bd = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
            bd.toPlainString()
        } catch (e: Exception) {
            value.toString()
        }
    }
}
