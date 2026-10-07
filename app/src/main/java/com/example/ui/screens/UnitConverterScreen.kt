package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ConversionUnit
import com.example.domain.UnitCategory
import com.example.domain.UnitRepository
import com.example.ui.CalculatorViewModel
import com.example.ui.UnitConverterUiState
import com.example.ui.components.KeyVariant
import com.example.ui.components.KeypadButton
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.KeyNumberBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun UnitConverterScreen(
    state: UnitConverterUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var showFromUnitDialog by remember { mutableStateOf(false) }
    var showToUnitDialog by remember { mutableStateOf(false) }
    var showAllConversionsSheet by remember { mutableStateOf(false) }

    val categoryUnits = remember(state.category) {
        UnitRepository.unitsByCategory[state.category] ?: emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("unit_converter_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Selector Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(UnitCategory.values()) { category ->
                    val isSelected = category == state.category
                    val icon = getCategoryIcon(category)

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectConverterCategory(category) },
                        label = {
                            Text(
                                text = category.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = category.displayName,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurface,
                            labelColor = TextSecondary,
                            iconColor = TextSecondary,
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color(0xFF090D16),
                            selectedLeadingIconColor = Color(0xFF090D16)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = KeyNumberBorder,
                            selectedBorderColor = CyanAccent,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("cat_chip_${category.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversion Conversion Cards: FROM & TO with SWAP button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conversion_main_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, KeyNumberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // FROM SECTION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Unit Picker Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, KeyNumberBorder, RoundedCornerShape(10.dp))
                                .clickable { showFromUnitDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("from_unit_picker"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.fromUnit.name} (${state.fromUnit.symbol})",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select unit",
                                tint = CyanAccent
                            )
                        }

                        // Input Value
                        Text(
                            text = state.inputString,
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f).padding(start = 12.dp).testTag("from_value_text")
                        )
                    }

                    // SWAP ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(KeyNumberBorder)
                        )
                        IconButton(
                            onClick = { viewModel.swapConverterUnits() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.dp, CyanAccent.copy(alpha = 0.5f), CircleShape)
                                .testTag("swap_units_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap units",
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(KeyNumberBorder)
                        )
                    }

                    // TO SECTION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // To Unit Picker Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, KeyNumberBorder, RoundedCornerShape(10.dp))
                                .clickable { showToUnitDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("to_unit_picker"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.toUnit.name} (${state.toUnit.symbol})",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select unit",
                                tint = CyanAccent
                            )
                        }

                        // Converted Result with Copy & Save Actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text(
                                text = state.convertedResult,
                                color = CyanAccent,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false).testTag("to_value_text")
                            )
                            IconButton(
                                onClick = {
                                    viewModel.copyToClipboard(
                                        "${state.convertedResult} ${state.toUnit.symbol}",
                                        "Unit Conversion"
                                    )
                                },
                                modifier = Modifier.size(32.dp).testTag("copy_conversion_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy converted value",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.saveConversionToHistory() },
                                modifier = Modifier.size(32.dp).testTag("save_conversion_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = "Save to history",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Toggle: View All Units Conversion Drawer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All conversions for ${state.inputString} ${state.fromUnit.symbol}:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                TextButton(
                    onClick = { showAllConversionsSheet = !showAllConversionsSheet },
                    modifier = Modifier.testTag("toggle_all_conversions")
                ) {
                    Text(
                        text = if (showAllConversionsSheet) "Hide Grid" else "Show All (${state.allConversions.size})",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }
            }

            AnimatedVisibility(visible = showAllConversionsSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, KeyNumberBorder, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.allConversions) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .clickable {
                                        viewModel.selectToUnit(item.unit)
                                        viewModel.copyToClipboard("${item.formattedValue} ${item.unit.symbol}")
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.unit.name} (${item.unit.symbol})",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = item.formattedValue,
                                    color = CyanAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Built-in Converter Numeric Keypad (4x4 grid: C, +/-, ⌫, ÷; 7 8 9 ×; 4 5 6 -; 1 2 3 +; 0 .)
        ConverterNumericPad(
            onKeyClick = { viewModel.onConverterKeypadInput(it) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }

    // Unit Picker Dialogs
    if (showFromUnitDialog) {
        UnitPickerDialog(
            title = "Select Input Unit",
            units = categoryUnits,
            selectedUnit = state.fromUnit,
            onSelect = {
                viewModel.selectFromUnit(it)
                showFromUnitDialog = false
            },
            onDismiss = { showFromUnitDialog = false }
        )
    }

    if (showToUnitDialog) {
        UnitPickerDialog(
            title = "Select Output Unit",
            units = categoryUnits,
            selectedUnit = state.toUnit,
            onSelect = {
                viewModel.selectToUnit(it)
                showToUnitDialog = false
            },
            onDismiss = { showToUnitDialog = false }
        )
    }
}

@Composable
private fun ConverterNumericPad(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: C, +/-, ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "C",
                onClick = { onKeyClick("C") },
                variant = KeyVariant.ACTION,
                modifier = Modifier.weight(1f),
                testTag = "conv_key_c"
            )
            KeypadButton(
                text = "+/-",
                onClick = { onKeyClick("+/-") },
                variant = KeyVariant.NUMBER,
                modifier = Modifier.weight(1f),
                testTag = "conv_key_sign"
            )
            KeypadButton(
                text = "⌫",
                onClick = { onKeyClick("⌫") },
                variant = KeyVariant.ACTION,
                modifier = Modifier.weight(1f),
                testTag = "conv_key_backspace"
            )
        }

        // Row 2: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "7",
                onClick = { onKeyClick("7") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_7"
            )
            KeypadButton(
                text = "8",
                onClick = { onKeyClick("8") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_8"
            )
            KeypadButton(
                text = "9",
                onClick = { onKeyClick("9") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_9"
            )
        }

        // Row 3: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "4",
                onClick = { onKeyClick("4") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_4"
            )
            KeypadButton(
                text = "5",
                onClick = { onKeyClick("5") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_5"
            )
            KeypadButton(
                text = "6",
                onClick = { onKeyClick("6") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_6"
            )
        }

        // Row 4: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "1",
                onClick = { onKeyClick("1") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_1"
            )
            KeypadButton(
                text = "2",
                onClick = { onKeyClick("2") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_2"
            )
            KeypadButton(
                text = "3",
                onClick = { onKeyClick("3") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_3"
            )
        }

        // Row 5: 0, .
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "0",
                onClick = { onKeyClick("0") },
                modifier = Modifier.weight(2f),
                testTag = "conv_key_0"
            )
            KeypadButton(
                text = ".",
                onClick = { onKeyClick(".") },
                modifier = Modifier.weight(1f),
                testTag = "conv_key_dot"
            )
        }
    }
}

@Composable
private fun UnitPickerDialog(
    title: String,
    units: List<ConversionUnit>,
    selectedUnit: ConversionUnit,
    onSelect: (ConversionUnit) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(units) { unit ->
                    val isCurrent = unit.id == selectedUnit.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) DarkSurfaceVariant else Color.Transparent)
                            .clickable { onSelect(unit) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${unit.name} (${unit.symbol})",
                            color = if (isCurrent) CyanAccent else TextPrimary,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CyanAccent)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun getCategoryIcon(cat: UnitCategory): ImageVector {
    return when (cat) {
        UnitCategory.LENGTH -> Icons.Default.Straighten
        UnitCategory.MASS -> Icons.Default.Scale
        UnitCategory.TEMPERATURE -> Icons.Default.DeviceThermostat
        UnitCategory.VOLUME -> Icons.Default.WaterDrop
        UnitCategory.AREA -> Icons.Default.CropSquare
        UnitCategory.SPEED -> Icons.Default.Speed
        UnitCategory.TIME -> Icons.Default.Schedule
        UnitCategory.DIGITAL -> Icons.Default.Storage
        UnitCategory.PRESSURE -> Icons.Default.Compress
        UnitCategory.ENERGY -> Icons.Default.Bolt
    }
}
