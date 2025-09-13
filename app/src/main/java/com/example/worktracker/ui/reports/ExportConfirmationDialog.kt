package com.example.worktracker.ui.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.worktracker.data.database.entity.WorkActivityLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ExportConfirmationDialog(
    logToExport: WorkActivityLog,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    // Revised duration logic based on non-nullable startTime
    val duration: String = if (logToExport.endTime != null) {
        // startTime is non-nullable, so a direct subtraction is safe.
        val diff = logToExport.endTime - logToExport.startTime
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        "${hours}h ${minutes}m"
    } else {
        // If endTime is null, startTime (being non-nullable) means it's ongoing.
        "Ongoing"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export to PDF?") },
        text = {
            Column {
                Text("Do you want to export the following activity log to a PDF file?")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Category: ${logToExport.categoryName}", fontWeight = FontWeight.SemiBold)
                // Direct access for startTime as it's non-nullable in WorkActivityLog
                Text("Started: ${dateFormatter.format(Date(logToExport.startTime))}")
                Text("Duration: $duration")
                if (logToExport.description.isNotBlank()) {
                    Text("Description: ${logToExport.description}")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}
