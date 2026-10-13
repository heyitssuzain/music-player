package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.AuraViewModel
import com.example.ui.components.TrackItemView
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraSurface
import com.example.ui.theme.AuraSurfaceCard
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary

@Composable
fun LibraryScreen(
    viewModel: AuraViewModel,
    localTracks: List<Track>,
    isScanning: Boolean,
    currentTrack: Track?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Songs", "Artists", "Albums")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
            .testTag("library_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Local Library",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                    color = AuraTextPrimary
                )
                Text(
                    text = "${localTracks.size} local tracks on device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuraTextSecondary
                )
            }

            IconButton(
                onClick = { viewModel.rescanLibrary() },
                enabled = !isScanning,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AuraSurfaceCard)
                    .testTag("library_rescan_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        color = AuraCyan,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan Library",
                        tint = AuraCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AuraSurface,
            contentColor = AuraCyan,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AuraCyan,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) AuraCyan else AuraTextMuted
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Songs tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
                ) {
                    items(localTracks) { track ->
                        TrackItemView(
                            track = track,
                            isPlayingThis = currentTrack?.id == track.id,
                            isFavorite = viewModel.isTrackFavorite(track.id),
                            onTrackClick = { viewModel.playTrack(track) },
                            onFavoriteToggle = { viewModel.toggleFavorite(track) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            1 -> {
                // Artists tab
                val artists = remember(localTracks) {
                    localTracks.groupBy { it.artist }.toList()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
                ) {
                    items(artists) { (artistName, songs) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (songs.isNotEmpty()) viewModel.playTrack(songs.first())
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AuraSurfaceCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AuraCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = artistName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = AuraTextPrimary
                                )
                                Text(
                                    text = "${songs.size} songs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AuraTextSecondary
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Albums tab
                val albums = remember(localTracks) {
                    localTracks.groupBy { it.album }.toList()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
                ) {
                    items(albums) { (albumName, songs) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (songs.isNotEmpty()) viewModel.playTrack(songs.first())
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraSurfaceCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Album,
                                    contentDescription = null,
                                    tint = AuraCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = albumName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = AuraTextPrimary
                                )
                                Text(
                                    text = "${songs.size} tracks • ${songs.firstOrNull()?.artist ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AuraTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
