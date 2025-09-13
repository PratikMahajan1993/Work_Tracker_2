package com.example.worktracker.ui.screens.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun SetGeminiApiKeyDialog(
    showDialog: Boolean,
    apiKeyInput: String,
    onApiKeyInputChange: (String) -> Unit,
    apiKeyError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Set Gemini API Key") },
            text = {
                Column {
                    Text("Enter your Gemini API Key to enable AI features.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = onApiKeyInputChange,
                        label = { Text("Gemini API Key") },
                        isError = apiKeyError != null,
                        singleLine = true // API keys are typically single line
                    )
                    if (apiKeyError != null) {
                        Text(
                            text = apiKeyError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false) // Prevent accidental dismissal
        )
    }
}
