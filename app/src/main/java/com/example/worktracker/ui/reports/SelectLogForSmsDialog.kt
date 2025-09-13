package com.example.worktracker.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.worktracker.data.database.entity.WorkActivityLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SelectLogForSmsDialog(
    logs: List<WorkActivityLog>,
    selectedLog: WorkActivityLog?,
    smsContentPreview: String?,
    isGeneratingSmsContent: Boolean,
    onLogSelected: (WorkActivityLog) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dialogTimeFormatter = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Log for SMS") },
        text = {
            Column {
                Text("Choose a log to generate an SMS summary:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp) // Limit height of the list
                ) {
                    items(logs) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLogSelected(log) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = log.id == selectedLog?.id,
                                onClick = { onLogSelected(log) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${log.categoryName} - ${dialogTimeFormatter.format(Date(log.startTime))}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                val durationStr = if (log.endTime != null) {
                                    val diff = log.endTime - log.startTime
                                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                                    "${hours}h ${minutes}m"
                                } else {
                                    "Ongoing"
                                }
                                Text(
                                    text = "Duration: $durationStr",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("SMS Preview:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                if (isGeneratingSmsContent) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (smsContentPreview != null) {
                    Text(smsContentPreview, style = MaterialTheme.typography.bodyMedium)
                } else if (selectedLog != null) {
                    Text("Select a log to generate preview.", style = MaterialTheme.typography.bodyMedium) // This message may not appear if preview is generated immediately
                } else {
                    Text("No log selected.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedLog != null && smsContentPreview != null && !isGeneratingSmsContent
            ) {
                Text("Confirm & Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false) // Prevent dismissal on outside click
    )
}
