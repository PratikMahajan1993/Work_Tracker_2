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

const val ROUTE_ARG_PRODUCTION_LOG_ID = "productionLogId" // Consistent with AppRoutes

data class LogProductionUiState(
    val isLoadingBoys: Boolean = true,
    val theBoysList: List<TheBoysInfo> = emptyList(),
    val selectedBoy: TheBoysInfo? = null,
    val selectedBoyError: String? = null,

    val isLoadingComponents: Boolean = true,
    val componentList: List<ComponentInfo> = emptyList(),
    val selectedComponent: ComponentInfo? = null,
    val selectedComponentError: String? = null,

    val machineNumber: String = "",
    val productionQuantity: String = "",
    val rejectionQuantity: String = "", // New field for UI

    val startTimeInput: String = "", // User input for start time HH:mm
    val startTimeError: String? = null,
    val startTimeMillis: Long? = null, // Derived from startTimeInput and current date

    val downtimeInput: String = "", // User input for downtime in minutes
    val downtimeError: String? = null,

    val machineNumberError: String? = null,
    val productionQuantityError: String? = null,
    val rejectionQuantityError: String? = null, // New error field for UI
    val snackbarMessage: String? = null,

    // Edit mode fields
    val isEditMode: Boolean = false,
    val editingProductionLogId: Long? = null,
    val initialStartTimeForEdit: Long? = null, // Stores original startTime when editing
    val initialEndTimeForEdit: Long? = null,   // Stores original endTime when editing (if log was completed)
    val initialDurationForEdit: Long? = null,// Stores original duration when editing (if log was completed)
    
    val isSaving: Boolean = false,
    val navigateBack: Boolean = false
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

        if (isEdit) { // Simplified condition
            // productionLogId is guaranteed to be non-null here if isEdit is true
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
                        rejectionQuantity = log.rejectionQuantity?.toString() ?: "", // Load rejection qty
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

    fun onBoySelected(boy: TheBoysInfo?) {
        _uiState.update { it.copy(selectedBoy = boy, selectedBoyError = null) }
    }

    fun onComponentSelected(component: ComponentInfo?) {
        _uiState.update { it.copy(selectedComponent = component, selectedComponentError = null) }
    }

    fun onMachineNumberChange(number: String) {
        _uiState.update { it.copy(machineNumber = number, machineNumberError = null) }
    }

    fun onProductionQuantityChange(quantity: String) {
        _uiState.update { it.copy(productionQuantity = quantity, productionQuantityError = null) }
    }

    fun onRejectionQuantityChange(quantity: String) { // New handler
        _uiState.update { it.copy(rejectionQuantity = quantity, rejectionQuantityError = null) }
    }

    fun onStartTimeInputChange(input: String) {
        _uiState.update { it.copy(startTimeInput = input, startTimeError = null) }
        if (input.matches(Regex("([01]?[0-9]|2[0-3]):[0-5][0-9]"))) {
            try {
                val parsedDate = timeFormatter.parse(input)
                if (parsedDate != null) {
                    val calendar = Calendar.getInstance()
                    val baseDateMillis = if (_uiState.value.isEditMode && _uiState.value.initialStartTimeForEdit != null) {
                        _uiState.value.initialStartTimeForEdit!!
                    } else {
                        System.currentTimeMillis()
                    }
                    val baseCalendar = Calendar.getInstance().apply { timeInMillis = baseDateMillis }
                    
                    calendar.time = parsedDate
                    calendar.set(Calendar.YEAR, baseCalendar.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, baseCalendar.get(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, baseCalendar.get(Calendar.DAY_OF_MONTH))
                    
                    _uiState.update { it.copy(startTimeMillis = calendar.timeInMillis, startTimeError = null) }
                } else {
                    _uiState.update { it.copy(startTimeMillis = null, startTimeError = "Invalid time format.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(startTimeMillis = null, startTimeError = "Invalid time format.") }
            }
        } else if (input.isNotBlank()) {
             _uiState.update { it.copy(startTimeMillis = null, startTimeError = "Use HH:mm format.") }
        } else {
            _uiState.update { it.copy(startTimeMillis = null, startTimeError = null) }
        }
    }

    fun onDowntimeInputChange(input: String) {
        _uiState.update { it.copy(downtimeInput = input, downtimeError = null) }
    }
    
    fun onNavigatedBack() {
        _uiState.update { it.copy(navigateBack = false) }
    }

    fun resetFormAndTimer() { 
        _uiState.update {
            LogProductionUiState(
                theBoysList = it.theBoysList,
                componentList = it.componentList,
                isEditMode = false, 
                editingProductionLogId = null,
                initialStartTimeForEdit = null,
                initialEndTimeForEdit = null,
                initialDurationForEdit = null,
                isLoadingBoys = false, 
                isLoadingComponents = false,
                rejectionQuantity = "", // Reset rejection quantity
                rejectionQuantityError = null
            )
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        _uiState.update { 
            it.copy(
                selectedBoyError = null, selectedComponentError = null,
                machineNumberError = null, productionQuantityError = null,
                rejectionQuantityError = null, // Clear rejection error
                startTimeError = null, downtimeError = null,
                snackbarMessage = null
            )
        }
        val currentState = _uiState.value

        if (currentState.selectedBoy == null) {
            _uiState.update { it.copy(selectedBoyError = "A 'Boy' must be selected.") }; isValid = false
        }
        if (currentState.selectedComponent == null) {
            _uiState.update { it.copy(selectedComponentError = "A Component must be selected.") }; isValid = false
        }
        if (currentState.machineNumber.toIntOrNull() == null || currentState.machineNumber.toInt() <= 0) {
            _uiState.update { it.copy(machineNumberError = "Machine number must be a positive integer.") }; isValid = false
        }
        if (currentState.productionQuantity.toIntOrNull() == null || currentState.productionQuantity.toInt() <= 0) {
            _uiState.update { it.copy(productionQuantityError = "Quantity must be a positive integer.") }; isValid = false
        }
        if (currentState.rejectionQuantity.isNotBlank() && (currentState.rejectionQuantity.toIntOrNull() == null || currentState.rejectionQuantity.toInt() < 0)) {
            _uiState.update { it.copy(rejectionQuantityError = "Rejection quantity must be a non-negative integer.")}; isValid = false
        }
        if (currentState.startTimeInput.isBlank()) {
            _uiState.update { it.copy(startTimeError = "Start time cannot be empty.") }; isValid = false
        } else if (currentState.startTimeMillis == null) {
            _uiState.update { it.copy(startTimeError = currentState.startTimeError ?: "Invalid start time format (HH:mm).") }; isValid = false
        }
        if (currentState.downtimeInput.isNotBlank() && (currentState.downtimeInput.toIntOrNull() == null || currentState.downtimeInput.toInt() < 0)) {
            _uiState.update { it.copy(downtimeError = "Downtime must be a non-negative integer.") }; isValid = false
        }
        return isValid
    }

    fun onSaveOrUpdatePressed() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isSaving = true) }
        val currentState = _uiState.value
        
        val currentStartTimeMillis: Long = currentState.startTimeMillis!! // Validated non-null by validateInputs

        val resolvedId: Long
        val resolvedEndTime: Long
        var resolvedDuration: Long // Explicitly non-nullable Long

        if (currentState.isEditMode) {
            resolvedId = currentState.editingProductionLogId ?: 0L 

            if (currentState.initialEndTimeForEdit != null) { 
                resolvedEndTime = currentState.initialEndTimeForEdit
                resolvedDuration = if (currentStartTimeMillis != currentState.initialStartTimeForEdit) {
                    resolvedEndTime - currentStartTimeMillis
                } else {
                    currentState.initialDurationForEdit ?: (resolvedEndTime - currentStartTimeMillis)
                }

                if (resolvedDuration < 0) {
                    _uiState.update { it.copy(isSaving = false, snackbarMessage = "Start time cannot be after original end time.") }
                    return@onSaveOrUpdatePressed
                }
            } else { 
                resolvedEndTime = System.currentTimeMillis()
                resolvedDuration = resolvedEndTime - currentStartTimeMillis
            }
        } else { 
            resolvedId = 0L 
            resolvedEndTime = System.currentTimeMillis()
            resolvedDuration = resolvedEndTime - currentStartTimeMillis
        }

        if (resolvedDuration < 0) {
            resolvedDuration = 0L
        }

        val downtimeMinutes: Int? = if (currentState.downtimeInput.isBlank()) {
            null
        } else {
            currentState.downtimeInput.toIntOrNull()
        }

        val rejectionQuantity: Int? = if (currentState.rejectionQuantity.isBlank()) { // Parse rejection qty
            null
        } else {
            currentState.rejectionQuantity.toIntOrNull()
        }

        val activity = ProductionActivity(
            id = resolvedId,
            boyId = currentState.selectedBoy!!.boyId, 
            componentName = currentState.selectedComponent!!.componentName, 
            machineNumber = currentState.machineNumber.toInt(), 
            productionQuantity = currentState.productionQuantity.toInt(), 
            rejectionQuantity = rejectionQuantity, // Add to entity
            startTime = currentStartTimeMillis,
            endTime = resolvedEndTime,
            duration = resolvedDuration, 
            downtimeMinutes = downtimeMinutes
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

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
