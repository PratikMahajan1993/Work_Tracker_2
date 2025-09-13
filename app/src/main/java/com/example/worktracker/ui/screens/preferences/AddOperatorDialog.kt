package com.example.worktracker.ui.screens.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOperatorDialog(
    showDialog: Boolean,
    uiState: PreferencesUiState,
    onInputChange: (fieldName: String, value: String) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit // Explicit save for the last step if preferred over implicit save via onNextStep
) {
    if (!showDialog) return

    val currentStep = uiState.addOperatorStep
    val fieldOrder = listOf(
        FIELD_OPERATOR_ID,
        FIELD_OPERATOR_NAME,
        FIELD_HOURLY_SALARY,
        FIELD_OPERATOR_ROLE,
        FIELD_OPERATOR_PRIORITY,
        FIELD_OPERATOR_NOTES,
        FIELD_OPERATOR_NOTES_AI
    )

    if (currentStep >= fieldOrder.size) {
        // This case should ideally not be reached if onNextStep calls onSave on the last step.
        // Or, it's a dedicated confirmation/summary step if we add one.
        return
    }

    val currentField = fieldOrder[currentStep]
    val currentValue = uiState.newOperatorInputs[currentField] ?: ""
    val currentError = uiState.newOperatorErrors[currentField]

    val (label, keyboardType, isOptional) = when (currentField) {
        FIELD_OPERATOR_ID -> Triple("Operator ID*", KeyboardType.Number, false)
        FIELD_OPERATOR_NAME -> Triple("Operator Name*", KeyboardType.Text, false)
        FIELD_HOURLY_SALARY -> Triple("Hourly Salary*", KeyboardType.Decimal, false)
        FIELD_OPERATOR_ROLE -> Triple("Role*", KeyboardType.Text, false)
        FIELD_OPERATOR_PRIORITY -> Triple("Priority (1-5)*", KeyboardType.Number, false)
        FIELD_OPERATOR_NOTES -> Triple("Notes (Optional)", KeyboardType.Text, true)
        FIELD_OPERATOR_NOTES_AI -> Triple("Notes for AI (Optional)", KeyboardType.Text, true)
        else -> Triple("Unknown Field", KeyboardType.Text, true)
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(currentStep) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingOperator == null) "Add New Operator - Step ${currentStep + 1}/${fieldOrder.size}" else "Edit Operator") },
        text = {
            Column {
                Text("Enter ${label.removeSuffix("*").lowercase()}${if(isOptional) "" else " (required)"}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { onInputChange(currentField, it) },
                    label = { Text(label) },
                    singleLine = currentField != FIELD_OPERATOR_NOTES && currentField != FIELD_OPERATOR_NOTES_AI,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        capitalization = if (currentField == FIELD_OPERATOR_NAME || currentField == FIELD_OPERATOR_ROLE) KeyboardCapitalization.Words else KeyboardCapitalization.None
                    ),
                    isError = currentError != null,
                    supportingText = { currentError?.let { Text(it) } }
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = onPreviousStep) {
                        Text("Previous")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Button(onClick = {
                    if (currentStep == fieldOrder.size - 1) {
                        onSave() // Call explicit save on the last step
                    } else {
                        onNextStep()
                    }
                }) {
                    Text(if (currentStep == fieldOrder.size - 1) "Save" else "Next")
                }
            }
        },
        dismissButton = {
            if (currentStep == 0) { // Only show Cancel on the first step, otherwise it's handled by Previous/Dismiss
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// Placeholder for editingOperator, remove if not used
// This dialog should be for ADDING only. If Edit is needed, it should be a separate dialog.
// For now, assume this is AddOperatorDialog, and editingOperator is not a concept here.
private val editingOperator: Any? = null