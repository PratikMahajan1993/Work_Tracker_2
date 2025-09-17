package com.example.worktracker.ui.screens.workdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.repository.ActivityCategoryRepository
import com.example.worktracker.data.repository.ComponentInfoRepository
import com.example.worktracker.data.repository.WorkActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ROUTE_ARG_WORK_LOG_ID = "workLogId"

@HiltViewModel
class GenericWorkDetailsViewModel @Inject constructor(
    private val workActivityRepository: WorkActivityRepository,
    private val componentInfoRepository: ComponentInfoRepository,
    private val activityCategoryRepository: ActivityCategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkDetailsState())
    val uiState: StateFlow<WorkDetailsState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        val navWorkLogId: Long? = savedStateHandle.get<Long>(ROUTE_ARG_WORK_LOG_ID)
        val categoryName: String = savedStateHandle.get<String>(ROUTE_ARG_CATEGORY_NAME) ?: "Unknown Category"
        val isEditMode = navWorkLogId != null && navWorkLogId > 0L

        _uiState.update {
            it.copy(
                categoryName = categoryName,
                editingWorkLogId = if (isEditMode) navWorkLogId else null,
                isEditMode = isEditMode,
                logDate = System.currentTimeMillis() // Default log date
            )
        }

        // Collect available components and update selected lists in session
        viewModelScope.launch {
            combine(
                componentInfoRepository.getAllComponents(),
                _uiState // Depend on uiState to re-filter when selected IDs change
            ) { availableComponents, currentState ->
                Pair(availableComponents, currentState)
            }.collectLatest { (availableComponents, currentState) ->
                _uiState.update {
                    it.copy(
                        availableComponents = availableComponents,
                        selectedComponentsInSession = availableComponents.filter { comp ->
                            currentState.selectedComponentIds.contains(comp.id)
                        }
                    )
                }
            }
        }

        if (isEditMode) { // Simplified condition
            loadWorkLogForEditing(navWorkLogId!!)
        } else {
            loadOngoingOrNewLog(categoryName)
        }
    }

    private fun loadWorkLogForEditing(workLogId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ASSUMPTION: workActivityRepository.getWorkActivityWithDetailsById exists
            // and returns a data class WorkActivityLogWithDetails(workActivity: WorkActivityLog, components: List<ComponentInfo>)
            val details = workActivityRepository.getWorkActivityWithDetailsById(workLogId) // Placeholder for actual call
            if (details != null) {
                val log = details.workActivity
                _uiState.update {
                    it.copy(
                        currentLogId = log.id,
                        startTime = log.startTime,
                        endTime = log.endTime,
                        duration = log.duration,
                        description = log.description ?: "",
                        operatorId = log.operatorId?.toString() ?: "",
                        expenses = log.expenses?.toString() ?: "",
                        logDate = log.logDate,
                        taskSuccessful = log.taskSuccessful,
                        assignedBy = log.assignedBy,
                        selectedComponentIds = details.components.map { c -> c.id }.toSet(),
                        initialEndTimeForEdit = log.endTime,
                        initialDurationForEdit = log.duration,
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load activity details for editing.")
                }
            }
            validateInputsAndSetButtonState()
        }
    }

    private fun loadOngoingOrNewLog(categoryName: String) {
        viewModelScope.launch {
            workActivityRepository.getOngoingActivityByCategoryName(categoryName)
                .collectLatest { ongoingActivityDetails ->
                    if (ongoingActivityDetails != null) {
                        val log = ongoingActivityDetails.workActivity
                        _uiState.update {
                            it.copy(
                                currentLogId = log.id,
                                startTime = log.startTime,
                                description = log.description ?: "",
                                operatorId = log.operatorId?.toString() ?: "",
                                expenses = log.expenses?.toString() ?: "",
                                logDate = log.logDate,
                                taskSuccessful = log.taskSuccessful,
                                assignedBy = log.assignedBy,
                                selectedComponentIds = ongoingActivityDetails.components.map { c -> c.id }.toSet(),
                                endTime = null,
                                duration = null,
                                isLoading = false,
                                error = null
                            )
                        }
                    } else {
                        // New log, reset relevant fields
                        _uiState.update {
                            it.copy(
                                currentLogId = null,
                                startTime = null,
                                endTime = null,
                                duration = null,
                                description = "",
                                operatorId = "",
                                expenses = "",
                                taskSuccessful = null,
                                assignedBy = null,
                                selectedComponentIds = emptySet(),
                                isLoading = false,
                                error = null,
                                logDate = System.currentTimeMillis()
                            )
                        }
                    }
                    validateInputsAndSetButtonState()
                }
        }
    }


    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription, error = null) }
        validateInputsAndSetButtonState()
    }

    fun onOperatorIdChange(newId: String) {
        _uiState.update { it.copy(operatorId = newId, error = null) }
        validateInputsAndSetButtonState()
    }

    fun onExpensesChange(newExpenses: String) {
        _uiState.update { it.copy(expenses = newExpenses, error = null) }
        validateInputsAndSetButtonState()
    }

    fun onTaskSuccessChanged(isSuccess: Boolean) {
        _uiState.update { it.copy(taskSuccessful = isSuccess, error = null) }
        validateInputsAndSetButtonState()
    }

    fun onAssignedByChanged(assignee: String) {
        _uiState.update { it.copy(assignedBy = assignee, error = null) }
        validateInputsAndSetButtonState()
    }

    fun onToggleComponentSelectionDialog(show: Boolean) {
        _uiState.update { it.copy(showComponentSelectionDialog = show) }
    }

    fun onComponentSelected(componentId: Long, isSelected: Boolean) {
        _uiState.update {
            val newSelectedIds = if (isSelected) {
                it.selectedComponentIds + componentId
            } else {
                it.selectedComponentIds - componentId
            }
            it.copy(selectedComponentIds = newSelectedIds)
        }
    }

    // Removed TheBoys selection dialog functions

    fun onStartPressed() {
        val currentState = _uiState.value
        if (currentState.isEditMode || currentState.currentLogId != null || currentState.startTime != null) {
            return
        }

        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val currentCategory = activityCategoryRepository.getCategoryByName(currentState.categoryName)

            val newLog = WorkActivityLog(
                categoryName = currentState.categoryName,
                categoryId = currentCategory?.id?.toLong(),
                startTime = currentTime,
                description = currentState.description,
                operatorId = currentState.operatorId.toIntOrNull(),
                expenses = currentState.expenses.toDoubleOrNull(),
                logDate = currentState.logDate,
                taskSuccessful = currentState.taskSuccessful,
                assignedBy = currentState.assignedBy,
                endTime = null,
                duration = null
            )
            try {
                val newId = workActivityRepository.insertWorkActivity(
                    log = newLog,
                    componentIds = currentState.selectedComponentIds.toList()
                )
                _uiState.update {
                    it.copy(
                        currentLogId = newId,
                        startTime = currentTime,
                        error = null
                    )
                }
                validateInputsAndSetButtonState()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to start activity: ${e.message}") }
            }
        }
    }

    private fun validateInputsAndSetButtonState() {
        val currentState = _uiState.value
        var validationErrorForDisplay: String? = null
        var blockingError: String? = null

        val isActivityStarted = currentState.startTime != null

        if (isActivityStarted || currentState.isEditMode) {
            if (currentState.description.isBlank()) {
                blockingError = blockingError ?: "Description cannot be empty."
            }
            val operatorIdInt = currentState.operatorId.toIntOrNull()
            if (currentState.operatorId.isNotEmpty() && operatorIdInt == null) {
                blockingError = blockingError ?: "Operator ID must be a valid number or empty."
            }
            if (currentState.taskSuccessful == null) {
                blockingError = blockingError ?: "Please select if the task was successful."
            }
            if (currentState.assignedBy.isNullOrBlank()) {
                blockingError = blockingError ?: "Please select who assigned the task."
            }
        }

        val expensesDouble = currentState.expenses.toDoubleOrNull()
        if (currentState.expenses.isNotEmpty() && (expensesDouble == null || expensesDouble < 0.0)) {
            validationErrorForDisplay = "Expenses must be a valid non-negative number or empty."
        }
        
        val allMandatoryConditionsMet = (isActivityStarted || currentState.isEditMode) &&
                currentState.description.isNotBlank() &&
                (currentState.operatorId.isEmpty() || currentState.operatorId.toIntOrNull() != null) &&
                currentState.taskSuccessful != null &&
                !currentState.assignedBy.isNullOrBlank()

        val finalErrorForDisplay = blockingError ?: validationErrorForDisplay

        _uiState.update {
            it.copy(
                isEndButtonEnabled = allMandatoryConditionsMet,
                error = if (allMandatoryConditionsMet && finalErrorForDisplay == validationErrorForDisplay) {
                    validationErrorForDisplay
                } else {
                    finalErrorForDisplay
                }
            )
        }
    }

    fun onSaveOrUpdatePressed() {
        validateInputsAndSetButtonState()
        val currentState = _uiState.value

        if (!currentState.isEndButtonEnabled) {
            if (currentState.error == null && (currentState.startTime != null || currentState.isEditMode) ) {
                 _uiState.update { it.copy(error = "Please ensure all required fields are correctly filled.") }
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val logIdToUse: Long?
            val finalStartTime: Long?
            var finalEndTime: Long?
            var finalDuration: Long?

            if (currentState.isEditMode) {
                logIdToUse = currentState.editingWorkLogId
                finalStartTime = currentState.startTime
                if (logIdToUse == null || finalStartTime == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Error: Editing information is missing.") }
                    return@launch
                }
                if (currentState.initialEndTimeForEdit == null) {
                    finalEndTime = System.currentTimeMillis()
                    finalDuration = finalEndTime - finalStartTime
                } else {
                    finalEndTime = currentState.initialEndTimeForEdit
                    finalDuration = currentState.initialDurationForEdit
                }
            } else {
                logIdToUse = currentState.currentLogId
                finalStartTime = currentState.startTime
                if (logIdToUse == null || finalStartTime == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Cannot save: No active session found.") }
                    return@launch
                }
                finalEndTime = System.currentTimeMillis()
                finalDuration = finalEndTime - finalStartTime
            }

            val currentCategory = activityCategoryRepository.getCategoryByName(currentState.categoryName)

            val logEntry = WorkActivityLog(
                id = logIdToUse,
                categoryName = currentState.categoryName,
                categoryId = currentCategory?.id?.toLong(),
                startTime = finalStartTime,
                endTime = finalEndTime,
                description = currentState.description,
                operatorId = currentState.operatorId.toIntOrNull(),
                expenses = currentState.expenses.toDoubleOrNull(),
                logDate = currentState.logDate,
                taskSuccessful = currentState.taskSuccessful,
                assignedBy = currentState.assignedBy,
                duration = finalDuration
            )

            try {
                workActivityRepository.insertWorkActivity(
                    log = logEntry,
                    componentIds = currentState.selectedComponentIds.toList()
                )
                _uiState.update { it.copy(isLoading = false) }
                _navigationEvent.emit(NavigationEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save: ${e.message}") }
            }
        }
    }

    sealed class NavigationEvent {
        object NavigateBack : NavigationEvent()
    }
}
