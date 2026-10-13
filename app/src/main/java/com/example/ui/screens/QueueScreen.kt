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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.Track
import com.example.ui.AuraViewModel
import com.example.ui.components.TrackItemView
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraSurfaceElevated
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary

@Composable
fun QueueScreen(
    viewModel: AuraViewModel,
    queue: List<Track>,
    currentIndex: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
            .statusBarsPadding()
            .testTag("queue_screen")
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
                    text = "Play Queue",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                    color = AuraTextPrimary
                )
                Text(
                    text = "${queue.size} songs in sequence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuraTextSecondary
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_queue_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Queue",
                    tint = AuraTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Playback queue is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuraTextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 120.dp)
            ) {
                itemsIndexed(queue) { index, track ->
                    val isPlaying = index == currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPlaying) AuraSurfaceElevated else androidx.compose.ui.graphics.Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackItemView(
                            track = track,
                            isPlayingThis = isPlaying,
                            isFavorite = viewModel.isTrackFavorite(track.id),
                            onTrackClick = { viewModel.mediaController.setQueueAndPlay(queue, index) },
                            onFavoriteToggle = { viewModel.toggleFavorite(track) },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { viewModel.mediaController.removeFromQueue(index) },
                            modifier = Modifier.testTag("remove_from_queue_$index")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove from Queue",
                                tint = AuraTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
