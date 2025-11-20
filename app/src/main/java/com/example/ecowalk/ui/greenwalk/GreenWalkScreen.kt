package com.example.ecowalk.ui.greenwalk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ecowalk.data.local.GreenWalkEntry

@Composable
fun GreenWalkScreen(
    viewModel: GreenWalkViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val walkHistory by viewModel.walkHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Green Walk",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        when (uiState) {
            is GreenWalkUiState.Input -> {
                InputForm(
                    onAnalyze = { start, end ->
                        viewModel.analyzeWalk(start, end)
                    }
                )
            }

            is GreenWalkUiState.Analyzing -> {
                AnalyzingView()
            }

            is GreenWalkUiState.Results -> {
                val walk = (uiState as GreenWalkUiState.Results).walk
                ResultsView(
                    walk = walk,
                    onSave = { steps -> viewModel.saveCurrentWalk(steps) },
                    onNewWalk = { viewModel.resetToInput() }
                )
            }

            is GreenWalkUiState.Error -> {
                val error = (uiState as GreenWalkUiState.Error).message
                ErrorView(
                    message = error,
                    onRetry = { viewModel.resetToInput() }
                )
            }
        }

        // Walk History
        if (walkHistory.isNotEmpty() && uiState is GreenWalkUiState.Input) {
            Spacer(Modifier.height(32.dp))
            Text(
                "Walk History",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(walkHistory) { walk ->
                    WalkHistoryCard(walk)
                }
            }
        }
    }
}

@Composable
fun InputForm(
    onAnalyze: (String, String) -> Unit
) {
    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Enter Walk Details",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "Enter location names (e.g., 'Central Park, New York' or 'Times Square')",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = startLocation,
                onValueChange = { startLocation = it },
                label = { Text("Start Location") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Central Park, New York") }
            )

            OutlinedTextField(
                value = endLocation,
                onValueChange = { endLocation = it },
                label = { Text("End Location") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Times Square, New York") }
            )

            Button(
                onClick = {
                    if (startLocation.isNotBlank() && endLocation.isNotBlank()) {
                        onAnalyze(startLocation, endLocation)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = startLocation.isNotBlank() && endLocation.isNotBlank()
            ) {
                Text("Analyze Walk")
            }
        }
    }
}

@Composable
fun AnalyzingView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(
            "Analyzing your route...",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Fetching route and checking green spaces",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ResultsView(
    walk: GreenWalkEntry,
    onSave: (Int) -> Unit,
    onNewWalk: () -> Unit
) {
    var userSteps by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Walk Complete! 🌳",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Distance: ${"%.2f".format(walk.totalDistanceKm)} km",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Green Exposure",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    "${walk.greenExposurePercentage.toInt()}%",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "${walk.greenExposurePercentage.toInt()}% of your route was in tree-rich/park zones",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Route Details",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("From: ${walk.startLocationName}")
                Text("To: ${walk.endLocationName}")
            }
        }

        OutlinedTextField(
            value = userSteps,
            onValueChange = { userSteps = it },
            label = { Text("Steps (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter step count if you have it") }
        )

        Button(
            onClick = {
                val steps = userSteps.toIntOrNull() ?: 0
                onSave(steps)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Walk")
        }

        OutlinedButton(
            onClick = onNewWalk,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze Another Walk")
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
fun WalkHistoryCard(walk: GreenWalkEntry) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${walk.startLocationName} → ${walk.endLocationName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        walk.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${walk.greenExposurePercentage.toInt()}% green",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${"%.2f".format(walk.totalDistanceKm)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
