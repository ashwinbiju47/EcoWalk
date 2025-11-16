package com.example.ecowalk.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsScreen(viewModel: StatsViewModel) {

    val daily by viewModel.dailySteps.collectAsState()
    val weekly by viewModel.weeklySteps.collectAsState()
    val carbon by viewModel.carbonOffset.collectAsState()
    val history by viewModel.stepHistory.collectAsState()

    var manualSteps by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.refreshStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text("Stats Overview", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        SummaryCard("Daily Steps", daily.toString())
        SummaryCard("Weekly Steps", weekly.toString())
        SummaryCard("Carbon Offset", "${"%.2f".format(carbon)} g")

        Spacer(Modifier.height(24.dp))

        Text("Add Steps", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = manualSteps,
            onValueChange = { manualSteps = it },
            label = { Text("Manual steps") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                manualSteps.toIntOrNull()?.let { viewModel.addManualSteps(it) }
                manualSteps = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.refreshStats() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh Summary")
        }

        Spacer(Modifier.height(28.dp))

        Text("Entry History", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(history) { entry ->
                HistoryRow(entry.date, entry.steps)
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun HistoryRow(date: String, steps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(date, style = MaterialTheme.typography.bodyLarge)
        Text("$steps steps", style = MaterialTheme.typography.bodyLarge)
    }
}
