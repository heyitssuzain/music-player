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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
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
fun DownloadsScreen(
    viewModel: AuraViewModel,
    downloads: List<DownloadEntity>,
    storageUsage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
            .testTag("downloads_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Offline Music",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                color = AuraTextPrimary
            )
            Text(
                text = "Listen anywhere without mobile data or internet",
                style = MaterialTheme.typography.bodyMedium,
                color = AuraTextSecondary
            )
        }

        // Storage Usage Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AuraSurfaceElevated)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AuraCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = AuraCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Storage Consumed: $storageUsage",
                        style = MaterialTheme.typography.titleLarge,
                        color = AuraTextPrimary
                    )
                    Text(
                        text = "${downloads.count { it.status == "COMPLETED" }} tracks downloaded offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = AuraTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No downloaded tracks",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AuraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Download songs from online search to listen offline anytime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
            ) {
                items(downloads) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("download_record_${record.trackId}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AuraSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = AuraTextPrimary
                                    )
                                    Text(
                                        text = "${record.artist} • ${record.status}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when (record.status) {
                                            "COMPLETED" -> AuraSuccess
                                            "DOWNLOADING" -> AuraCyan
                                            else -> AuraError
                                        }
                                    )
                                }

                                if (record.status == "DOWNLOADING") {
                                    IconButton(onClick = { viewModel.cancelDownload(record.trackId) }) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Cancel Download",
                                            tint = AuraError
                                        )
                                    }
                                } else if (record.status == "COMPLETED") {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Downloaded",
                                        tint = AuraSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            if (record.status == "DOWNLOADING") {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { record.progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = AuraCyan,
                                    trackColor = AuraCyan.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
