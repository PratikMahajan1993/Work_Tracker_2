package com.example.worktracker.ui.reports

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // Added import for Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button // Keep Button import for SelectLogForPdfDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Keep TextButton import for SelectLogForPdfDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.text.font.FontWeight // Not used in this file directly after removals
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
// import com.example.worktracker.data.database.entity.WorkActivityLog // Not directly used in this file after removals
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsHubScreen(
    mainScreenPadding: PaddingValues, 
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel.eventFlow) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReportsViewModel.PdfExportEvent.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(message = event.message) }
                }
                is ReportsViewModel.PdfExportEvent.NavigateToSmsApp -> {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:${event.phoneNumber}")
                            putExtra("sms_body", event.messageBody)
                        }
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No SMS app found to handle the request.", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open SMS app: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(mainScreenPadding), 
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reports Hub") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { reportsScaffoldPadding -> 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(reportsScaffoldPadding) 
                .verticalScroll(rememberScrollState())
                .padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionCard(
                title = "Send Activity Details (Text)",
                onClick = viewModel::onSendActivityDetailsViaSmsClicked
            )
            ActionCard(
                title = "Export Activity Log as PDF",
                onClick = viewModel::onSelectLogForPdfExportClicked
            )

            AnimatedVisibility(
                visible = uiState.generalError != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                uiState.generalError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (uiState.showSelectLogForPdfDialog) {
            SelectLogForPdfDialog(
                logs = uiState.recentLogs, 
                isLoading = uiState.isLoadingRecentLogs,
                onLogSelected = viewModel::onLogSelectedForPdfAndShowConfirmation, 
                onDismissRequest = viewModel::onDismissSelectLogForPdfDialog
            )
        }

        if (uiState.showPdfExportDialog && uiState.selectedLogForPdfExport != null) {
            ExportConfirmationDialog(
                logToExport = uiState.selectedLogForPdfExport!!,
                onConfirm = viewModel::onConfirmPdfExport,
                onDismiss = viewModel::onDismissPdfDialog
            )
        }

        if (uiState.showSelectLogForSmsDialog) {
            SelectLogForSmsDialog(
                logs = uiState.logToSmsCandidates,
                selectedLog = uiState.selectedLogForSms,
                smsContentPreview = uiState.smsContentPreview,
                isGeneratingSmsContent = uiState.isGeneratingSmsContent,
                onLogSelected = viewModel::onLogSelectedForSmsGeneration,
                onDismiss = viewModel::onDismissSelectLogForSmsDialog,
                onConfirm = viewModel::onConfirmAndSendSms
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(16.dp) 
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SelectLogForPdfDialog(
    logs: List<RecentLogDisplayInfo>,
    isLoading: Boolean,
    onLogSelected: (RecentLogDisplayInfo) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 500.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Select Log to Export as PDF", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) { // Ensured Box for centering
                        CircularProgressIndicator()
                    }
                } else if (logs.isEmpty()) {
                    Text("No recent logs available to export.", modifier = Modifier.padding(vertical = 20.dp))
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(logs, key = { it.id }) { logDisplayInfo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLogSelected(logDisplayInfo) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(logDisplayInfo.categoryName, style = MaterialTheme.typography.bodyLarge)
                                    Text("Started: ${logDisplayInfo.startTimeFormatted}", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(logDisplayInfo.durationFormatted, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("CANCEL")
                    }
                }
            }
        }
    }
}

// Removed duplicated ExportConfirmationDialog
// Removed duplicated SelectLogForSmsDialog
// Removed duplicated formatDurationForSms

