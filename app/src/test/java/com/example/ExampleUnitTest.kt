package com.example

import com.example.domain.ScientificEvaluator
import com.example.domain.UnitCategory
import com.example.domain.UnitRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBasicArithmetic() {
        val res = ScientificEvaluator.evaluate("2+3*4")
        assertTrue(res is ScientificEvaluator.EvalResult.Success)
        assertEquals("14", (res as ScientificEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testParenthesesAndPrecedence() {
        val res = ScientificEvaluator.evaluate("(2+3)*4")
        assertTrue(res is ScientificEvaluator.EvalResult.Success)
        assertEquals("20", (res as ScientificEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testScientificTrigonometryDegrees() {
        val sin30 = ScientificEvaluator.evaluate("sin(30)", isDegree = true)
        assertTrue(sin30 is ScientificEvaluator.EvalResult.Success)
        assertEquals("0.5", (sin30 as ScientificEvaluator.EvalResult.Success).formatted)

        val cos60 = ScientificEvaluator.evaluate("cos(60)", isDegree = true)
        assertTrue(cos60 is ScientificEvaluator.EvalResult.Success)
        assertEquals("0.5", (cos60 as ScientificEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testPowersAndSquareRoot() {
        val sqrt16 = ScientificEvaluator.evaluate("√(16)")
        assertTrue(sqrt16 is ScientificEvaluator.EvalResult.Success)
        assertEquals("4", (sqrt16 as ScientificEvaluator.EvalResult.Success).formatted)

        val pow = ScientificEvaluator.evaluate("2^5")
        assertTrue(pow is ScientificEvaluator.EvalResult.Success)
        assertEquals("32", (pow as ScientificEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testFactorial() {
        val fact5 = ScientificEvaluator.evaluate("5!")
        assertTrue(fact5 is ScientificEvaluator.EvalResult.Success)
        assertEquals("120", (fact5 as ScientificEvaluator.EvalResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val res = ScientificEvaluator.evaluate("10/0")
        assertTrue(res is ScientificEvaluator.EvalResult.Error)
    }

    @Test
    fun testUnitConversionLength() {
        val units = UnitRepository.unitsByCategory[UnitCategory.LENGTH]!!
        val m = units.first { it.id == "m" }
        val km = units.first { it.id == "km" }

        val converted = UnitRepository.convert(1000.0, m, km)
        assertEquals(1.0, converted, 0.0001)
    }

    @Test
    fun testUnitConversionTemperature() {
        val units = UnitRepository.unitsByCategory[UnitCategory.TEMPERATURE]!!
        val c = units.first { it.id == "c" }
        val f = units.first { it.id == "f" }

        val converted = UnitRepository.convert(100.0, c, f)
        assertEquals(212.0, converted, 0.0001)

        val cBack = UnitRepository.convert(32.0, f, c)
        assertEquals(0.0, cBack, 0.0001)
    }
}
