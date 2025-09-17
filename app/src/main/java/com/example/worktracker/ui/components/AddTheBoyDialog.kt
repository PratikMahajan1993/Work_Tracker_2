package com.example.worktracker.ui.components

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
import com.example.worktracker.ui.screens.preferences.PreferencesUiState

// Field constants for TheBoysInfo
const val FIELD_BOY_NAME = "boyName"
const val FIELD_BOY_ROLE = "boyRole"
const val FIELD_BOY_NOTES = "boyNotes"
const val FIELD_BOY_NOTES_AI = "boyNotesAi"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTheBoyDialog(
    showDialog: Boolean,
    uiState: PreferencesUiState,
    onInputChange: (fieldName: String, value: String) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!showDialog) return

    val currentStep = uiState.addTheBoyStep
    val fieldOrder = listOf(
        FIELD_BOY_NAME,
        FIELD_BOY_ROLE,
        FIELD_BOY_NOTES,
        FIELD_BOY_NOTES_AI
    )

    if (currentStep >= fieldOrder.size) {
        return 
    }

    val currentField = fieldOrder[currentStep]
    val currentValue = uiState.newTheBoyInputs[currentField] ?: ""
    val currentError = uiState.newTheBoyErrors[currentField]

    val (label, keyboardType, isOptional) = when (currentField) {
        FIELD_BOY_NAME -> Triple("Boy Name*", KeyboardType.Text, false)
        FIELD_BOY_ROLE -> Triple("Role*", KeyboardType.Text, false)
        FIELD_BOY_NOTES -> Triple("Notes (Optional)", KeyboardType.Text, true)
        FIELD_BOY_NOTES_AI -> Triple("Notes for AI (Optional)", KeyboardType.Text, true)
        else -> Triple("Unknown Field", KeyboardType.Text, true)
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(currentStep) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New 'Boy' - Step ${currentStep + 1}/${fieldOrder.size}") },
        text = {
            Column {
                Text("Enter ${label.removeSuffix("*").lowercase()}${if(isOptional) "" else " (required)"}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { onInputChange(currentField, it) },
                    label = { Text(label) },
                    singleLine = currentField != FIELD_BOY_NOTES && currentField != FIELD_BOY_NOTES_AI,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        capitalization = if (currentField == FIELD_BOY_NAME || currentField == FIELD_BOY_ROLE) KeyboardCapitalization.Words else KeyboardCapitalization.None
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
                        onSave()
                    } else {
                        onNextStep()
                    }
                }) {
                    Text(if (currentStep == fieldOrder.size - 1) "Save" else "Next")
                }
            }
        },
        dismissButton = {
            if (currentStep == 0) { 
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
