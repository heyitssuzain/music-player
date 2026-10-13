package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.provider.ProviderDiagnostics
import com.example.ui.AuraViewModel
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraSurfaceCard
import com.example.ui.theme.AuraSurfaceElevated
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary

@Composable
fun SettingsScreen(
    viewModel: AuraViewModel,
    diagnostics: List<ProviderDiagnostics>,
    storageUsage: String,
    modifier: Modifier = Modifier
) {
    val sleepTimer by viewModel.sleepTimerMinutes.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Settings & Diagnostics",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                color = AuraTextPrimary
            )
            Text(
                text = "AURA engine status, sleep timer, and privacy configuration",
                style = MaterialTheme.typography.bodyMedium,
                color = AuraTextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sleep Timer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AuraCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sleep Timer",
                            style = MaterialTheme.typography.titleLarge,
                            color = AuraTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (sleepTimer != null) "Active: Pausing in $sleepTimer minutes" else "Automatically pause playback when you fall asleep",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(null, 15, 30, 45, 60).forEach { mins ->
                            val isSelected = sleepTimer == mins
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSleepTimer(mins) },
                                label = { Text(if (mins == null) "Off" else "${mins}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AuraCyan,
                                    selectedLabelColor = AuraDarkBackground,
                                    containerColor = AuraSurfaceCard,
                                    labelColor = AuraTextPrimary
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Provider Diagnostics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Provider Status & Diagnostics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AuraTextPrimary
                )
                IconButton(onClick = { viewModel.refreshDiagnostics() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Diagnostics",
                        tint = AuraCyan
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(diagnostics) { diag ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AuraSurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (diag.isAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (diag.isAvailable) AuraSuccess else AuraError,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = diag.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp),
                            color = AuraTextPrimary
                        )
                        Text(
                            text = "${diag.note} • Latency: ${diag.latencyMs}ms",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = AuraTextSecondary
                        )
                    }
                }
            }
        }

        // Data & Cache Actions
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Library & Storage Maintenance",
                style = MaterialTheme.typography.headlineMedium,
                color = AuraTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Offline Storage Consumed: $storageUsage",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AuraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.rescanLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraCyan, contentColor = AuraDarkBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Rescan Music")
                        }
                        OutlinedButton(
                            onClick = { viewModel.clearHistory() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = AuraTextPrimary)
                            Spacer(Modifier.width(6.dp))
                            Text("Clear History", color = AuraTextPrimary)
                        }
                    }
                }
            }
        }

        // Landing & Welcome Experience
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome & Showcase",
                style = MaterialTheme.typography.headlineMedium,
                color = AuraTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AURA Cinematic Landing",
                            style = MaterialTheme.typography.titleLarge,
                            color = AuraTextPrimary
                        )
                        Text(
                            text = "View the anime protagonist welcome screen and feature overview",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraTextSecondary
                        )
                    }
                    Button(
                        onClick = { viewModel.revisitLandingPage() },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraCyan, contentColor = AuraDarkBackground)
                    ) {
                        Text("View")
                    }
                }
            }
        }

        // Privacy Guarantee
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraSurfaceCard)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AuraCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Privacy & Local-First Philosophy",
                            style = MaterialTheme.typography.titleLarge,
                            color = AuraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AURA operates with zero tracker SDKs, zero telemetry, and zero mandatory user accounts. All listening history and playlists remain 100% device-local in SQLite.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraTextSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AURA Android v1.0.0 • Media3 ExoPlayer Engine",
                style = MaterialTheme.typography.labelSmall,
                color = AuraTextMuted,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
