package com.example.worktracker.ui.logwork

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme

// Removed hardcoded activityCategories list

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkActivityScreen(
    onCategorySelected: (String) -> Unit,
    viewModel: LogWorkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Work Activity Category") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center // Center loading indicator
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.categories.isEmpty()) {
                Text(
                    text = "No activity categories available.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(), // Column takes full size if not loading
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // verticalArrangement = Arrangement.spacedBy(8.dp) // Use LazyColumn for spacing
                ) {
                    Text(
                        text = "Select an Activity Category:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(uiState.categories, key = { it.name }) { categoryInfo ->
                            Button(
                                onClick = { onCategorySelected(categoryInfo.name) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (categoryInfo.isOngoing) Color.Green else MaterialTheme.colorScheme.primary,
                                    contentColor = if (categoryInfo.isOngoing) Color.Black else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(text = categoryInfo.name)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "LogWorkActivityScreen - Loading")
@Composable
fun LogWorkActivityScreenLoadingPreview() {
    WorkTrackerTheme {
        // To preview loading, we need a way to inject a ViewModel in a loading state
        // For simplicity, we can't directly show the Hilt ViewModel loading state here easily.
        // This preview will likely show the empty or initial state of a dummy ViewModel if Hilt doesn't fully operate in previews.
        // A more robust preview would mock the LogWorkUiState directly if the screen accepted it as a parameter.
        LogWorkActivityScreen(onCategorySelected = { })
    }
}

@Preview(showBackground = true, name = "LogWorkActivityScreen - With Data")
@Composable
fun LogWorkActivityScreenWithDataPreview() {
    // This preview will also likely use a default/empty ViewModel state.
    // To properly preview data, you'd typically pass a LogWorkUiState directly to a modified Composable signature
    // or use Hilt's preview annotations if applicable.
    WorkTrackerTheme {
        LogWorkActivityScreen(onCategorySelected = { cat -> println("Selected: $cat") })
        // For a more representative preview, you might need a fake ViewModel
        // or pass state directly to the composable for preview purposes.
    }
}
