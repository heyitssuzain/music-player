package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuraBottomNavigation
import com.example.ui.components.AuraMiniPlayer
import com.example.ui.components.AuraTab
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.QueueScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceModal
import com.example.ui.theme.AuraDarkBackground

@Composable
fun AuraApp(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AuraTab.HOME) }
    var isQueueVisible by remember { mutableStateOf(false) }

    val hasCompletedLanding by viewModel.hasCompletedLanding.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val positionMs by viewModel.playbackPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()

    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val localTracks by viewModel.localTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val historyTracks by viewModel.historyTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()

    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val diagnostics by viewModel.diagnostics.collectAsStateWithLifecycle()
    val storageUsage by viewModel.storageUsage.collectAsStateWithLifecycle()

    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsStateWithLifecycle()
    val isVoiceModalVisible by viewModel.isVoiceModalVisible.collectAsStateWithLifecycle()

    if (!hasCompletedLanding) {
        LandingScreen(
            onEnterAura = { viewModel.completeLandingPage() },
            modifier = modifier
        )
        return
    }

    // Back handling for screens
    BackHandler(enabled = isQueueVisible || isFullPlayerVisible || selectedTab != AuraTab.HOME) {
        when {
            isQueueVisible -> isQueueVisible = false
            isFullPlayerVisible -> viewModel.isFullPlayerVisible.value = false
            selectedTab != AuraTab.HOME -> selectedTab = AuraTab.HOME
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuraDarkBackground)
            ) {
                // Persistent Mini Player above bottom bar
                if (currentTrack != null && !isFullPlayerVisible && !isQueueVisible) {
                    AuraMiniPlayer(
                        track = currentTrack!!,
                        isPlaying = isPlaying,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        onExpandClick = { viewModel.isFullPlayerVisible.value = true },
                        onPlayPauseClick = { viewModel.mediaController.togglePlayPause() },
                        onNextClick = { viewModel.mediaController.playNext() }
                    )
                }

                if (!isFullPlayerVisible && !isQueueVisible) {
                    AuraBottomNavigation(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        },
        containerColor = AuraDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Content
            when (selectedTab) {
                AuraTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    allTracks = allTracks,
                    favoriteTracks = favoriteTracks,
                    historyTracks = historyTracks,
                    currentTrack = currentTrack,
                    onNavigateTab = { selectedTab = it }
                )
                AuraTab.SEARCH -> SearchScreen(
                    viewModel = viewModel,
                    searchResults = searchResults,
                    isSearching = isSearching,
                    searchError = searchError,
                    currentTrack = currentTrack
                )
                AuraTab.LIBRARY -> LibraryScreen(
                    viewModel = viewModel,
                    localTracks = localTracks,
                    isScanning = isScanning,
                    currentTrack = currentTrack
                )
                AuraTab.PLAYLISTS -> PlaylistsScreen(
                    viewModel = viewModel,
                    playlists = playlists
                )
                AuraTab.DOWNLOADS -> DownloadsScreen(
                    viewModel = viewModel,
                    downloads = downloads,
                    storageUsage = storageUsage
                )
                AuraTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    diagnostics = diagnostics,
                    storageUsage = storageUsage
                )
            }

            // Full Player Screen (Overlaid)
            AnimatedVisibility(
                visible = isFullPlayerVisible && currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                currentTrack?.let { track ->
                    PlayerScreen(
                        track = track,
                        isPlaying = isPlaying,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        isShuffle = isShuffle,
                        repeatMode = repeatMode,
                        isFavorite = viewModel.isTrackFavorite(track.id),
                        onCollapseClick = { viewModel.isFullPlayerVisible.value = false },
                        onQueueClick = { isQueueVisible = true },
                        onPlayPauseClick = { viewModel.mediaController.togglePlayPause() },
                        onNextClick = { viewModel.mediaController.playNext() },
                        onPreviousClick = { viewModel.mediaController.playPrevious() },
                        onSeek = { viewModel.mediaController.seekTo(it) },
                        onToggleShuffle = { viewModel.mediaController.toggleShuffle() },
                        onToggleRepeat = { viewModel.mediaController.toggleRepeat() },
                        onToggleFavorite = { viewModel.toggleFavorite(track) },
                        onDownloadClick = { viewModel.downloadTrack(track) },
                        onSleepTimerClick = { selectedTab = AuraTab.SETTINGS; viewModel.isFullPlayerVisible.value = false }
                    )
                }
            }

            // Queue Screen (Overlaid)
            AnimatedVisibility(
                visible = isQueueVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                QueueScreen(
                    viewModel = viewModel,
                    queue = queue,
                    currentIndex = currentIndex,
                    onClose = { isQueueVisible = false }
                )
            }

            // Voice Control Modal
            if (isVoiceModalVisible) {
                VoiceModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.stopVoiceControl() }
                )
            }
        }
    }
}
