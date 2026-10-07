package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.KeyNumberBorder
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun CalculatorDisplay(
    expression: String,
    previewResult: String,
    finalResult: String,
    error: String?,
    isDegree: Boolean,
    memoryValue: Double?,
    onToggleAngleMode: () -> Unit,
    onMemoryClick: () -> Unit,
    onBackspace: () -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll expression to end when expression changes
    LaunchedEffect(expression) {
        if (expression.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val displayResult = when {
        finalResult.isNotEmpty() -> finalResult
        previewResult.isNotEmpty() -> previewResult
        else -> ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .border(1.dp, KeyNumberBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("calculator_display")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            // Top Status Bar: Angle mode pill, Memory pill, Copy button, Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle Mode Badge (DEG / RAD)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, KeyNumberBorder, RoundedCornerShape(8.dp))
                            .clickable(onClick = onToggleAngleMode)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("angle_mode_badge")
                    ) {
                        Text(
                            text = if (isDegree) "DEG" else "RAD",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Memory Indicator
                    if (memoryValue != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(IndigoAccent.copy(alpha = 0.2f))
                                .border(1.dp, IndigoAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable(onClick = onMemoryClick)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("memory_badge")
                        ) {
                            Text(
                                text = "M",
                                color = IndigoAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action buttons: Copy & Backspace
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (displayResult.isNotEmpty() || expression.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val toCopy = if (finalResult.isNotEmpty()) finalResult else if (previewResult.isNotEmpty()) previewResult else expression
                                onCopy(toCopy)
                            },
                            modifier = Modifier.size(36.dp).testTag("copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (expression.isNotEmpty()) {
                        IconButton(
                            onClick = onBackspace,
                            modifier = Modifier.size(36.dp).testTag("backspace_display_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Expression View with Syntax Highlighting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (expression.isEmpty()) AnnotatedString("0") else formatSyntaxHighlighted(expression),
                    color = if (expression.isEmpty()) TextTertiary else TextPrimary,
                    fontSize = if (expression.length > 18) 22.sp else 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("expression_text")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live / Final Result Area or Error message
            if (error != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = RoseAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = error,
                        color = RoseAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                }
            } else {
                Text(
                    text = when {
                        finalResult.isNotEmpty() -> "= $finalResult"
                        previewResult.isNotEmpty() -> "= $previewResult"
                        else -> ""
                    },
                    color = if (finalResult.isNotEmpty()) CyanAccent else TextSecondary,
                    fontSize = if (finalResult.isNotEmpty()) 34.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("result_text")
                )
            }
        }
    }
}

/**
 * Builds syntax highlighted string for clean expression reading.
 */
private fun formatSyntaxHighlighted(raw: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = raw.length

        while (i < len) {
            val c = raw[i]
            when {
                c.isDigit() || c == '.' -> {
                    val start = length
                    while (i < len && (raw[i].isDigit() || raw[i] == '.')) {
                        append(raw[i])
                        i++
                    }
                    addStyle(SpanStyle(color = TextPrimary), start, length)
                    continue
                }
                c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '^' || c == '%' -> {
                    val start = length
                    append(c)
                    addStyle(SpanStyle(color = AmberAccent, fontWeight = FontWeight.Bold), start, length)
                    i++
                }
                c == '(' || c == ')' -> {
                    val start = length
                    append(c)
                    addStyle(SpanStyle(color = IndigoAccent, fontWeight = FontWeight.Bold), start, length)
                    i++
                }
                c.isLetter() || c == '⁻' || c == '¹' || c == 'π' || c == 'φ' || c == '√' || c == '∛' -> {
                    val start = length
                    while (i < len && (raw[i].isLetter() || raw[i] in listOf('⁻', '¹', '²', '³', 'π', 'φ', '√', '∛'))) {
                        append(raw[i])
                        i++
                    }
                    addStyle(SpanStyle(color = CyanAccent, fontWeight = FontWeight.SemiBold), start, length)
                    continue
                }
                else -> {
                    append(c)
                    i++
                }
            }
        }
    }
}
