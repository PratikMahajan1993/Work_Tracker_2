package com.example.worktracker.ui.screens.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.WorkActivityLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    onNavigateToLogWork: () -> Unit,
    onNavigateToLogProduction: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply scaffold padding here
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), // fill parent Box
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.ongoingActivities.isEmpty() && uiState.recentLogs.isEmpty()) {
            EmptyDashboardContent(
                onLogActivityClick = onNavigateToLogWork,
                onLogProductionClick = onNavigateToLogProduction
            )
        } else {
            DashboardContent(
                ongoingActivities = uiState.ongoingActivities,
                recentActivityItems = uiState.recentLogs, // Changed to recentActivityItems
                onLogActivityClick = onNavigateToLogWork,
                onLogProductionClick = onNavigateToLogProduction
            )
        }
    }
}

@Composable
fun EmptyDashboardContent(
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
            imageVector = Icons.Outlined.SpaceDashboard,
            contentDescription = "Dashboard Icon",
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome to Work Tracker!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Get started by logging your work activities. Your dashboard will show summaries and insights once you have some data.",
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
fun DashboardContent(
    ongoingActivities: List<WorkActivityLog>,
    recentActivityItems: List<RecentActivityItem>, // Changed parameter name and type
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Spacer(modifier = Modifier.height(24.dp))

        if (ongoingActivities.isNotEmpty()) {
            Text(
                "Ongoing Activities",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ongoingActivities.forEach { log ->
                WorkLogDashboardItem(log = log) // Ongoing activities remain non-collapsible for now
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (recentActivityItems.isNotEmpty()) {
            Text(
                "Recent Work",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            recentActivityItems.forEach { item ->
                when (item) {
                    is RecentActivityItem.RecentWorkLogItem -> {
                        WorkLogDashboardItem(log = item.workLog, isCollapsible = true)
                    }
                    is RecentActivityItem.RecentProductionLogItem -> {
                        ProductionLogDashboardItem(logItem = item, isCollapsible = true)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun WorkLogDashboardItem(log: WorkActivityLog, isCollapsible: Boolean = false) {
    var isExpanded by remember { mutableStateOf(!isCollapsible) } // if not collapsible, it's expanded by default

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isCollapsible) Modifier.clickable { isExpanded = !isExpanded } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Operator Activity: ${log.categoryName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    log.description?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Start: ${formatDashboardTimestamp(log.startTime)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        log.endTime?.let {
                            Text(
                                text = "End: ${formatDashboardTimestamp(it)}",
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
fun ProductionLogDashboardItem(logItem: RecentActivityItem.RecentProductionLogItem, isCollapsible: Boolean = false) {
    var isExpanded by remember { mutableStateOf(!isCollapsible) }
    val log = logItem.productionLog
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isCollapsible) Modifier.clickable { isExpanded = !isExpanded } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Production: ${log.componentName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The Boy: ${logItem.operatorName ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Machine: ${log.machineNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quantity: ${log.productionQuantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Start: ${formatDashboardTimestamp(log.startTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "End: ${formatDashboardTimestamp(log.endTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
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

private fun formatDashboardTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0) return "--:--:--"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
