package com.example.worktracker.ui.screens.preferences.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.repository.ComponentInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageComponentsUiState(
    val components: List<ComponentInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Form fields
    val editingComponentId: Long? = null,
    val componentName: String = "",
    val customer: String = "",
    val priorityLevelString: String = "", // Store as String for TextField
    val cycleTimeMinutesString: String = "", // Store as String for TextField
    val notesForAi: String = "",

    // Validation errors
    val componentNameError: String? = null,
    val customerError: String? = null,
    val priorityLevelError: String? = null,
    val cycleTimeMinutesError: String? = null,

    val showDeleteConfirmationDialog: Boolean = false,
    val componentToDelete: ComponentInfo? = null
)

sealed interface ManageComponentsUiEvent {
    data class OnComponentNameChange(val name: String) : ManageComponentsUiEvent
    data class OnCustomerChange(val customer: String) : ManageComponentsUiEvent
    data class OnPriorityLevelChange(val priority: String) : ManageComponentsUiEvent
    data class OnCycleTimeChange(val cycleTime: String) : ManageComponentsUiEvent
    data class OnNotesForAiChange(val notes: String) : ManageComponentsUiEvent
    object OnSaveComponentClick : ManageComponentsUiEvent
    data class OnDeleteComponentClick(val component: ComponentInfo) : ManageComponentsUiEvent // From ComponentListScreen
    object OnConfirmDeleteComponent : ManageComponentsUiEvent
    object OnDismissDeleteComponentDialog : ManageComponentsUiEvent
    data class OnEditComponentClick(val component: ComponentInfo) : ManageComponentsUiEvent // Legacy, might be removed if not used
    object OnClearFormClick : ManageComponentsUiEvent // User initiated form clear
    data class LoadComponentForEditing(val componentId: Long) : ManageComponentsUiEvent // For navigation
    object ClearFormForNewEntry : ManageComponentsUiEvent // For navigation or explicit new entry state
}

@HiltViewModel
class ManageComponentsViewModel @Inject constructor(
    private val componentInfoRepository: ComponentInfoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageComponentsUiState())
    val uiState: StateFlow<ManageComponentsUiState> = _uiState.asStateFlow()

    init {
        loadAllComponentsForListScreen() // Renamed for clarity
    }

    // This loads all components for the ComponentListScreen
    private fun loadAllComponentsForListScreen() {
        viewModelScope.launch {
            componentInfoRepository.getAllComponents().collectLatest { components ->
                _uiState.update { it.copy(components = components) }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        _uiState.update { it.copy(
            componentNameError = null,
            customerError = null,
            priorityLevelError = null,
            cycleTimeMinutesError = null,
            errorMessage = null
        )}

        if (_uiState.value.componentName.isBlank()) {
            _uiState.update { it.copy(componentNameError = "Component name cannot be empty") }
            isValid = false
        }
        if (_uiState.value.customer.isBlank()) {
            _uiState.update { it.copy(customerError = "Customer name cannot be empty") }
            isValid = false
        }

        val priority = _uiState.value.priorityLevelString.toIntOrNull()
        if (priority == null || priority !in 1..5) {
            _uiState.update { it.copy(priorityLevelError = "Priority must be a number between 1 and 5") }
            isValid = false
        }

        val cycleTime = _uiState.value.cycleTimeMinutesString.toDoubleOrNull() // Changed to toDoubleOrNull
        if (cycleTime == null || cycleTime <= 0) {
            _uiState.update { it.copy(cycleTimeMinutesError = "Cycle time must be a positive number") }
            isValid = false
        }
        return isValid
    }

    fun handleEvent(event: ManageComponentsUiEvent) {
        when (event) {
            is ManageComponentsUiEvent.OnComponentNameChange -> _uiState.update { it.copy(componentName = event.name, componentNameError = null, errorMessage = null) }
            is ManageComponentsUiEvent.OnCustomerChange -> _uiState.update { it.copy(customer = event.customer, customerError = null, errorMessage = null) }
            is ManageComponentsUiEvent.OnPriorityLevelChange -> _uiState.update { it.copy(priorityLevelString = event.priority, priorityLevelError = null, errorMessage = null) }
            is ManageComponentsUiEvent.OnCycleTimeChange -> _uiState.update { it.copy(cycleTimeMinutesString = event.cycleTime, cycleTimeMinutesError = null, errorMessage = null) }
            is ManageComponentsUiEvent.OnNotesForAiChange -> _uiState.update { it.copy(notesForAi = event.notes) }
            ManageComponentsUiEvent.OnSaveComponentClick -> saveComponent()
            is ManageComponentsUiEvent.OnDeleteComponentClick -> {
                _uiState.update { it.copy(showDeleteConfirmationDialog = true, componentToDelete = event.component) }
            }
            ManageComponentsUiEvent.OnConfirmDeleteComponent -> {
                _uiState.value.componentToDelete?.let { component ->
                    viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true) } // Indicate loading for delete
                        componentInfoRepository.deleteComponent(component)
                        _uiState.update { it.copy(showDeleteConfirmationDialog = false, componentToDelete = null, isLoading = false) }
                        // The component list on ComponentListScreen will update automatically via its own collection of the flow
                    }
                }
            }
            ManageComponentsUiEvent.OnDismissDeleteComponentDialog -> {
                 _uiState.update { it.copy(showDeleteConfirmationDialog = false, componentToDelete = null) }
            }
            is ManageComponentsUiEvent.OnEditComponentClick -> {
                val component = event.component
                _uiState.update {
                    it.copy(
                        editingComponentId = component.id,
                        componentName = component.componentName,
                        customer = component.customer,
                        priorityLevelString = component.priorityLevel.toString(),
                        cycleTimeMinutesString = component.cycleTimeMinutes.toString(), // Stays as toString for Double
                        notesForAi = component.notesForAi ?: "",
                        componentNameError = null, customerError = null, priorityLevelError = null, cycleTimeMinutesError = null, errorMessage = null
                    )
                }
            }
            ManageComponentsUiEvent.OnClearFormClick -> clearForm() // User initiated form clear/cancel edit
            ManageComponentsUiEvent.ClearFormForNewEntry -> clearForm() // For navigation to a new entry state
            is ManageComponentsUiEvent.LoadComponentForEditing -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) } // Loading state for form population
                    componentInfoRepository.getComponentById(event.componentId).collectLatest { component ->
                        if (component != null) {
                            _uiState.update {
                                it.copy(
                                    editingComponentId = component.id,
                                    componentName = component.componentName,
                                    customer = component.customer,
                                    priorityLevelString = component.priorityLevel.toString(),
                                    cycleTimeMinutesString = component.cycleTimeMinutes.toString(), // Stays as toString for Double
                                    notesForAi = component.notesForAi ?: "",
                                    isLoading = false
                                )
                            }
                        } else {
                            clearForm() 
                            _uiState.update { it.copy(isLoading = false, errorMessage = "Component with ID ${event.componentId} not found. Add new component instead.") }
                        }
                    }
                }
            }
        }
    }

    private fun saveComponent() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val componentInfo = ComponentInfo(
                id = _uiState.value.editingComponentId ?: 0L, 
                componentName = _uiState.value.componentName.trim(),
                customer = _uiState.value.customer.trim(),
                priorityLevel = _uiState.value.priorityLevelString.toInt(),
                cycleTimeMinutes = _uiState.value.cycleTimeMinutesString.toDouble(), // Changed to toDouble
                notesForAi = _uiState.value.notesForAi.trim().takeIf { it.isNotBlank() }
            )

            val result = if (_uiState.value.editingComponentId == null) {
                componentInfoRepository.insertComponent(componentInfo)
            } else {
                componentInfoRepository.updateComponent(componentInfo)
            }

            result.fold(
                onSuccess = {
                    clearForm() 
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message ?: "An unknown error occurred") }
                }
            )
        }
    }

    private fun clearForm() {
        _uiState.update {
            it.copy(
                editingComponentId = null,
                componentName = "",
                customer = "",
                priorityLevelString = "",
                cycleTimeMinutesString = "",
                notesForAi = "",
                componentNameError = null,
                customerError = null,
                priorityLevelError = null,
                cycleTimeMinutesError = null,
                errorMessage = null,
                isLoading = false 
            )
        }
    }
}
