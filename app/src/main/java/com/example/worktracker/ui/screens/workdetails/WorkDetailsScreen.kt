package com.example.worktracker.ui.screens.workdetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.ui.theme.WorkTrackerTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object AssignedByOptions {
    const val TOP_MGT = "TOP MGT"
    const val CEO = "CEO"
    const val SELF = "SELF"
    val list = listOf(TOP_MGT, CEO, SELF)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkDetailsScreen(
    state: WorkDetailsState,
    actions: WorkDetailsActions, 
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    // Content (description, operator ID, etc.) is enabled if editing or if activity has started.
    val isContentFieldsEnabled = state.isEditMode || state.startTime != null

    if (state.showComponentSelectionDialog) {
        ComponentSelectionDialog(
            availableComponents = state.availableComponents,
            selectedComponentIds = state.selectedComponentIds,
            onComponentSelected = actions.onComponentSelected,
            onDismissRequest = { actions.onToggleComponentSelectionDialog(false) }
        )
    }
    // Similar dialog for TheBoys if it exists in the design, omitted for now based on current file content

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit: ${state.categoryName}" else "Log: ${state.categoryName}") }, 
                navigationIcon = {
                    IconButton(onClick = actions.onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Date: ${dateFormatter.format(Date(state.logDate))}", 
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            TimeInformationSection(state = state, actions = actions)

            OutlinedTextField(
                value = state.description,
                onValueChange = actions.onDescriptionChanged,
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                singleLine = false,
                isError = state.error?.contains("Description") == true,
                enabled = isContentFieldsEnabled
            )

            OutlinedTextField(
                value = state.operatorId,
                onValueChange = actions.onOperatorIdChanged,
                label = { Text("Operator ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = state.error?.contains("Operator ID") == true,
                enabled = isContentFieldsEnabled
            )

            OutlinedTextField(
                value = state.expenses,
                onValueChange = actions.onExpensesChanged,
                label = { Text("Expenses") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("Rs") },
                singleLine = true,
                isError = state.error?.contains("Expenses") == true,
                enabled = isContentFieldsEnabled
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Task Successful?", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.selectableGroup()) {
                    RadioButtonWithText(text = "Yes", selected = state.taskSuccessful == true, enabled = isContentFieldsEnabled) {
                        actions.onTaskSuccessChanged(true)
                    }
                    Spacer(Modifier.width(8.dp))
                    RadioButtonWithText(text = "No", selected = state.taskSuccessful == false, enabled = isContentFieldsEnabled) {
                        actions.onTaskSuccessChanged(false)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Assigned By:", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.selectableGroup()) {
                    AssignedByOptions.list.forEach { option ->
                        RadioButtonWithText(text = option, selected = state.assignedBy == option, enabled = isContentFieldsEnabled) {
                            actions.onAssignedByChanged(option)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Components Associated:", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { actions.onToggleComponentSelectionDialog(true) }, enabled = isContentFieldsEnabled) {
                        Icon(Icons.Filled.AddCircleOutline, contentDescription = "Select Components")
                    }
                }
                if (state.selectedComponentsInSession.isEmpty() && isContentFieldsEnabled) {
                    Text("No components selected.", style = MaterialTheme.typography.bodySmall)
                } else if (state.selectedComponentsInSession.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.selectedComponentsInSession.forEach { component ->
                            InputChip(
                                selected = true,
                                onClick = { /* Future: Maybe allow removal from here or toggle selection dialog for this item */ },
                                label = { Text(component.componentName) }
                            )
                        }
                    }
                }
            }

            // Similar section for TheBoys if needed

            Spacer(modifier = Modifier.weight(1f)) 

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = actions.onSaveOrUpdatePressed, // Updated action
                enabled = state.isEndButtonEnabled && !state.isLoading, // ViewModel controls this based on validation
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.isEditMode) "Update Activity" 
                    else if (state.endTime != null) "Activity Ended" // Should ideally not happen if log is ended
                    else "End Activity & Save"
                )
            }
        }
    }
}

@Composable
fun TimeInformationSection(state: WorkDetailsState, actions: WorkDetailsActions) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    var currentTimeForDuration by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(state.startTime, state.endTime) { // More specific keys
        if (state.startTime != null && state.endTime == null) {
            while (true) {
                delay(1000)
                currentTimeForDuration = System.currentTimeMillis()
            }
        }
    }

    val durationString by remember(state.startTime, state.endTime, state.duration, currentTimeForDuration, state.isEditMode, state.initialDurationForEdit) {
        derivedStateOf {
            if (state.isEditMode) {
                if (state.initialDurationForEdit != null) {
                    formatDuration(state.initialDurationForEdit)
                } else if (state.startTime != null) { // Editing an ongoing log
                    val diff = currentTimeForDuration - state.startTime
                    if (diff >= 0) formatDuration(diff) else "--:--:--"
                } else "--:--:--"
            } else if (state.startTime != null) {
                val endTimeForCalc = state.endTime ?: currentTimeForDuration
                val diff = endTimeForCalc - state.startTime
                if (diff >= 0) formatDuration(diff) else "--:--:--"
            } else "--:--:--"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = actions.onStartPressed,
            enabled = !state.isEditMode && state.startTime == null && !state.isLoading
        ) {
            Text("Start")
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Start: ${state.startTime?.let { timeFormatter.format(Date(it)) } ?: "--:--:--"}")
            Text("End:   ${state.endTime?.let { timeFormatter.format(Date(it)) } ?: (if(state.isEditMode && state.initialEndTimeForEdit != null) timeFormatter.format(Date(state.initialEndTimeForEdit)) else "--:--:--")}")
            Text("Duration: $durationString")
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}

// Helper function to format duration, can be moved to a common place if needed
private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}


@Composable
fun ComponentSelectionDialog(
    availableComponents: List<ComponentInfo>,
    selectedComponentIds: Set<Long>,
    onComponentSelected: (componentId: Long, isSelected: Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp) 
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Select Components", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                if (availableComponents.isEmpty()) {
                    Text("No components available. Add them via Preferences -> Manage Components.")
                } else {
                    LazyColumn {
                        items(availableComponents, key = { it.id }) { component ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedComponentIds.contains(component.id),
                                        onClick = {
                                            onComponentSelected(component.id, !selectedComponentIds.contains(component.id))
                                        },
                                        role = Role.Checkbox
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedComponentIds.contains(component.id),
                                    onCheckedChange = null 
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(component.componentName)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun RadioButtonWithText(text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
                enabled = enabled
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null, 
            enabled = enabled
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 4.dp),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

// Previews need to be updated to reflect WorkDetailsActions and new state fields for edit mode
@Preview(showBackground = true, name = "Initial State - Edit Mode Off")
@Composable
fun WorkDetailsScreenInitialPreview() {
    WorkTrackerTheme {
        WorkDetailsScreen(
            state = WorkDetailsState(
                categoryName = "Initial Setup",
                isEditMode = false,
                logDate = System.currentTimeMillis(),
                availableComponents = listOf(
                    ComponentInfo(id = 1, componentName = "Comp A", customer = "Cust1", priorityLevel = 1, cycleTimeMinutes = 10.0, notesForAi = null),
                    ComponentInfo(id = 2, componentName = "Comp B", customer = "Cust2", priorityLevel = 2, cycleTimeMinutes = 20.0, notesForAi = null)
                )
            ),
            actions = WorkDetailsActions() 
        )
    }
}

@Preview(showBackground = true, name = "Started - Edit Mode Off")
@Composable
fun WorkDetailsScreenStartedPreview() {
    WorkTrackerTheme {
        WorkDetailsScreen(
            state = WorkDetailsState(
                categoryName = "Assembly",
                isEditMode = false,
                currentLogId = 1L, 
                startTime = System.currentTimeMillis() - 100000L, 
                description = "Assembling product X.",
                operatorId = "101",
                expenses = "50.0",
                taskSuccessful = true,
                assignedBy = AssignedByOptions.SELF,
                isEndButtonEnabled = true,
                logDate = System.currentTimeMillis() - 100000L
            ),
            actions = WorkDetailsActions()
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode - Log Loaded")
@Composable
fun WorkDetailsScreenEditModePreview() {
    WorkTrackerTheme {
        WorkDetailsScreen(
            state = WorkDetailsState(
                categoryName = "Maintenance Task",
                isEditMode = true,
                editingWorkLogId = 5L,
                currentLogId = 5L, // Should be same as editingWorkLogId
                startTime = System.currentTimeMillis() - 3600000L, // 1 hour ago
                description = "Scheduled server maintenance.",
                operatorId = "202",
                expenses = "120.50",
                taskSuccessful = true,
                assignedBy = AssignedByOptions.TOP_MGT,
                isEndButtonEnabled = true, // Assuming valid state for update
                logDate = System.currentTimeMillis() - 3600000L,
                initialEndTimeForEdit = System.currentTimeMillis() - 1800000L, // Ended 30 mins ago
                initialDurationForEdit = 1800000L // 30 mins duration
            ),
            actions = WorkDetailsActions()
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode - Ongoing Log Loaded")
@Composable
fun WorkDetailsScreenEditModeOngoingPreview() {
    WorkTrackerTheme {
        WorkDetailsScreen(
            state = WorkDetailsState(
                categoryName = "Troubleshooting",
                isEditMode = true,
                editingWorkLogId = 6L,
                currentLogId = 6L,
                startTime = System.currentTimeMillis() - 600000L, // 10 minutes ago
                description = "Investigating network issue.",
                operatorId = "303",
                isEndButtonEnabled = true, // Assuming other fields are filled making it valid
                logDate = System.currentTimeMillis() - 600000L,
                initialEndTimeForEdit = null, // Ongoing
                initialDurationForEdit = null // Ongoing
            ),
            actions = WorkDetailsActions()
        )
    }
}
