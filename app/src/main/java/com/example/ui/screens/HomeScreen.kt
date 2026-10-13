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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.AuraViewModel
import com.example.ui.components.AuraTab
import com.example.ui.components.TrackItemView
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraSurface
import com.example.ui.theme.AuraSurfaceCard
import com.example.ui.theme.AuraSurfaceElevated
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraViolet
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuraViewModel,
    allTracks: List<Track>,
    favoriteTracks: List<Track>,
    historyTracks: List<Track>,
    currentTrack: Track?,
    onNavigateTab: (AuraTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Header & Greeting
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AuraSurfaceElevated,
                                AuraSurfaceCard,
                                AuraDarkBackground
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.labelSmall,
                                color = AuraCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AURA",
                                style = MaterialTheme.typography.displayMedium,
                                color = AuraTextPrimary
                            )
                            Text(
                                text = "Music, beautifully simple.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AuraTextSecondary
                            )
                        }

                        // Voice Trigger Button
                        IconButton(
                            onClick = { viewModel.startVoiceControl() },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AuraCyan, AuraElectricBlue)
                                    )
                                )
                                .testTag("home_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Control",
                                tint = AuraDarkBackground,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shortcut Search Bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateTab(AuraTab.SEARCH) }
                            .testTag("home_search_bar"),
                        colors = CardDefaults.cardColors(containerColor = AuraSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AuraTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search tracks, artists, or open streams...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AuraTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Quick Category Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { onNavigateTab(AuraTab.LIBRARY) },
                    label = { Text("Local Music") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = AuraSurfaceCard,
                        labelColor = AuraTextPrimary
                    )
                )
                FilterChip(
                    selected = false,
                    onClick = { onNavigateTab(AuraTab.PLAYLISTS) },
                    label = { Text("Playlists") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = AuraSurfaceCard,
                        labelColor = AuraTextPrimary
                    )
                )
                FilterChip(
                    selected = false,
                    onClick = { onNavigateTab(AuraTab.DOWNLOADS) },
                    label = { Text("Offline") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = AuraSurfaceCard,
                        labelColor = AuraTextPrimary
                    )
                )
            }
        }

        // Favorites Horizontal Shelf
        if (favoriteTracks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AuraTextPrimary
                    )
                    Text(
                        text = "${favoriteTracks.size} songs",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraCyan
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteTracks) { track ->
                        FavoriteTrackCard(
                            track = track,
                            isPlaying = currentTrack?.id == track.id,
                            onClick = { viewModel.playTrack(track) }
                        )
                    }
                }
            }
        }

        // Recently Played
        if (historyTracks.isNotEmpty()) {
            item {
                Text(
                    text = "Recently Played",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AuraTextPrimary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(historyTracks.take(4)) { track ->
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

        // All Tracks in Library
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Songs",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AuraTextPrimary
                )
                IconButton(
                    onClick = { viewModel.rescanLibrary() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan library",
                        tint = AuraCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (allTracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AuraTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No songs found in library",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AuraTextSecondary
                        )
                        Text(
                            text = "Tap refresh to scan local device storage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraTextMuted
                        )
                    }
                }
            }
        } else {
            items(allTracks) { track ->
                TrackItemView(
                    track = track,
                    isPlayingThis = currentTrack?.id == track.id,
                    isFavorite = viewModel.isTrackFavorite(track.id),
                    onTrackClick = { viewModel.playTrack(track) },
                    onFavoriteToggle = { viewModel.toggleFavorite(track) },
                    onDownloadClick = if (!track.isLocal) { { viewModel.downloadTrack(track) } } else null,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FavoriteTrackCard(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
            .testTag("fav_card_${track.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = AuraSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (track.artworkUri != null) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AuraCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color(0x77000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = AuraCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 13.sp),
                color = AuraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                color = AuraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
