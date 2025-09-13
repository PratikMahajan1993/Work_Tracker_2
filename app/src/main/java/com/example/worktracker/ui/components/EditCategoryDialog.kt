package com.example.worktracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Import all from Material3
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.worktracker.data.database.entity.ActivityCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(
    showDialog: Boolean,
    editingCategory: ActivityCategory?,
    categoryNameInput: String,
    categoryNameError: String?,
    onCategoryNameInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog && editingCategory != null) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Edit Category: ${editingCategory.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = categoryNameInput,
                        onValueChange = onCategoryNameInputChange,
                        label = { Text("Category Name") },
                        isError = categoryNameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (categoryNameError != null) {
                        Text(
                            text = categoryNameError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onConfirm) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}