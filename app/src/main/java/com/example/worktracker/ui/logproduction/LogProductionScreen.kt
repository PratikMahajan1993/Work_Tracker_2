package com.example.worktracker.ui.logproduction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.TheBoysInfo
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogProductionScreen(
    navController: NavController,
    viewModel: LogProductionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiState.collectLatest { state ->
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearSnackbarMessage()
            }
        }
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            navController.popBackStack()
            viewModel.onNavigatedBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Production Log" else "Log Production Activity") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isEditMode) {
                EditModeContent(uiState = uiState, viewModel = viewModel)
            } else {
                QuestionnaireModeContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EditModeContent(uiState: LogProductionUiState, viewModel: LogProductionViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TheBoySelector(
            uiState = uiState,
            onBoySelected = { viewModel.onBoySelected(it) }
        )
        ComponentSelector(
            uiState = uiState,
            onComponentSelected = { viewModel.onComponentSelected(it) }
        )
        OutlinedTextField(value = uiState.machineNumber, onValueChange = viewModel::onMachineNumberChange, label = { Text("Machine Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = uiState.machineNumberError != null, singleLine = true)
        uiState.machineNumberError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        OutlinedTextField(value = uiState.productionQuantity, onValueChange = viewModel::onProductionQuantityChange, label = { Text("Production Quantity") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = uiState.productionQuantityError != null, singleLine = true)
        uiState.productionQuantityError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        OutlinedTextField(value = uiState.rejectionQuantity, onValueChange = viewModel::onRejectionQuantityChange, label = { Text("Rejection Quantity (Optional)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = uiState.rejectionQuantityError != null, singleLine = true)
        uiState.rejectionQuantityError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        ProductionTimeDetailsSection(uiState, viewModel)

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = viewModel::onSaveOrUpdatePressed,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoadingBoys && !uiState.isLoadingComponents && !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Update Activity")
            }
        }
    }
}

@Composable
fun QuestionnaireModeContent(uiState: LogProductionUiState, viewModel: LogProductionViewModel) {
    val currentStep = uiState.addProductionLogStep
    val fieldName = viewModel.addProductionLogFieldOrder[currentStep]
    val error = uiState.newProductionLogErrors[fieldName]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Step ${currentStep + 1} of ${viewModel.addProductionLogFieldOrder.size}", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            when (fieldName) {
                FIELD_BOY -> QuestionnaireBoySelector(uiState, viewModel, error)
                FIELD_COMPONENT -> QuestionnaireComponentSelector(uiState, viewModel, error)
                FIELD_MACHINE_NUMBER -> QuestionnaireTextField(uiState, viewModel, FIELD_MACHINE_NUMBER, "Machine Number", KeyboardType.Number, error)
                FIELD_PROD_QTY -> QuestionnaireTextField(uiState, viewModel, FIELD_PROD_QTY, "Production Quantity", KeyboardType.Number, error)
                FIELD_REJECT_QTY -> QuestionnaireTextField(uiState, viewModel, FIELD_REJECT_QTY, "Rejection Quantity (Optional)", KeyboardType.Number, error)
                FIELD_START_TIME -> QuestionnaireTextField(uiState, viewModel, FIELD_START_TIME, "Start Time (HH:mm)", KeyboardType.Ascii, error)
                FIELD_DOWNTIME -> QuestionnaireTextField(uiState, viewModel, FIELD_DOWNTIME, "Downtime (minutes, Optional)", KeyboardType.Number, error)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        StepNavigationControls(uiState, viewModel)
    }
}

@Composable
fun StepNavigationControls(uiState: LogProductionUiState, viewModel: LogProductionViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.addProductionLogStep > 0) {
                Button(onClick = viewModel::onPreviousStep) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Button(onClick = viewModel::onNextStep) {
                Text(if (uiState.addProductionLogStep == viewModel.addProductionLogFieldOrder.size - 1) "Finish" else "Next")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = viewModel::resetFormAndTimer,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Reset All Fields")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireBoySelector(uiState: LogProductionUiState, viewModel: LogProductionViewModel, error: String?) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBoyId = uiState.newProductionLogInputs[FIELD_BOY]
    val selectedBoyName = uiState.theBoysList.find { it.boyId.toString() == selectedBoyId }?.name ?: "Select a 'Boy'"

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !it }) {
            Box(modifier = Modifier.clickable { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedBoyName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select 'Boy'") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    isError = error != null,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.theBoysList.forEach { boy ->
                    DropdownMenuItem(
                        text = { Text(boy.name) },
                        onClick = {
                            viewModel.onNewProductionLogInputChange(FIELD_BOY, boy.boyId.toString())
                            expanded = false
                        }
                    )
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireComponentSelector(uiState: LogProductionUiState, viewModel: LogProductionViewModel, error: String?) {
    var expanded by remember { mutableStateOf(false) }
    val selectedComponentId = uiState.newProductionLogInputs[FIELD_COMPONENT]
    val selectedComponentName = uiState.componentList.find { it.id.toString() == selectedComponentId }?.componentName ?: "Select a Component"

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !it }) {
            Box(modifier = Modifier.clickable { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedComponentName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Component") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    isError = error != null,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.componentList.forEach { component ->
                    DropdownMenuItem(
                        text = { Text(component.componentName) },
                        onClick = {
                            viewModel.onNewProductionLogInputChange(FIELD_COMPONENT, component.id.toString())
                            expanded = false
                        }
                    )
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
    }
}

@Composable
fun QuestionnaireTextField(uiState: LogProductionUiState, viewModel: LogProductionViewModel, fieldName: String, label: String, keyboardType: KeyboardType, error: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = uiState.newProductionLogInputs[fieldName] ?: "",
            onValueChange = { viewModel.onNewProductionLogInputChange(fieldName, it) },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = error != null,
            singleLine = true
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp)) }
    }
}

// Unchanged original composables used by Edit Mode
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheBoySelector(uiState: LogProductionUiState, onBoySelected: (TheBoysInfo?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = uiState.selectedBoy?.name ?: "Select a 'Boy'", onValueChange = {}, readOnly = true, label = { Text("Select 'Boy'") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), isError = uiState.selectedBoyError != null)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            uiState.theBoysList.forEach { boy -> DropdownMenuItem(text = { Text(boy.name) }, onClick = { onBoySelected(boy); expanded = false }) }
        }
    }
    uiState.selectedBoyError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start=16.dp)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentSelector(uiState: LogProductionUiState, onComponentSelected: (ComponentInfo?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = uiState.selectedComponent?.componentName ?: "Select a Component", onValueChange = {}, readOnly = true, label = { Text("Select Component") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), isError = uiState.selectedComponentError != null)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            uiState.componentList.forEach { component -> DropdownMenuItem(text = { Text(component.componentName) }, onClick = { onComponentSelected(component); expanded = false }) }
        }
    }
    uiState.selectedComponentError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start=16.dp)) }
}

@Composable
fun ProductionTimeDetailsSection(uiState: LogProductionUiState, viewModel: LogProductionViewModel) {
    val dateTimeDisplayFormat = remember { SimpleDateFormat("HH:mm:ss dd/MM/yy", Locale.getDefault()) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = uiState.startTimeInput, onValueChange = viewModel::onStartTimeInputChange, label = { Text("Start Time (HH:mm)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), isError = uiState.startTimeError != null, singleLine = true)
        uiState.startTimeError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = uiState.downtimeInput, onValueChange = viewModel::onDowntimeInputChange, label = { Text("Downtime (minutes)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = uiState.downtimeError != null, singleLine = true)
        uiState.downtimeError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Parsed Start Time (for save): ${uiState.startTimeMillis?.let { dateTimeDisplayFormat.format(Date(it)) } ?: "--:--"}", style = MaterialTheme.typography.bodyMedium)
        if (uiState.isEditMode) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Original Start: ${uiState.initialStartTimeForEdit?.let { dateTimeDisplayFormat.format(Date(it)) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Original End: ${uiState.initialEndTimeForEdit?.let { dateTimeDisplayFormat.format(Date(it)) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Original Duration: ${uiState.initialDurationForEdit?.let { formatDuration(it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun formatDuration(millis: Long): String {
    if (millis <= 0) return "00:00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
