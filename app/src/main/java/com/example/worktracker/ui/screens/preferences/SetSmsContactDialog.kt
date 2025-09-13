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
fun SetSmsContactDialog(
    showDialog: Boolean,
    smsContactInput: String,
    onSmsContactInputChange: (String) -> Unit,
    smsContactError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Set Preferred SMS Contact") },
            text = {
                Column {
                    Text("Enter the phone number to be used for sending reports via SMS.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = smsContactInput,
                        onValueChange = onSmsContactInputChange,
                        label = { Text("Phone Number") },
                        isError = smsContactError != null,
                        singleLine = true
                    )
                    if (smsContactError != null) {
                        Text(
                            text = smsContactError,
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
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }
}
