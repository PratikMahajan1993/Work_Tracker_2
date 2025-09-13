package com.example.worktracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.worktracker.data.database.entity.ActivityCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesDialog(
    showDialog: Boolean,
    categories: List<ActivityCategory>,
    onDismiss: () -> Unit,
    onAddNewCategoryClicked: () -> Unit,
    onEditCategoryClicked: (ActivityCategory) -> Unit,
    onDeleteCategoryClicked: (ActivityCategory) -> Unit
) {
    if (showDialog) {
        var showDeleteConfirmDialog by remember { mutableStateOf<ActivityCategory?>(null) }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Manage Activity Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (categories.isEmpty()) {
                        Text(
                            text = "No categories added yet. Add your first category!",
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories, key = { it.id }) { category ->
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(onClick = { onEditCategoryClicked(category) }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Category")
                                            }
                                            IconButton(onClick = { showDeleteConfirmDialog = category }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Category")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAddNewCategoryClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Category")
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Category")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }

        showDeleteConfirmDialog?.let { categoryToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete category '${categoryToDelete.name}'? This will also update associated work logs to '(Deleted Category)'.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteCategoryClicked(categoryToDelete)
                            showDeleteConfirmDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
