package com.example.worktracker.ui.logproduction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.data.repository.ComponentInfoRepository
import com.example.worktracker.data.repository.ProductionActivityRepository
import com.example.worktracker.data.repository.TheBoysRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

const val ROUTE_ARG_PRODUCTION_LOG_ID = "productionLogId"

// Field constants for the questionnaire map
const val FIELD_BOY = "boy"
const val FIELD_COMPONENT = "component"
const val FIELD_MACHINE_NUMBER = "machineNumber"
const val FIELD_PROD_QTY = "productionQuantity"
const val FIELD_REJECT_QTY = "rejectionQuantity"
const val FIELD_START_TIME = "startTime"
const val FIELD_DOWNTIME = "downtime"

data class LogProductionUiState(
    // Common state for both modes
    val isLoadingBoys: Boolean = true,
    val theBoysList: List<TheBoysInfo> = emptyList(),
    val isLoadingComponents: Boolean = true,
    val componentList: List<ComponentInfo> = emptyList(),
    val snackbarMessage: String? = null,
    val isSaving: Boolean = false,
    val navigateBack: Boolean = false,
    val isEditMode: Boolean = false,

    // Edit Mode State (Original full form)
    val editingProductionLogId: Long? = null,
    val selectedBoy: TheBoysInfo? = null,
    val selectedComponent: ComponentInfo? = null,
    val machineNumber: String = "",
    val productionQuantity: String = "",
    val rejectionQuantity: String = "",
    val startTimeInput: String = "",
    val downtimeInput: String = "",
    val startTimeMillis: Long? = null,
    val initialStartTimeForEdit: Long? = null,
    val initialEndTimeForEdit: Long? = null,
    val initialDurationForEdit: Long? = null,
    val selectedBoyError: String? = null,
    val selectedComponentError: String? = null,
    val machineNumberError: String? = null,
    val productionQuantityError: String? = null,
    val rejectionQuantityError: String? = null,
    val startTimeError: String? = null,
    val downtimeError: String? = null,

    // Create Mode State (New Questionnaire)
    val addProductionLogStep: Int = 0,
    val newProductionLogInputs: Map<String, String> = emptyMap(),
    val newProductionLogErrors: Map<String, String?> = emptyMap()
)

@HiltViewModel
class LogProductionViewModel @Inject constructor(
    private val theBoysRepository: TheBoysRepository,
    private val productionActivityRepository: ProductionActivityRepository,
    private val componentInfoRepository: ComponentInfoRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogProductionUiState())
    val uiState: StateFlow<LogProductionUiState> = _uiState.asStateFlow()

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Defines the order of questions for the new log questionnaire
    val addProductionLogFieldOrder = listOf(
        FIELD_BOY,
        FIELD_COMPONENT,
        FIELD_MACHINE_NUMBER,
        FIELD_PROD_QTY,
        FIELD_REJECT_QTY,
        FIELD_START_TIME,
        FIELD_DOWNTIME
    )

    init {
        val productionLogId: Long? = savedStateHandle.get<Long>(ROUTE_ARG_PRODUCTION_LOG_ID)
        val isEdit = productionLogId != null && productionLogId > 0L

        _uiState.update {
            it.copy(
                isEditMode = isEdit,
                editingProductionLogId = if (isEdit) productionLogId else null
            )
        }

        loadTheBoysList()
        loadComponentsList()

        if (isEdit) {
            loadProductionLogForEditing(productionLogId!!)
        }
    }

    private fun loadProductionLogForEditing(logId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val log = productionActivityRepository.getProductionActivityById(logId)
            if (log != null) {
                while (_uiState.value.isLoadingBoys || _uiState.value.isLoadingComponents) {
                    kotlinx.coroutines.delay(100)
                }

                val calendar = Calendar.getInstance().apply { timeInMillis = log.startTime }
                val startTimeStr = timeFormatter.format(calendar.time)

                _uiState.update {
                    it.copy(
                        selectedBoy = it.theBoysList.find { boy -> boy.boyId == log.boyId },
                        selectedComponent = it.componentList.find { comp -> comp.componentName == log.componentName },
                        machineNumber = log.machineNumber.toString(),
                        productionQuantity = log.productionQuantity.toString(),
                        rejectionQuantity = log.rejectionQuantity?.toString() ?: "",
                        startTimeInput = startTimeStr,
                        startTimeMillis = log.startTime,
                        downtimeInput = log.downtimeMinutes?.toString() ?: "",
                        initialStartTimeForEdit = log.startTime,
                        initialEndTimeForEdit = log.endTime,
                        initialDurationForEdit = log.duration,
                        isSaving = false
                    )
                }
            } else {
                _uiState.update { it.copy(isSaving = false, snackbarMessage = "Failed to load production log.") }
            }
        }
    }

    private fun loadTheBoysList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBoys = true) }
            val boys = theBoysRepository.getAllTheBoys().first()
            _uiState.update { it.copy(isLoadingBoys = false, theBoysList = boys) }
        }
    }

    private fun loadComponentsList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComponents = true) }
            componentInfoRepository.getAllComponents().collect { components ->
                _uiState.update { it.copy(isLoadingComponents = false, componentList = components) }
            }
        }
    }

    //region Edit Mode Functions
    fun onBoySelected(boy: TheBoysInfo?) { _uiState.update { it.copy(selectedBoy = boy, selectedBoyError = null) } }
    fun onComponentSelected(component: ComponentInfo?) { _uiState.update { it.copy(selectedComponent = component, selectedComponentError = null) } }
    fun onMachineNumberChange(number: String) { _uiState.update { it.copy(machineNumber = number, machineNumberError = null) } }
    fun onProductionQuantityChange(quantity: String) { _uiState.update { it.copy(productionQuantity = quantity, productionQuantityError = null) } }
    fun onRejectionQuantityChange(quantity: String) { _uiState.update { it.copy(rejectionQuantity = quantity, rejectionQuantityError = null) } }
    fun onDowntimeInputChange(input: String) { _uiState.update { it.copy(downtimeInput = input, downtimeError = null) } }
    fun onStartTimeInputChange(input: String) {
        _uiState.update { it.copy(startTimeInput = input, startTimeError = null) }
        // This validation and parsing logic is only for the edit mode's text field.
        validateAndParseTime(input)
    }
    //endregion

    //region Questionnaire Functions
    fun onNewProductionLogInputChange(fieldName: String, value: String) {
        val currentInputs = _uiState.value.newProductionLogInputs.toMutableMap()
        currentInputs[fieldName] = value
        val currentErrors = _uiState.value.newProductionLogErrors.toMutableMap()
        currentErrors[fieldName] = null
        _uiState.update { it.copy(newProductionLogInputs = currentInputs, newProductionLogErrors = currentErrors) }
    }

    fun onNextStep() {
        if (validateCurrentStep()) {
            val currentStep = _uiState.value.addProductionLogStep
            if (currentStep < addProductionLogFieldOrder.size - 1) {
                _uiState.update { it.copy(addProductionLogStep = currentStep + 1) }
            } else {
                onSaveOrUpdatePressed() // Auto-save on the last step
            }
        }
    }

    fun onPreviousStep() {
        val currentStep = _uiState.value.addProductionLogStep
        if (currentStep > 0) {
            _uiState.update { it.copy(addProductionLogStep = currentStep - 1) }
        }
    }
    //endregion

    fun onNavigatedBack() { _uiState.update { it.copy(navigateBack = false) } }
    fun clearSnackbarMessage() { _uiState.update { it.copy(snackbarMessage = null) } }

    fun resetFormAndTimer() {
        _uiState.update {
            // Preserve the fetched lists of boys and components
            LogProductionUiState(
                theBoysList = it.theBoysList,
                componentList = it.componentList,
                isLoadingBoys = false,
                isLoadingComponents = false
            )
        }
    }

    private fun validateCurrentStep(): Boolean {
        val state = _uiState.value
        val currentStep = state.addProductionLogStep
        val fieldName = addProductionLogFieldOrder[currentStep]
        val value = state.newProductionLogInputs[fieldName]
        var error: String? = null

        when (fieldName) {
            FIELD_BOY -> if (value.isNullOrBlank()) error = "A 'Boy' must be selected."
            FIELD_COMPONENT -> if (value.isNullOrBlank()) error = "A Component must be selected."
            FIELD_MACHINE_NUMBER -> if (value.isNullOrBlank() || value.toIntOrNull() == null || value.toInt() <= 0) error = "Must be a positive number."
            FIELD_PROD_QTY -> if (value.isNullOrBlank() || value.toIntOrNull() == null || value.toInt() <= 0) error = "Must be a positive number."
            FIELD_REJECT_QTY -> if (!value.isNullOrBlank() && (value.toIntOrNull() == null || value.toInt() < 0)) error = "Must be a non-negative number."
            FIELD_START_TIME -> if (value.isNullOrBlank() || !value.matches(Regex("([01]?[0-9]|2[0-3]):[0-5][0-9]"))) error = "Use HH:mm format."
            FIELD_DOWNTIME -> if (!value.isNullOrBlank() && (value.toIntOrNull() == null || value.toInt() < 0)) error = "Must be a non-negative number."
        }

        if (error != null) {
            val currentErrors = state.newProductionLogErrors.toMutableMap()
            currentErrors[fieldName] = error
            _uiState.update { it.copy(newProductionLogErrors = currentErrors) }
            return false
        }
        return true
    }

    private fun validateInputsForEditMode(): Boolean {
        var isValid = true
        _uiState.update { it.copy(selectedBoyError = null, selectedComponentError = null, machineNumberError = null, productionQuantityError = null, rejectionQuantityError = null, startTimeError = null, downtimeError = null, snackbarMessage = null) }
        val currentState = _uiState.value

        if (currentState.selectedBoy == null) { _uiState.update { it.copy(selectedBoyError = "A 'Boy' must be selected.") }; isValid = false }
        if (currentState.selectedComponent == null) { _uiState.update { it.copy(selectedComponentError = "A Component must be selected.") }; isValid = false }
        if (currentState.machineNumber.toIntOrNull() == null || currentState.machineNumber.toInt() <= 0) { _uiState.update { it.copy(machineNumberError = "Machine number must be a positive integer.") }; isValid = false }
        if (currentState.productionQuantity.toIntOrNull() == null || currentState.productionQuantity.toInt() <= 0) { _uiState.update { it.copy(productionQuantityError = "Quantity must be a positive integer.") }; isValid = false }
        if (currentState.rejectionQuantity.isNotBlank() && (currentState.rejectionQuantity.toIntOrNull() == null || currentState.rejectionQuantity.toInt() < 0)) { _uiState.update { it.copy(rejectionQuantityError = "Rejection quantity must be a non-negative integer.") }; isValid = false }
        if (currentState.startTimeInput.isBlank()) { _uiState.update { it.copy(startTimeError = "Start time cannot be empty.") }; isValid = false }
        else if (currentState.startTimeMillis == null) { _uiState.update { it.copy(startTimeError = currentState.startTimeError ?: "Invalid start time format (HH:mm).") }; isValid = false }
        if (currentState.downtimeInput.isNotBlank() && (currentState.downtimeInput.toIntOrNull() == null || currentState.downtimeInput.toInt() < 0)) { _uiState.update { it.copy(downtimeError = "Downtime must be a non-negative integer.") }; isValid = false }
        return isValid
    }

    // Helper data class for extracted form data
    private data class FormData(
        val boyId: Int,
        val componentName: String,
        val machineNum: Int,
        val prodQty: Int,
        val rejectQty: Int?,
        val startTimeMillis: Long,
        val downtimeMins: Int?
    )

    fun onSaveOrUpdatePressed() {
        if (_uiState.value.isEditMode) {
            if (!validateInputsForEditMode()) return
        }
        // No pre-validation needed for questionnaire as it's done per step

        _uiState.update { it.copy(isSaving = true) }
        val currentState = _uiState.value

        val formData = if (currentState.isEditMode) {
            FormData(
                boyId = currentState.selectedBoy!!.boyId,
                componentName = currentState.selectedComponent!!.componentName,
                machineNum = currentState.machineNumber.toInt(),
                prodQty = currentState.productionQuantity.toInt(),
                rejectQty = currentState.rejectionQuantity.toIntOrNull(),
                startTimeMillis = currentState.startTimeMillis!!,
                downtimeMins = currentState.downtimeInput.toIntOrNull()
            )
        } else {
            val inputs = currentState.newProductionLogInputs
            val boy = currentState.theBoysList.find { it.boyId.toString() == inputs[FIELD_BOY] }!!
            val component = currentState.componentList.find { it.id.toString() == inputs[FIELD_COMPONENT] }!!
            val timeMillis = parseTimeInputToMillis(inputs[FIELD_START_TIME]!!, System.currentTimeMillis())
            FormData(
                boyId = boy.boyId,
                componentName = component.componentName,
                machineNum = inputs[FIELD_MACHINE_NUMBER]!!.toInt(),
                prodQty = inputs[FIELD_PROD_QTY]!!.toInt(),
                rejectQty = inputs[FIELD_REJECT_QTY]?.toIntOrNull(),
                startTimeMillis = timeMillis,
                downtimeMins = inputs[FIELD_DOWNTIME]?.toIntOrNull()
            )
        }

        val resolvedId: Long = if (currentState.isEditMode) currentState.editingProductionLogId!! else 0L
        val resolvedEndTime: Long
        var resolvedDuration: Long

        if (currentState.isEditMode && currentState.initialEndTimeForEdit != null) {
            resolvedEndTime = currentState.initialEndTimeForEdit
            val initialDuration = currentState.initialDurationForEdit ?: (resolvedEndTime - (currentState.initialStartTimeForEdit ?: 0L))
            resolvedDuration = if (formData.startTimeMillis != currentState.initialStartTimeForEdit) resolvedEndTime - formData.startTimeMillis else initialDuration
        } else {
            resolvedEndTime = System.currentTimeMillis()
            resolvedDuration = resolvedEndTime - formData.startTimeMillis
        }

        if (resolvedDuration < 0) { resolvedDuration = 0L }

        val activity = ProductionActivity(
            id = resolvedId,
            boyId = formData.boyId,
            componentName = formData.componentName,
            machineNumber = formData.machineNum,
            productionQuantity = formData.prodQty,
            rejectionQuantity = formData.rejectQty,
            startTime = formData.startTimeMillis,
            endTime = resolvedEndTime,
            duration = resolvedDuration,
            downtimeMinutes = formData.downtimeMins
        )

        viewModelScope.launch {
            try {
                productionActivityRepository.insertProductionActivity(activity)
                val message = if (currentState.isEditMode) "Production activity updated!" else "Production activity saved!"
                _uiState.update { it.copy(isSaving = false, snackbarMessage = message, navigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, snackbarMessage = "Error: ${e.message}") }
            }
        }
    }

    private fun validateAndParseTime(input: String) {
        if (input.matches(Regex("([01]?[0-9]|2[0-3]):[0-5][0-9]"))) {
            try {
                val baseDate = if (_uiState.value.isEditMode && _uiState.value.initialStartTimeForEdit != null) _uiState.value.initialStartTimeForEdit!! else System.currentTimeMillis()
                val parsedMillis = parseTimeInputToMillis(input, baseDate)
                _uiState.update { it.copy(startTimeMillis = parsedMillis, startTimeError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(startTimeMillis = null, startTimeError = "Invalid time format.") }
            }
        } else if (input.isNotBlank()) {
            _uiState.update { it.copy(startTimeMillis = null, startTimeError = "Use HH:mm format.") }
        } else {
            _uiState.update { it.copy(startTimeMillis = null, startTimeError = null) }
        }
    }
    
    private fun parseTimeInputToMillis(timeInput: String, baseDateMillis: Long): Long {
        val parsedDate = timeFormatter.parse(timeInput)!!
        val timeCalendar = Calendar.getInstance().apply { time = parsedDate }
        val baseCalendar = Calendar.getInstance().apply { timeInMillis = baseDateMillis }
        timeCalendar.set(Calendar.YEAR, baseCalendar.get(Calendar.YEAR))
        timeCalendar.set(Calendar.MONTH, baseCalendar.get(Calendar.MONTH))
        timeCalendar.set(Calendar.DAY_OF_MONTH, baseCalendar.get(Calendar.DAY_OF_MONTH))
        return timeCalendar.timeInMillis
    }
}
