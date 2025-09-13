package com.example.worktracker.ui.viewlogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewLogsScreen(
    viewModel: ViewLogsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Activity Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.operatorIdFilter,
                onValueChange = viewModel::onFilterChanged,
                label = { Text("Filter by Operator ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs found matching your criteria.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LogsTable(logs = uiState.logs)
            }
        }
    }
}

@Composable
fun LogsTable(logs: List<LogDisplayInfo>) {
    Column {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "Sr.No.", weight = 0.15f, fontWeight = FontWeight.Bold)
            TableCell(text = "Date", weight = 0.25f, fontWeight = FontWeight.Bold)
            TableCell(text = "Activity", weight = 0.3f, fontWeight = FontWeight.Bold)
            TableCell(text = "Duration", weight = 0.2f, fontWeight = FontWeight.Bold)
            TableCell(text = "Op.ID", weight = 0.1f, fontWeight = FontWeight.Bold, alignment = TextAlign.Center)
        }
        HorizontalDivider()

        // Data Rows
        LazyColumn {
            items(logs, key = { it.srNo }) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(text = log.srNo, weight = 0.15f)
                    TableCell(text = log.date, weight = 0.25f)
                    TableCell(text = log.activityCategory, weight = 0.3f)
                    TableCell(text = log.duration, weight = 0.2f)
                    TableCell(text = log.operatorId, weight = 0.1f, alignment = TextAlign.Center)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    alignment: TextAlign = TextAlign.Start,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        textAlign = alignment,
        fontWeight = fontWeight,
        fontSize = 12.sp // Adjust font size for better fit
    )
}

@Preview(showBackground = true, name = "ViewLogsScreen - Empty")
@Composable
fun ViewLogsScreenEmptyPreview() {
    WorkTrackerTheme {
        // This preview will use a default ViewModel state, likely empty or loading.
        // For a more controlled preview, pass a ViewLogsUiState directly.
        ViewLogsScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "ViewLogsScreen - With Data")
@Composable
fun ViewLogsScreenWithDataPreview() {
    WorkTrackerTheme {
        // This preview will use a default ViewModel state.
        // Providing a mock ViewModel or passing state directly is better for complex previews.
        val sampleLogs = listOf(
            LogDisplayInfo("1", "01 Jan 2023", "Office Work", "02:30:00", "101"),
            LogDisplayInfo("2", "01 Jan 2023", "Meeting", "01:00:00", "102"),
            LogDisplayInfo("3", "02 Jan 2023", "Trip Outside", "05:15:45", "101")
        )
        // To effectively preview this, you'd need to mock the ViewModel or pass state directly.
        // For now, this preview won't show the sampleLogs unless the ViewModel is mocked.
        ViewLogsScreen(onNavigateBack = {})
        // A better preview would involve a fake ViewModel that returns this state:
        // ViewLogsScreen(viewModel = FakeViewLogsViewModel(sampleLogs), onNavigateBack = {})
    }
}
