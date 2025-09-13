package com.example.worktracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.worktracker.data.database.entity.TheBoysInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheBoysListDialog(
    showDialog: Boolean,
    theBoys: List<TheBoysInfo>,
    onDismiss: () -> Unit,
    onAddNewBoyClicked: () -> Unit,
    onEditBoyClicked: (TheBoysInfo) -> Unit,
    onDeleteBoyClicked: (TheBoysInfo) -> Unit // Callback to show delete confirmation
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("'The Boys' Management") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onAddNewBoyClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Add New 'Boy'")
                }

                if (theBoys.isEmpty()) {
                    Text(
                        text = "No 'Boys' defined yet. Click 'Add New \'Boy\'' to get started!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(theBoys, key = { it.boyId }) { theBoy ->
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
                                        Text(theBoy.name, style = MaterialTheme.typography.titleMedium)
                                        Text("ID: ${theBoy.boyId}", style = MaterialTheme.typography.bodySmall)
                                        Text("Role: ${theBoy.role}", style = MaterialTheme.typography.bodySmall)
                                        // Add other relevant TheBoysInfo fields if necessary
                                    }
                                    Row {
                                        IconButton(onClick = { onEditBoyClicked(theBoy) }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit '${theBoy.name}'")
                                        }
                                        IconButton(onClick = { onDeleteBoyClicked(theBoy) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete '${theBoy.name}'", tint = MaterialTheme.colorScheme.error)
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
        modifier = Modifier.padding(vertical = 16.dp)
    )
}
