package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorUiState
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.KeyVariant
import com.example.ui.components.KeypadButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.KeyNumberBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    isScientificMode: Boolean,
    modifier: Modifier = Modifier
) {
    var showMemoryRow by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("calculator_screen_container"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Section
        CalculatorDisplay(
            expression = state.expression,
            previewResult = state.previewResult,
            finalResult = state.finalResult,
            error = state.error,
            isDegree = state.isDegree,
            memoryValue = state.memoryValue,
            onToggleAngleMode = { viewModel.onToggleDegRad() },
            onMemoryClick = { showMemoryRow = !showMemoryRow },
            onBackspace = { viewModel.onBackspace() },
            onCopy = { viewModel.copyToClipboard(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Keypad Section
        if (isScientificMode) {
            ScientificKeypad(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.weight(1f, fill = false)
            )
        } else {
            StandardKeypad(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
private fun StandardKeypad(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Memory utility row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "MC",
                onClick = { viewModel.memoryClear() },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "MR",
                onClick = { viewModel.memoryRecall() },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "M+",
                onClick = { viewModel.memoryAdd() },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "M-",
                onClick = { viewModel.memorySubtract() },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 1: AC, ( ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "AC",
                onClick = { viewModel.onClearAll() },
                variant = KeyVariant.ACTION,
                modifier = Modifier.weight(1f),
                testTag = "btn_ac"
            )
            KeypadButton(
                text = "(",
                onClick = { viewModel.onAppendInput("(") },
                variant = KeyVariant.SCIENTIFIC,
                modifier = Modifier.weight(0.5f),
                testTag = "btn_paren_open"
            )
            KeypadButton(
                text = ")",
                onClick = { viewModel.onAppendInput(")") },
                variant = KeyVariant.SCIENTIFIC,
                modifier = Modifier.weight(0.5f),
                testTag = "btn_paren_close"
            )
            KeypadButton(
                text = "%",
                onClick = { viewModel.onAppendInput("%") },
                variant = KeyVariant.OPERATOR,
                modifier = Modifier.weight(1f),
                testTag = "btn_percent"
            )
            KeypadButton(
                text = "÷",
                onClick = { viewModel.onAppendInput("÷") },
                variant = KeyVariant.OPERATOR,
                modifier = Modifier.weight(1f),
                testTag = "btn_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "7",
                onClick = { viewModel.onAppendInput("7") },
                modifier = Modifier.weight(1f),
                testTag = "btn_7"
            )
            KeypadButton(
                text = "8",
                onClick = { viewModel.onAppendInput("8") },
                modifier = Modifier.weight(1f),
                testTag = "btn_8"
            )
            KeypadButton(
                text = "9",
                onClick = { viewModel.onAppendInput("9") },
                modifier = Modifier.weight(1f),
                testTag = "btn_9"
            )
            KeypadButton(
                text = "×",
                onClick = { viewModel.onAppendInput("×") },
                variant = KeyVariant.OPERATOR,
                modifier = Modifier.weight(1f),
                testTag = "btn_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "4",
                onClick = { viewModel.onAppendInput("4") },
                modifier = Modifier.weight(1f),
                testTag = "btn_4"
            )
            KeypadButton(
                text = "5",
                onClick = { viewModel.onAppendInput("5") },
                modifier = Modifier.weight(1f),
                testTag = "btn_5"
            )
            KeypadButton(
                text = "6",
                onClick = { viewModel.onAppendInput("6") },
                modifier = Modifier.weight(1f),
                testTag = "btn_6"
            )
            KeypadButton(
                text = "−",
                onClick = { viewModel.onAppendInput("−") },
                variant = KeyVariant.OPERATOR,
                modifier = Modifier.weight(1f),
                testTag = "btn_minus"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "1",
                onClick = { viewModel.onAppendInput("1") },
                modifier = Modifier.weight(1f),
                testTag = "btn_1"
            )
            KeypadButton(
                text = "2",
                onClick = { viewModel.onAppendInput("2") },
                modifier = Modifier.weight(1f),
                testTag = "btn_2"
            )
            KeypadButton(
                text = "3",
                onClick = { viewModel.onAppendInput("3") },
                modifier = Modifier.weight(1f),
                testTag = "btn_3"
            )
            KeypadButton(
                text = "+",
                onClick = { viewModel.onAppendInput("+") },
                variant = KeyVariant.OPERATOR,
                modifier = Modifier.weight(1f),
                testTag = "btn_plus"
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "±",
                onClick = {
                    val cur = state.expression
                    if (cur.isNotEmpty()) {
                        if (cur.startsWith("-")) {
                            viewModel.onClearAll()
                            viewModel.onAppendInput(cur.substring(1))
                        } else {
                            viewModel.onClearAll()
                            viewModel.onAppendInput("-$cur")
                        }
                    }
                },
                variant = KeyVariant.NUMBER,
                modifier = Modifier.weight(1f),
                testTag = "btn_sign"
            )
            KeypadButton(
                text = "0",
                onClick = { viewModel.onAppendInput("0") },
                modifier = Modifier.weight(1f),
                testTag = "btn_0"
            )
            KeypadButton(
                text = ".",
                onClick = { viewModel.onAppendInput(".") },
                modifier = Modifier.weight(1f),
                testTag = "btn_dot"
            )
            KeypadButton(
                text = "=",
                onClick = { viewModel.onCalculateEqual("STANDARD") },
                variant = KeyVariant.EQUALS,
                modifier = Modifier.weight(1f),
                testTag = "btn_equals"
            )
        }
    }
}

@Composable
private fun ScientificKeypad(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Scientific Utility Control Row: 2nd, DEG/RAD, MC, MR, M+
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(
                text = "2nd",
                onClick = { viewModel.onToggleInverse() },
                variant = KeyVariant.ACCENT_TOGGLE,
                isActiveToggle = state.isInverse,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_2nd"
            )
            KeypadButton(
                text = if (state.isDegree) "DEG" else "RAD",
                onClick = { viewModel.onToggleDegRad() },
                variant = KeyVariant.ACCENT_TOGGLE,
                isActiveToggle = !state.isDegree,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                testTag = "btn_deg_rad"
            )
            KeypadButton(
                text = "MC",
                onClick = { viewModel.memoryClear() },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "MR",
                onClick = { viewModel.memoryRecall() },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "M+",
                onClick = { viewModel.memoryAdd() },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 1: Trig & Logs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sinLabel = if (state.isInverse) "sin⁻¹" else "sin"
            val cosLabel = if (state.isInverse) "cos⁻¹" else "cos"
            val tanLabel = if (state.isInverse) "tan⁻¹" else "tan"
            val lnLabel = if (state.isInverse) "eˣ" else "ln"
            val logLabel = if (state.isInverse) "10ˣ" else "log"

            KeypadButton(
                text = sinLabel,
                onClick = { viewModel.onAppendInput("$sinLabel(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = cosLabel,
                onClick = { viewModel.onAppendInput("$cosLabel(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = tanLabel,
                onClick = { viewModel.onAppendInput("$tanLabel(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = lnLabel,
                onClick = {
                    if (state.isInverse) viewModel.onAppendInput("e^(") else viewModel.onAppendInput("ln(")
                },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = logLabel,
                onClick = {
                    if (state.isInverse) viewModel.onAppendInput("10^(") else viewModel.onAppendInput("log(")
                },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Powers, Roots, Constants & Factorial
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sqrtLabel = if (state.isInverse) "x²" else "√"
            KeypadButton(
                text = sqrtLabel,
                onClick = {
                    if (state.isInverse) viewModel.onAppendInput("^2") else viewModel.onAppendInput("√(")
                },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "xʸ",
                onClick = { viewModel.onAppendInput("^") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "π",
                onClick = { viewModel.onAppendInput("π") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "e",
                onClick = { viewModel.onAppendInput("e") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "n!",
                onClick = { viewModel.onAppendInput("!") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Parentheses, %, AC, ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(
                text = "(",
                onClick = { viewModel.onAppendInput("(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = ")",
                onClick = { viewModel.onAppendInput(")") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "%",
                onClick = { viewModel.onAppendInput("%") },
                variant = KeyVariant.OPERATOR,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "AC",
                onClick = { viewModel.onClearAll() },
                variant = KeyVariant.ACTION,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "⌫",
                onClick = { viewModel.onBackspace() },
                variant = KeyVariant.ACTION,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 7, 8, 9, ÷, 1/x
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(
                text = "7",
                onClick = { viewModel.onAppendInput("7") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "8",
                onClick = { viewModel.onAppendInput("8") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "9",
                onClick = { viewModel.onAppendInput("9") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "÷",
                onClick = { viewModel.onAppendInput("÷") },
                variant = KeyVariant.OPERATOR,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "1/x",
                onClick = { viewModel.onAppendInput("1/(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: 4, 5, 6, ×, sinh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(
                text = "4",
                onClick = { viewModel.onAppendInput("4") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "5",
                onClick = { viewModel.onAppendInput("5") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "6",
                onClick = { viewModel.onAppendInput("6") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "×",
                onClick = { viewModel.onAppendInput("×") },
                variant = KeyVariant.OPERATOR,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "sinh",
                onClick = { viewModel.onAppendInput("sinh(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 6: 1, 2, 3, −, cosh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(
                text = "1",
                onClick = { viewModel.onAppendInput("1") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "2",
                onClick = { viewModel.onAppendInput("2") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "3",
                onClick = { viewModel.onAppendInput("3") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "−",
                onClick = { viewModel.onAppendInput("−") },
                variant = KeyVariant.OPERATOR,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "cosh",
                onClick = { viewModel.onAppendInput("cosh(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 7: 0, ., +, =, tanh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(
                text = "0",
                onClick = { viewModel.onAppendInput("0") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = ".",
                onClick = { viewModel.onAppendInput(".") },
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "+",
                onClick = { viewModel.onAppendInput("+") },
                variant = KeyVariant.OPERATOR,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "=",
                onClick = { viewModel.onCalculateEqual("SCIENTIFIC") },
                variant = KeyVariant.EQUALS,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "tanh",
                onClick = { viewModel.onAppendInput("tanh(") },
                variant = KeyVariant.SCIENTIFIC,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
