package com.example.worktracker.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.WorkActivityLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HistoryScreen(
    paddingValues: PaddingValues, 
    onNavigateToLogWork: () -> Unit,
    onNavigateToLogProduction: () -> Unit,
    onNavigateToEditWorkLog: (workLogId: Long, categoryName: String) -> Unit, // Updated signature
    onNavigateToEditProductionLog: (productionLogId: Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) 
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.historyItems.isEmpty()) { 
            EmptyHistoryContent(
                onLogActivityClick = onNavigateToLogWork,
                onLogProductionClick = onNavigateToLogProduction
            )
        } else {
            LogsList(
                items = uiState.historyItems,
                onDeleteWorkLogClick = viewModel::onDeleteWorkLogClicked,
                onDeleteProductionLogClick = viewModel::onDeleteProductionLogClicked,
                onEditWorkLogClick = { workLog -> 
                    onNavigateToEditWorkLog(workLog.id, workLog.categoryName ?: "Unknown Category") 
                },
                onEditProductionLogClick = { productionLog -> onNavigateToEditProductionLog(productionLog.id) }
            )
        }

        DeleteConfirmationDialog(
            showDialog = uiState.showDeleteConfirmationDialog,
            itemName = uiState.deleteDialogItemName,
            onConfirm = viewModel::onConfirmDelete,
            onDismiss = viewModel::onDismissDeleteDialog
        )
    }
}

@Composable
fun LogsList(
    items: List<HistoryListItem>,
    onDeleteWorkLogClick: (WorkActivityLog) -> Unit,
    onDeleteProductionLogClick: (ProductionActivity) -> Unit,
    onEditWorkLogClick: (WorkActivityLog) -> Unit, 
    onEditProductionLogClick: (ProductionActivity) -> Unit
) { 
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp) 
    ) {
        items(items) { item ->
            when (item) {
                is HistoryListItem.WorkHistoryItem -> {
                    WorkLogItem(
                        log = item.workLog,
                        onDeleteClick = { onDeleteWorkLogClick(item.workLog) },
                        onEditClick = { onEditWorkLogClick(item.workLog) } 
                    )
                }
                is HistoryListItem.ProductionHistoryItem -> {
                    ProductionLogItem(
                        item = item,
                        onDeleteClick = { onDeleteProductionLogClick(item.productionLog) },
                        onEditClick = { onEditProductionLogClick(item.productionLog) } 
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EmptyHistoryContent(
    onLogActivityClick: () -> Unit,
    onLogProductionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ListAlt, 
            contentDescription = "No Logs Icon",
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No activities yet.", 
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start tracking your activities to see your history here.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                onLogActivityClick()
            }) {
                Text("Log Operator Activity")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                onLogProductionClick()
            }) {
                Text("Log Production")
            }
        }
    }
}

@Composable
fun WorkLogItem(
    log: WorkActivityLog,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operator Activity: ${log.categoryName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (isExpanded) {
                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Work Log",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Work Log",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    log.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Column(modifier = Modifier.fillMaxWidth()) { 
                        Text(
                            text = "Start: ${formatTimestamp(log.startTime)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        log.endTime?.let {
                            Text(
                                text = "End: ${formatTimestamp(it)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    log.duration?.let {
                         Text(
                            text = "Duration: ${formatDuration(it)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionLogItem(
    item: HistoryListItem.ProductionHistoryItem,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val log = item.productionLog
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) 
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Production: ${log.componentName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                if (isExpanded) {
                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Production Log",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Production Log",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The Boy: ${item.boyName}", 
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Machine No: ${log.machineNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quantity: ${log.productionQuantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    log.rejectionQuantity?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rejection Quantity: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column( 
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start 
                    ) {
                        Text(
                            text = "Start: ${formatTimestamp(log.startTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp)) 
                        Text(
                            text = "End: ${formatTimestamp(log.endTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp)) 
                    Text(
                        text = "Duration: ${formatDuration(log.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this item: \"$itemName\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) 
    return sdf.format(Date(timestamp))
}

private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
