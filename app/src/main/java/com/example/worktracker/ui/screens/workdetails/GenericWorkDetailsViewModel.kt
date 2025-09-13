package com.example.worktracker.ui.screens.workdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.example.worktracker.data.repository.ActivityCategoryRepository
import com.example.worktracker.data.repository.ComponentInfoRepository
import com.example.worktracker.data.repository.TheBoysRepository
import com.example.worktracker.data.repository.WorkActivityRepository
// Assuming a structure like this is defined or will be defined in the repository/data layer
// For the purpose of this ViewModel, we'll assume it exists:
// data class WorkActivityLogWithDetails(
//     val workActivity: WorkActivityLog,
//     val components: List<ComponentInfo>,
//     val theBoys: List<TheBoysInfo>
// )
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
    private val theBoysRepository: TheBoysRepository,
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

        // Collect available components and boys, and update selected lists in session
        viewModelScope.launch {
            combine(
                componentInfoRepository.getAllComponents(),
                theBoysRepository.getAllTheBoys(),
                _uiState // Depend on uiState to re-filter when selected IDs change
            ) { availableComponents, availableTheBoys, currentState ->
                Triple(availableComponents, availableTheBoys, currentState)
            }.collectLatest { (availableComponents, availableTheBoys, currentState) ->
                _uiState.update {
                    it.copy(
                        availableComponents = availableComponents,
                        availableTheBoys = availableTheBoys,
                        selectedComponentsInSession = availableComponents.filter { comp ->
                            currentState.selectedComponentIds.contains(comp.id)
                        },
                        selectedTheBoysInSession = availableTheBoys.filter { boy ->
                            currentState.selectedTheBoyIds.contains(boy.boyId.toLong())
                        }
                    )
                }
            }
        }

        if (isEditMode) { // Simplified condition
            // navWorkLogId is guaranteed to be non-null here if isEditMode is true
            loadWorkLogForEditing(navWorkLogId!!)
        } else {
            loadOngoingOrNewLog(categoryName)
        }
    }

    private fun loadWorkLogForEditing(workLogId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ASSUMPTION: workActivityRepository.getWorkActivityWithDetailsById exists
            // and returns a data class WorkActivityLogWithDetails(workActivity: WorkActivityLog, components: List<ComponentInfo>, theBoys: List<TheBoysInfo>)
            val details = workActivityRepository.getWorkActivityWithDetailsById(workLogId) // Placeholder for actual call
            if (details != null) {
                val log = details.workActivity
                _uiState.update {
                    it.copy(
                        currentLogId = log.id, // For consistency, though editingWorkLogId is the primary key for edit
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
                        selectedTheBoyIds = details.theBoys.map { b -> b.boyId.toLong() }.toSet(),
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
                                selectedTheBoyIds = ongoingActivityDetails.theBoys.map { b -> b.boyId.toLong() }.toSet(),
                                endTime = null, // Ongoing, so no end time
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
                                selectedTheBoyIds = emptySet(),
                                isLoading = false,
                                error = null,
                                logDate = System.currentTimeMillis() // Reset log date for new entry
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
        // Display update will be handled by the combined flow in init
    }

    fun onToggleTheBoySelectionDialog(show: Boolean) {
        _uiState.update { it.copy(showTheBoySelectionDialog = show) }
    }

    fun onTheBoySelected(boyId: Long, isSelected: Boolean) {
        _uiState.update {
            val newSelectedIds = if (isSelected) {
                it.selectedTheBoyIds + boyId
            } else {
                it.selectedTheBoyIds - boyId
            }
            it.copy(selectedTheBoyIds = newSelectedIds)
        }
        // Display update will be handled by the combined flow in init
    }

    fun onStartPressed() {
        val currentState = _uiState.value
        if (currentState.isEditMode || currentState.currentLogId != null || currentState.startTime != null) {
            // Should not start if already in edit mode, or if a log is already active/started.
            return
        }

        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val currentCategory = activityCategoryRepository.getCategoryByName(currentState.categoryName)

            val newLog = WorkActivityLog(
                categoryName = currentState.categoryName,
                categoryId = currentCategory?.id?.toLong(),
                startTime = currentTime,
                description = currentState.description, // Can be empty at start
                operatorId = currentState.operatorId.toIntOrNull(),
                expenses = currentState.expenses.toDoubleOrNull(),
                logDate = currentState.logDate,
                taskSuccessful = currentState.taskSuccessful, // Can be null at start
                assignedBy = currentState.assignedBy, // Can be null at start
                endTime = null,
                duration = null
            )
            try {
                val newId = workActivityRepository.insertWorkActivity(
                    log = newLog,
                    componentIds = currentState.selectedComponentIds.toList(),
                    theBoyIds = currentState.selectedTheBoyIds.toList()
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
        var blockingError: String? = null // Errors that prevent saving/ending

        val isActivityStarted = currentState.startTime != null

        // Common validations for started/editing activities
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
        
        // Button enabled if activity started (for create/resume) or in edit mode, and no blocking errors
        val allMandatoryConditionsMet = (isActivityStarted || currentState.isEditMode) &&
                currentState.description.isNotBlank() &&
                (currentState.operatorId.isEmpty() || currentState.operatorId.toIntOrNull() != null) &&
                currentState.taskSuccessful != null &&
                !currentState.assignedBy.isNullOrBlank()

        val finalErrorForDisplay = blockingError ?: validationErrorForDisplay

        _uiState.update {
            it.copy(
                isEndButtonEnabled = allMandatoryConditionsMet, // "End" or "Update" button
                error = if (allMandatoryConditionsMet && finalErrorForDisplay == validationErrorForDisplay) {
                    validationErrorForDisplay
                } else {
                    finalErrorForDisplay
                }
            )
        }
    }

    fun onSaveOrUpdatePressed() { // Renamed from onEndPressed
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
                finalStartTime = currentState.startTime // startTime should be populated from loaded log
                if (logIdToUse == null || finalStartTime == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Error: Editing information is missing.") }
                    return@launch
                }
                // If it was an ongoing log being edited, and user is now "completing" it
                // Or if it was a completed log, we keep its original end time unless explicitly changed (not supported yet)
                // For now, if it had an initial end time, we keep it. If not, we set it now.
                if (currentState.initialEndTimeForEdit == null) {
                    finalEndTime = System.currentTimeMillis()
                    finalDuration = finalEndTime - finalStartTime
                } else {
                    finalEndTime = currentState.initialEndTimeForEdit
                    finalDuration = currentState.initialDurationForEdit
                }
            } else { // Create/Resume mode - completing an ongoing log
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
                logDate = currentState.logDate, // Use logDate from state (could be original or current if new)
                taskSuccessful = currentState.taskSuccessful, // Not null due to validation
                assignedBy = currentState.assignedBy, // Not null due to validation
                duration = finalDuration
            )

            try {
                workActivityRepository.insertWorkActivity( // insertWorkActivity should handle upsert
                    log = logEntry,
                    componentIds = currentState.selectedComponentIds.toList(),
                    theBoyIds = currentState.selectedTheBoyIds.toList()
                )
                _uiState.update { it.copy(isLoading = false) }
                _navigationEvent.emit(NavigationEvent.NavigateBack)
                // UI reset for non-edit mode will happen upon re-entering the screen if it's a "create new" scenario
                // or if getOngoingActivityByCategoryName returns null next time.
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save: ${e.message}") }
            }
        }
    }

    sealed class NavigationEvent {
        object NavigateBack : NavigationEvent()
    }
}
