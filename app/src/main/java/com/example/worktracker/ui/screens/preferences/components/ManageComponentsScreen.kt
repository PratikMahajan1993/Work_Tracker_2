package com.example.worktracker.ui.screens.preferences.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel // Corrected import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageComponentsScreen(
    onNavigateBack: () -> Unit,
    editingComponentId: Long? = null, // Optional: for when navigating to edit
    viewModel: ManageComponentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(editingComponentId) {
        if (editingComponentId != null && editingComponentId != 0L) {
            viewModel.handleEvent(ManageComponentsUiEvent.LoadComponentForEditing(editingComponentId))
        } else {
            // If no valid ID is passed, ensure form is cleared or in 'add new' state
            viewModel.handleEvent(ManageComponentsUiEvent.ClearFormForNewEntry)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.editingComponentId != null) "Edit Component" else "Add Component") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.handleEvent(ManageComponentsUiEvent.ClearFormForNewEntry) // Clear form on back
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ComponentInputForm(uiState = uiState, onEvent = viewModel::handleEvent)
            
            Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom if content is less

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
            // Save/Update button is now part of ComponentInputForm
        }
         // DeleteConfirmationDialog has been moved to ComponentListScreen
    }
}

@Composable
fun ComponentInputForm(
    uiState: ManageComponentsUiState,
    onEvent: (ManageComponentsUiEvent) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.componentName,
            onValueChange = { onEvent(ManageComponentsUiEvent.OnComponentNameChange(it)) },
            label = { Text("Component Name*") },
            isError = uiState.componentNameError != null,
            supportingText = { uiState.componentNameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.customer,
            onValueChange = { onEvent(ManageComponentsUiEvent.OnCustomerChange(it)) },
            label = { Text("Customer*") },
            isError = uiState.customerError != null,
            supportingText = { uiState.customerError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.priorityLevelString,
            onValueChange = { onEvent(ManageComponentsUiEvent.OnPriorityLevelChange(it)) },
            label = { Text("Priority Level (1-5)*") },
            isError = uiState.priorityLevelError != null,
            supportingText = { uiState.priorityLevelError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.cycleTimeMinutesString,
            onValueChange = { onEvent(ManageComponentsUiEvent.OnCycleTimeChange(it)) },
            label = { Text("Cycle Time (minutes)*") },
            isError = uiState.cycleTimeMinutesError != null,
            supportingText = { uiState.cycleTimeMinutesError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Changed to KeyboardType.Decimal
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.notesForAi,
            onValueChange = { onEvent(ManageComponentsUiEvent.OnNotesForAiChange(it)) },
            label = { Text("Notes for AI (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(8.dp)) // Extra space before button

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = if (uiState.editingComponentId != null) Arrangement.SpaceEvenly else Arrangement.Center
        ) {
            Button(onClick = { onEvent(ManageComponentsUiEvent.OnSaveComponentClick) }) {
                Text(if (uiState.editingComponentId == null) "Add Component" else "Update Component")
            }
            if (uiState.editingComponentId != null) {
                Button(onClick = { onEvent(ManageComponentsUiEvent.OnClearFormClick) }) {
                    Text("Cancel Edit")
                }
            }
        }
    }
}

// ComponentList has been moved to ComponentListScreen.kt
// ComponentListItem has been moved to ComponentListScreen.kt (as ActualComponentListItem)
// DeleteConfirmationDialog has been moved to ComponentListScreen.kt (as ActualDeleteConfirmationDialog)
