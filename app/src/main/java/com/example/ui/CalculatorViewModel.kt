package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalculationRecord
import com.example.data.CalculationRepository
import com.example.domain.ScientificEvaluator
import com.example.domain.UnitCategory
import com.example.domain.ConversionUnit
import com.example.domain.UnitRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    STANDARD("Standard"),
    SCIENTIFIC("Scientific"),
    CONVERTER("Converter"),
    HISTORY("History")
}

enum class HistoryFilter {
    ALL, STANDARD, SCIENTIFIC, CONVERSION, FAVORITES
}

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val finalResult: String = "",
    val isDegree: Boolean = true,
    val isInverse: Boolean = false,
    val memoryValue: Double? = null,
    val error: String? = null
)

data class UnitConverterUiState(
    val category: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: ConversionUnit = UnitRepository.unitsByCategory[UnitCategory.LENGTH]!![0],
    val toUnit: ConversionUnit = UnitRepository.unitsByCategory[UnitCategory.LENGTH]!![1],
    val inputString: String = "1",
    val convertedResult: String = "0.001",
    val allConversions: List<UnitRepository.ConversionResultItem> = emptyList()
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculationRepository

    private val _currentTab = MutableStateFlow(AppTab.STANDARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    private val _converterState = MutableStateFlow(UnitConverterUiState())
    val converterState: StateFlow<UnitConverterUiState> = _converterState.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CalculationRepository(db.calculationDao())
        updateConverterCalculations()
    }

    val historyRecords: StateFlow<List<CalculationRecord>> = combine(
        repository.allHistory,
        _historyFilter
    ) { list, filter ->
        when (filter) {
            HistoryFilter.ALL -> list
            HistoryFilter.STANDARD -> list.filter { it.type == "STANDARD" }
            HistoryFilter.SCIENTIFIC -> list.filter { it.type == "SCIENTIFIC" }
            HistoryFilter.CONVERSION -> list.filter { it.type == "CONVERSION" }
            HistoryFilter.FAVORITES -> list.filter { it.isFavorite }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun performHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    // ==========================================
    // CALCULATOR ACTIONS
    // ==========================================

    fun onAppendInput(text: String) {
        performHapticFeedback()
        val cur = _calcState.value.expression

        // If previous state had error, reset
        val baseExpr = if (_calcState.value.error != null) "" else cur

        val newExpr = baseExpr + text
        _calcState.value = _calcState.value.copy(
            expression = newExpr,
            error = null
        )
        updateLivePreview(newExpr, _calcState.value.isDegree)
    }

    fun onBackspace() {
        performHapticFeedback()
        val cur = _calcState.value.expression
        if (cur.isNotEmpty()) {
            // Check if removing a function name (e.g. "sin(", "asin(", "sqrt(", "log(")
            val funcNames = listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sin(", "cos(", "tan(", "sinh(", "cosh(", "tanh(", "sqrt(", "cbrt(", "log(", "log2(", "ln(", "abs(")
            var deleted = false
            for (fn in funcNames) {
                if (cur.endsWith(fn)) {
                    val updated = cur.substring(0, cur.length - fn.length)
                    _calcState.value = _calcState.value.copy(expression = updated, error = null)
                    updateLivePreview(updated, _calcState.value.isDegree)
                    deleted = true
                    break
                }
            }
            if (!deleted) {
                val updated = cur.dropLast(1)
                _calcState.value = _calcState.value.copy(expression = updated, error = null)
                updateLivePreview(updated, _calcState.value.isDegree)
            }
        }
    }

    fun onClearAll() {
        performHapticFeedback()
        _calcState.value = _calcState.value.copy(
            expression = "",
            previewResult = "",
            finalResult = "",
            error = null
        )
    }

    fun onToggleDegRad() {
        performHapticFeedback()
        val newDeg = !_calcState.value.isDegree
        _calcState.value = _calcState.value.copy(isDegree = newDeg)
        updateLivePreview(_calcState.value.expression, newDeg)
    }

    fun onToggleInverse() {
        performHapticFeedback()
        _calcState.value = _calcState.value.copy(isInverse = !_calcState.value.isInverse)
    }

    fun onCalculateEqual(mode: String = "STANDARD") {
        performHapticFeedback()
        val expr = _calcState.value.expression
        if (expr.isBlank()) return

        when (val eval = ScientificEvaluator.evaluate(expr, _calcState.value.isDegree)) {
            is ScientificEvaluator.EvalResult.Success -> {
                _calcState.value = _calcState.value.copy(
                    finalResult = eval.formatted,
                    previewResult = "",
                    error = null
                )
                // Save to Room DB
                viewModelScope.launch {
                    val angleMode = if (_calcState.value.isDegree) "DEG" else "RAD"
                    repository.insert(
                        CalculationRecord(
                            expression = expr,
                            result = eval.formatted,
                            type = mode,
                            detail = angleMode
                        )
                    )
                }
            }
            is ScientificEvaluator.EvalResult.Error -> {
                _calcState.value = _calcState.value.copy(
                    error = eval.message,
                    previewResult = ""
                )
            }
        }
    }

    private fun updateLivePreview(expr: String, isDegree: Boolean) {
        if (expr.isBlank()) {
            _calcState.value = _calcState.value.copy(previewResult = "", error = null)
            return
        }

        // Try evaluating live without throwing errors to user
        when (val eval = ScientificEvaluator.evaluate(expr, isDegree)) {
            is ScientificEvaluator.EvalResult.Success -> {
                _calcState.value = _calcState.value.copy(previewResult = eval.formatted, error = null)
            }
            is ScientificEvaluator.EvalResult.Error -> {
                // Incomplete expression while typing is normal, so keep preview empty or existing
                _calcState.value = _calcState.value.copy(previewResult = "")
            }
        }
    }

    // Memory operations: M+, M-, MR, MC, MS
    fun memoryAdd() {
        performHapticFeedback()
        val currentVal = getCurrentNumericValue() ?: return
        val currentMem = _calcState.value.memoryValue ?: 0.0
        val newMem = currentMem + currentVal
        _calcState.value = _calcState.value.copy(memoryValue = newMem)
        emitToast("M+ : ${ScientificEvaluator.formatNumber(newMem)}")
    }

    fun memorySubtract() {
        performHapticFeedback()
        val currentVal = getCurrentNumericValue() ?: return
        val currentMem = _calcState.value.memoryValue ?: 0.0
        val newMem = currentMem - currentVal
        _calcState.value = _calcState.value.copy(memoryValue = newMem)
        emitToast("M- : ${ScientificEvaluator.formatNumber(newMem)}")
    }

    fun memoryRecall() {
        performHapticFeedback()
        val mem = _calcState.value.memoryValue ?: return
        val formatted = ScientificEvaluator.formatNumber(mem)
        onAppendInput(formatted)
    }

    fun memoryClear() {
        performHapticFeedback()
        _calcState.value = _calcState.value.copy(memoryValue = null)
        emitToast("Memory cleared")
    }

    fun memoryStore() {
        performHapticFeedback()
        val currentVal = getCurrentNumericValue() ?: return
        _calcState.value = _calcState.value.copy(memoryValue = currentVal)
        emitToast("MS : ${ScientificEvaluator.formatNumber(currentVal)}")
    }

    private fun getCurrentNumericValue(): Double? {
        if (_calcState.value.finalResult.isNotEmpty()) {
            return _calcState.value.finalResult.toDoubleOrNull()
        }
        if (_calcState.value.previewResult.isNotEmpty()) {
            return _calcState.value.previewResult.toDoubleOrNull()
        }
        val eval = ScientificEvaluator.evaluate(_calcState.value.expression, _calcState.value.isDegree)
        return if (eval is ScientificEvaluator.EvalResult.Success) eval.value else null
    }

    fun restoreCalculation(expr: String, result: String) {
        _calcState.value = _calcState.value.copy(
            expression = expr,
            finalResult = result,
            previewResult = "",
            error = null
        )
        _currentTab.value = AppTab.STANDARD
        emitToast("Restored calculation")
    }

    fun useResultAsInput(result: String) {
        _calcState.value = _calcState.value.copy(
            expression = result,
            finalResult = "",
            previewResult = "",
            error = null
        )
        _currentTab.value = AppTab.STANDARD
        emitToast("Result inserted")
    }

    fun copyToClipboard(text: String, label: String = "Calculation") {
        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            emitToast("Copied \"$text\" to clipboard")
        } catch (_: Exception) {}
    }

    // ==========================================
    // UNIT CONVERTER ACTIONS
    // ==========================================

    fun selectConverterCategory(cat: UnitCategory) {
        performHapticFeedback()
        val units = UnitRepository.unitsByCategory[cat] ?: return
        val from = units[0]
        val to = if (units.size > 1) units[1] else units[0]
        _converterState.value = _converterState.value.copy(
            category = cat,
            fromUnit = from,
            toUnit = to
        )
        updateConverterCalculations()
    }

    fun selectFromUnit(unit: ConversionUnit) {
        performHapticFeedback()
        _converterState.value = _converterState.value.copy(fromUnit = unit)
        updateConverterCalculations()
    }

    fun selectToUnit(unit: ConversionUnit) {
        performHapticFeedback()
        _converterState.value = _converterState.value.copy(toUnit = unit)
        updateConverterCalculations()
    }

    fun swapConverterUnits() {
        performHapticFeedback()
        val cur = _converterState.value
        _converterState.value = cur.copy(
            fromUnit = cur.toUnit,
            toUnit = cur.fromUnit
        )
        updateConverterCalculations()
    }

    fun onConverterKeypadInput(key: String) {
        performHapticFeedback()
        val cur = _converterState.value.inputString
        val updated = when (key) {
            "C" -> "0"
            "⌫" -> if (cur.length > 1) cur.dropLast(1) else "0"
            "+/-" -> {
                if (cur.startsWith("-")) cur.substring(1) else if (cur != "0") "-$cur" else cur
            }
            "." -> {
                if (cur.contains(".")) cur else "$cur."
            }
            else -> {
                if (cur == "0" && key != ".") key else cur + key
            }
        }
        _converterState.value = _converterState.value.copy(inputString = updated)
        updateConverterCalculations()
    }

    fun setConverterInputDirectly(value: String) {
        _converterState.value = _converterState.value.copy(inputString = value)
        updateConverterCalculations()
    }

    fun saveConversionToHistory() {
        val state = _converterState.value
        val inputVal = state.inputString.toDoubleOrNull() ?: return
        val expr = "$inputVal ${state.fromUnit.symbol}"
        val res = "${state.convertedResult} ${state.toUnit.symbol}"
        viewModelScope.launch {
            repository.insert(
                CalculationRecord(
                    expression = expr,
                    result = res,
                    type = "CONVERSION",
                    detail = "${state.category.displayName} (${state.fromUnit.symbol} → ${state.toUnit.symbol})"
                )
            )
            emitToast("Saved to history")
        }
    }

    private fun updateConverterCalculations() {
        val state = _converterState.value
        val num = state.inputString.toDoubleOrNull() ?: 0.0

        val converted = UnitRepository.convert(num, state.fromUnit, state.toUnit)
        val formatted = UnitRepository.formatUnitValue(converted)

        val allList = UnitRepository.convertToAll(state.category, num, state.fromUnit)

        _converterState.value = state.copy(
            convertedResult = formatted,
            allConversions = allList
        )
    }

    // ==========================================
    // HISTORY ACTIONS
    // ==========================================

    fun toggleFavorite(record: CalculationRecord) {
        performHapticFeedback()
        viewModelScope.launch {
            repository.toggleFavorite(record.id, record.isFavorite)
        }
    }

    fun deleteHistoryItem(id: Long) {
        performHapticFeedback()
        viewModelScope.launch {
            repository.delete(id)
            emitToast("Calculation deleted")
        }
    }

    fun clearAllHistory() {
        performHapticFeedback()
        viewModelScope.launch {
            repository.clearAll()
            emitToast("All history cleared")
        }
    }

    private fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }
}
