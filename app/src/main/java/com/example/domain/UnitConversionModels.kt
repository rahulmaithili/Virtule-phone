package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(val displayName: String, val iconName: String) {
    LENGTH("Length", "straighten"),
    MASS("Mass & Weight", "scale"),
    TEMPERATURE("Temperature", "thermostat"),
    VOLUME("Volume", "water_drop"),
    AREA("Area", "crop_square"),
    SPEED("Speed", "speed"),
    TIME("Time", "schedule"),
    DIGITAL("Digital Data", "storage"),
    PRESSURE("Pressure", "compress"),
    ENERGY("Energy", "bolt")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    // Factor to convert 1 of this unit to base unit
    val toBaseFactor: Double = 1.0,
    // For non-linear conversion (e.g. temperature)
    val customToBase: ((Double) -> Double)? = null,
    val customFromBase: ((Double) -> Double)? = null
) {
    fun toBase(value: Double): Double {
        return customToBase?.invoke(value) ?: (value * toBaseFactor)
    }

    fun fromBase(baseValue: Double): Double {
        return customFromBase?.invoke(baseValue) ?: (baseValue / toBaseFactor)
    }
}

object UnitRepository {

    val unitsByCategory: Map<UnitCategory, List<ConversionUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            ConversionUnit("m", "Meter", "m", 1.0),
            ConversionUnit("km", "Kilometer", "km", 1000.0),
            ConversionUnit("cm", "Centimeter", "cm", 0.01),
            ConversionUnit("mm", "Millimeter", "mm", 0.001),
            ConversionUnit("mi", "Mile", "mi", 1609.344),
            ConversionUnit("yd", "Yard", "yd", 0.9144),
            ConversionUnit("ft", "Foot", "ft", 0.3048),
            ConversionUnit("in", "Inch", "in", 0.0254),
            ConversionUnit("nm", "Nautical Mile", "NM", 1852.0)
        ),
        UnitCategory.MASS to listOf(
            ConversionUnit("kg", "Kilogram", "kg", 1.0),
            ConversionUnit("g", "Gram", "g", 0.001),
            ConversionUnit("mg", "Milligram", "mg", 0.000001),
            ConversionUnit("lb", "Pound", "lb", 0.45359237),
            ConversionUnit("oz", "Ounce", "oz", 0.028349523125),
            ConversionUnit("t", "Metric Ton", "t", 1000.0),
            ConversionUnit("st", "Stone", "st", 6.35029318)
        ),
        UnitCategory.TEMPERATURE to listOf(
            ConversionUnit(
                id = "c",
                name = "Celsius",
                symbol = "°C",
                customToBase = { it },
                customFromBase = { it }
            ),
            ConversionUnit(
                id = "f",
                name = "Fahrenheit",
                symbol = "°F",
                customToBase = { (it - 32.0) * (5.0 / 9.0) },
                customFromBase = { (it * 9.0 / 5.0) + 32.0 }
            ),
            ConversionUnit(
                id = "k",
                name = "Kelvin",
                symbol = "K",
                customToBase = { it - 273.15 },
                customFromBase = { it + 273.15 }
            )
        ),
        UnitCategory.VOLUME to listOf(
            ConversionUnit("l", "Liter", "L", 1.0),
            ConversionUnit("ml", "Milliliter", "mL", 0.001),
            ConversionUnit("m3", "Cubic Meter", "m³", 1000.0),
            ConversionUnit("gal", "Gallon (US)", "gal", 3.785411784),
            ConversionUnit("qt", "Quart (US)", "qt", 0.946352946),
            ConversionUnit("pt", "Pint (US)", "pt", 0.473176473),
            ConversionUnit("cup", "Cup (US)", "cup", 0.2365882365),
            ConversionUnit("floz", "Fluid Ounce (US)", "fl oz", 0.0295735295625),
            ConversionUnit("tbsp", "Tablespoon", "tbsp", 0.01478676478),
            ConversionUnit("tsp", "Teaspoon", "tsp", 0.00492892159)
        ),
        UnitCategory.AREA to listOf(
            ConversionUnit("m2", "Square Meter", "m²", 1.0),
            ConversionUnit("km2", "Square Kilometer", "km²", 1_000_000.0),
            ConversionUnit("ft2", "Square Foot", "sq ft", 0.09290304),
            ConversionUnit("yd2", "Square Yard", "sq yd", 0.83612736),
            ConversionUnit("mi2", "Square Mile", "sq mi", 2_589_988.110336),
            ConversionUnit("ac", "Acre", "ac", 4046.8564224),
            ConversionUnit("ha", "Hectare", "ha", 10_000.0)
        ),
        UnitCategory.SPEED to listOf(
            ConversionUnit("mps", "Meter/Second", "m/s", 1.0),
            ConversionUnit("kmh", "Kilometer/Hour", "km/h", 1.0 / 3.6),
            ConversionUnit("mph", "Miles/Hour", "mph", 0.44704),
            ConversionUnit("kn", "Knot", "kn", 0.51444444444),
            ConversionUnit("fps", "Foot/Second", "ft/s", 0.3048)
        ),
        UnitCategory.TIME to listOf(
            ConversionUnit("s", "Second", "s", 1.0),
            ConversionUnit("ms", "Millisecond", "ms", 0.001),
            ConversionUnit("min", "Minute", "min", 60.0),
            ConversionUnit("h", "Hour", "h", 3600.0),
            ConversionUnit("d", "Day", "d", 86400.0),
            ConversionUnit("wk", "Week", "wk", 604800.0),
            ConversionUnit("yr", "Year (365d)", "yr", 31536000.0)
        ),
        UnitCategory.DIGITAL to listOf(
            ConversionUnit("b", "Bit", "b", 0.125),
            ConversionUnit("B", "Byte", "B", 1.0),
            ConversionUnit("KB", "Kilobyte", "KB", 1024.0),
            ConversionUnit("MB", "Megabyte", "MB", 1024.0 * 1024.0),
            ConversionUnit("GB", "Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("TB", "Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("PB", "Petabyte", "PB", 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0)
        ),
        UnitCategory.PRESSURE to listOf(
            ConversionUnit("pa", "Pascal", "Pa", 1.0),
            ConversionUnit("bar", "Bar", "bar", 100000.0),
            ConversionUnit("psi", "Pound/Sq Inch", "psi", 6894.757293),
            ConversionUnit("atm", "Atmosphere", "atm", 101325.0),
            ConversionUnit("torr", "Torr (mmHg)", "mmHg", 133.322368)
        ),
        UnitCategory.ENERGY to listOf(
            ConversionUnit("j", "Joule", "J", 1.0),
            ConversionUnit("kj", "Kilojoule", "kJ", 1000.0),
            ConversionUnit("cal", "Calorie", "cal", 4.184),
            ConversionUnit("kcal", "Kilocalorie", "kcal", 4184.0),
            ConversionUnit("wh", "Watt-hour", "Wh", 3600.0),
            ConversionUnit("kwh", "Kilowatt-hour", "kWh", 3_600_000.0),
            ConversionUnit("btu", "BTU", "BTU", 1055.056)
        )
    )

    fun convert(value: Double, fromUnit: ConversionUnit, toUnit: ConversionUnit): Double {
        if (fromUnit.id == toUnit.id) return value
        val baseVal = fromUnit.toBase(value)
        return toUnit.fromBase(baseVal)
    }

    data class ConversionResultItem(
        val unit: ConversionUnit,
        val formattedValue: String,
        val numericValue: Double
    )

    fun convertToAll(
        category: UnitCategory,
        inputValue: Double,
        fromUnit: ConversionUnit
    ): List<ConversionResultItem> {
        val list = unitsByCategory[category] ?: emptyList()
        val baseVal = fromUnit.toBase(inputValue)
        return list.map { unit ->
            val result = unit.fromBase(baseVal)
            ConversionResultItem(
                unit = unit,
                formattedValue = formatUnitValue(result),
                numericValue = result
            )
        }
    }

    fun formatUnitValue(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        val absVal = kotlin.math.abs(value)
        if (absVal != 0.0 && (absVal >= 1e9 || absVal < 1e-4)) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val sciFormat = DecimalFormat("0.####E0", symbols)
            return sciFormat.format(value).replace("E", "e")
        }
        return try {
            val bd = BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
            bd.toPlainString()
        } catch (e: Exception) {
            value.toString()
        }
    }
}
