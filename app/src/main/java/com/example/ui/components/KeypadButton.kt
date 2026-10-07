package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.KeyActionBg
import com.example.ui.theme.KeyActionText
import com.example.ui.theme.KeyEqualsBg
import com.example.ui.theme.KeyEqualsText
import com.example.ui.theme.KeyNumberBg
import com.example.ui.theme.KeyNumberBorder
import com.example.ui.theme.KeyOpBg
import com.example.ui.theme.KeyOpText
import com.example.ui.theme.KeySciBg
import com.example.ui.theme.KeySciText
import com.example.ui.theme.TextPrimary

enum class KeyVariant {
    NUMBER,
    OPERATOR,
    SCIENTIFIC,
    ACTION,
    EQUALS,
    ACCENT_TOGGLE
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KeyVariant = KeyVariant.NUMBER,
    isActiveToggle: Boolean = false,
    fontSize: TextUnit = 22.sp,
    testTag: String = "btn_$text"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "btnScale"
    )

    val (bgColor, textColor, borderColor) = when (variant) {
        KeyVariant.NUMBER -> Triple(KeyNumberBg, TextPrimary, KeyNumberBorder)
        KeyVariant.OPERATOR -> Triple(KeyOpBg, KeyOpText, Color(0xFF4A3420))
        KeyVariant.SCIENTIFIC -> {
            if (isActiveToggle) {
                Triple(CyanAccent, Color(0xFF090D16), CyanAccent)
            } else {
                Triple(KeySciBg, KeySciText, Color(0xFF233555))
            }
        }
        KeyVariant.ACTION -> Triple(KeyActionBg, KeyActionText, Color(0xFF52212B))
        KeyVariant.EQUALS -> Triple(KeyEqualsBg, KeyEqualsText, Color(0xFF38E1FF))
        KeyVariant.ACCENT_TOGGLE -> {
            if (isActiveToggle) {
                Triple(CyanAccent, Color(0xFF090D16), CyanAccent)
            } else {
                Triple(KeyNumberBg, TextPrimary, KeyNumberBorder)
            }
        }
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = textColor.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = if (variant == KeyVariant.EQUALS || variant == KeyVariant.OPERATOR) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
