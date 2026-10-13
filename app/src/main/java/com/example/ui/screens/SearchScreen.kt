package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.AuraViewModel
import com.example.ui.components.TrackItemView
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSurfaceCard
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary

@Composable
fun SearchScreen(
    viewModel: AuraViewModel,
    searchResults: List<Track>,
    isSearching: Boolean,
    searchError: String?,
    currentTrack: Track?,
    modifier: Modifier = Modifier
) {
    var queryInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
            .padding(top = 16.dp)
            .testTag("search_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = queryInput,
            onValueChange = {
                queryInput = it
                viewModel.searchMusic(it)
            },
            placeholder = { Text("Search songs, artists, or audio streams...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AuraCyan
                )
            },
            trailingIcon = {
                if (queryInput.isNotEmpty()) {
                    IconButton(onClick = {
                        queryInput = ""
                        viewModel.searchMusic("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = AuraTextMuted
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuraCyan,
                unfocusedBorderColor = AuraSurfaceCard,
                focusedContainerColor = AuraSurfaceCard,
                unfocusedContainerColor = AuraSurfaceCard,
                focusedTextColor = AuraTextPrimary,
                unfocusedTextColor = AuraTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Results / Loading / Error
        when {
            isSearching -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(
                        color = AuraCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            searchError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = AuraError,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = searchError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraTextSecondary
                        )
                    }
                }
            }

            searchResults.isEmpty() && queryInput.isNotEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found for \"$queryInput\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AuraTextSecondary
                    )
                }
            }

            searchResults.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Discover Music on AURA",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AuraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Search online open music archives or local storage without Termux",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraTextMuted
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
                ) {
                    items(searchResults) { track ->
                        TrackItemView(
                            track = track,
                            isPlayingThis = currentTrack?.id == track.id,
                            isFavorite = viewModel.isTrackFavorite(track.id),
                            onTrackClick = { viewModel.playTrack(track) },
                            onFavoriteToggle = { viewModel.toggleFavorite(track) },
                            onDownloadClick = { viewModel.downloadTrack(track) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
