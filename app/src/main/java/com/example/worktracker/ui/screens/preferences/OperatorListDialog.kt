package com.example.worktracker.ui.screens.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.worktracker.data.database.entity.OperatorInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorListDialog(
    showDialog: Boolean,
    operators: List<OperatorInfo>,
    onDismiss: () -> Unit,
    onAddNewOperatorClicked: () -> Unit,
    onEditOperatorClicked: (OperatorInfo) -> Unit,
    onDeleteOperatorClicked: (OperatorInfo) -> Unit // Callback to show delete confirmation
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Operator Management") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onAddNewOperatorClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Add New Operator")
                }

                if (operators.isEmpty()) {
                    Text(
                        text = "No operators defined yet. Click 'Add New Operator' to get started!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(operators, key = { it.operatorId }) { operator ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(operator.name, style = MaterialTheme.typography.titleMedium)
                                        Text("ID: ${operator.operatorId}", style = MaterialTheme.typography.bodySmall)
                                        Text("Role: ${operator.role}", style = MaterialTheme.typography.bodySmall)
                                        operator.priority.let {
                                            Text("Priority: $it", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = { onEditOperatorClicked(operator) }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit Operator ${operator.name}")
                                        }
                                        IconButton(onClick = { onDeleteOperatorClicked(operator) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete Operator ${operator.name}", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        modifier = Modifier.padding(vertical = 16.dp) // Add some padding around the dialog itself
    )
}
