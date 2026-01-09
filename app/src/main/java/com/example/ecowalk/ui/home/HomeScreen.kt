package com.example.ecowalk.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecowalk.data.local.GreenWalkEntry
import com.example.ecowalk.data.local.User

@Composable
fun HomeScreen(
    user: User,
    walkHistory: List<GreenWalkEntry> = emptyList() // Added parameter
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Welcome Back,",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = user.email.substringBefore("@"), // Display name part of email
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "Your Dashboard",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth() // Align start correctly in Column
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    value = "%.1f".format(walkHistory.sumOf { it.totalDistanceKm }), // Calculate real total
                    unit = "km",
                    label = "Total Distance"
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Park,
                    value = if (walkHistory.isNotEmpty()) "%.0f".format(walkHistory.map { it.greenExposurePercentage }.average()) else "0", // Calculate real avg
                    unit = "%",
                    label = "Avg Green"
                )
            }

            Spacer(Modifier.height(12.dp))

            DashboardStatCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Timeline,
                value = walkHistory.size.toString(), // Real count
                unit = "Walks",
                label = "Total Walks Completed"
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(12.dp))
        }

        items(walkHistory.take(5)) { walk -> // Show top 5 recent
            HomeWalkHistoryCard(walk)
            Spacer(Modifier.height(8.dp))
        }
        
        if (walkHistory.isEmpty()) {
            item {
                Text(
                    "No walks recorded yet. Start a Green Walk today!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    unit: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HomeWalkHistoryCard(walk: GreenWalkEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                Text(
                    "${walk.startLocationName} → ${walk.endLocationName}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Text(
                    walk.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${"%.1f".format(walk.totalDistanceKm)} km",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${walk.greenExposurePercentage.toInt()}% green",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
