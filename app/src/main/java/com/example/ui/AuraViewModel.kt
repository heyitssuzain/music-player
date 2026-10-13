package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuraDatabase
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistEntryEntity
import com.example.data.local.TrackEntity
import com.example.data.model.Track
import com.example.data.provider.ProviderDiagnostics
import com.example.data.provider.ProviderRegistry
import com.example.data.provider.ProviderResult
import com.example.data.repository.LocalAudioScanner
import com.example.download.AuraDownloadManager
import com.example.media.AuraMediaController
import com.example.voice.AuraVoiceManager
import com.example.voice.VoiceIntent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AuraDatabase.getInstance(application)
    private val dao = db.auraDao()

    val mediaController = AuraMediaController(application, viewModelScope)
    private val scanner = LocalAudioScanner(application)
    val providerRegistry = ProviderRegistry(application)
    val downloadManager = AuraDownloadManager(application, dao, viewModelScope)
    val voiceManager = AuraVoiceManager(application)

    // Player states
    val currentTrack = mediaController.currentTrack
    val isPlaying = mediaController.isPlaying
    val playbackPositionMs = mediaController.playbackPositionMs
    val durationMs = mediaController.durationMs
    val isShuffleEnabled = mediaController.isShuffleEnabled
    val repeatMode = mediaController.repeatMode
    val queue = mediaController.queue
    val currentIndex = mediaController.currentIndex

    // UI state
    val isFullPlayerVisible = MutableStateFlow(false)
    val isVoiceModalVisible = MutableStateFlow(false)

    // Data streams
    val allTracks: StateFlow<List<Track>> = dao.getAllTracks()
        .map { entities -> entities.map { it.toDomainTrack() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localTracks: StateFlow<List<Track>> = dao.getLocalTracks()
        .map { entities -> entities.map { it.toDomainTrack() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = dao.getFavoriteTracks()
        .map { entities -> entities.map { it.toDomainTrack() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = dao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyTracks: StateFlow<List<Track>> = dao.getRecentHistory(50)
        .map { entities -> entities.map { it.toDomainTrack() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads = downloadManager.downloadsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    val searchQuery = MutableStateFlow("")
    val searchResults = MutableStateFlow<List<Track>>(emptyList())
    val isSearching = MutableStateFlow(false)
    val searchError = MutableStateFlow<String?>(null)

    // Diagnostics & Scanning
    val isScanning = MutableStateFlow(false)
    val diagnostics = MutableStateFlow<List<ProviderDiagnostics>>(emptyList())
    val storageUsage = MutableStateFlow("0 MB")

    // Sleep timer
    val sleepTimerMinutes = MutableStateFlow<Int?>(null)
    private var sleepTimerJob: Job? = null

    // Landing / Onboarding State
    val hasCompletedLanding = MutableStateFlow(true)

    init {
        rescanLibrary()
        refreshDiagnostics()
        refreshStorageUsage()

        // Track playback in history
        viewModelScope.launch {
            currentTrack.collect { track ->
                track?.let {
                    dao.insertHistory(HistoryEntity(trackId = it.id))
                }
            }
        }

        // Observe landing state
        viewModelScope.launch {
            dao.getSetting("has_completed_landing").collect { value ->
                hasCompletedLanding.value = (value == "true")
            }
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            isScanning.value = true
            try {
                val scanned = scanner.scanLocalAudio()
                dao.insertTracks(scanned.map { TrackEntity.fromDomainTrack(it) })
            } finally {
                isScanning.value = false
            }
        }
    }

    private var searchJob: Job? = null

    fun searchMusic(query: String) {
        searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            searchResults.value = emptyList()
            searchError.value = null
            isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // 400ms debounce
            isSearching.value = true
            searchError.value = null
            when (val result = providerRegistry.search(query)) {
                is ProviderResult.Success -> {
                    searchResults.value = result.data
                    // Cache found tracks so foreign keys are satisfied
                    dao.insertTracks(result.data.map { TrackEntity.fromDomainTrack(it) })
                }
                is ProviderResult.Error -> {
                    searchError.value = result.message
                }
            }
            isSearching.value = false
        }
    }

    private suspend fun resolvePlayableTrack(track: Track): Track {
        if (track.isLocal || track.mediaUri.startsWith("file:") || track.mediaUri.startsWith("content:")) {
            return track
        }
        return when (val resolved = providerRegistry.resolveStream(track)) {
            is ProviderResult.Success -> {
                if (resolved.data.isNotBlank()) track.copy(mediaUri = resolved.data) else track
            }
            is ProviderResult.Error -> track
        }
    }

    fun completeLandingPage() {
        viewModelScope.launch {
            dao.setSetting(com.example.data.local.SettingEntity("has_completed_landing", "true"))
            hasCompletedLanding.value = true
        }
    }

    fun revisitLandingPage() {
        hasCompletedLanding.value = false
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            val playable = resolvePlayableTrack(track)
            dao.insertTrack(TrackEntity.fromDomainTrack(playable))
            mediaController.playTrack(playable)
        }
    }

    fun setQueueAndPlay(tracks: List<Track>, startIndex: Int) {
        viewModelScope.launch {
            val safeIndex = startIndex.coerceIn(0, (tracks.size - 1).coerceAtLeast(0))
            val current = if (tracks.isNotEmpty()) resolvePlayableTrack(tracks[safeIndex]) else null
            val updatedTracks = tracks.toMutableList()
            if (current != null) {
                updatedTracks[safeIndex] = current
            }
            dao.insertTracks(updatedTracks.map { TrackEntity.fromDomainTrack(it) })
            mediaController.setQueueAndPlay(updatedTracks, safeIndex)
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            dao.insertTrack(TrackEntity.fromDomainTrack(track))
            val isFav = favoriteTracks.value.any { it.id == track.id }
            if (isFav) {
                dao.removeFavorite(track.id)
            } else {
                dao.addFavorite(FavoriteEntity(trackId = track.id))
            }
        }
    }

    fun isTrackFavorite(trackId: String): Boolean {
        return favoriteTracks.value.any { it.id == trackId }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                dao.insertPlaylist(PlaylistEntity(name = name.trim(), description = description.trim()))
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            dao.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            dao.insertTrack(TrackEntity.fromDomainTrack(track))
            dao.addTrackToPlaylist(
                PlaylistEntryEntity(
                    playlistId = playlistId,
                    trackId = track.id,
                    position = 999
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearHistory()
        }
    }

    fun downloadTrack(track: Track) {
        downloadManager.downloadTrack(track)
        refreshStorageUsage()
    }

    fun cancelDownload(trackId: String) {
        downloadManager.cancelDownload(trackId)
        refreshStorageUsage()
    }

    fun refreshStorageUsage() {
        viewModelScope.launch {
            val bytes = downloadManager.getStorageUsageBytes()
            storageUsage.value = downloadManager.formatBytes(bytes)
        }
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            diagnostics.value = providerRegistry.getAllDiagnostics()
        }
    }

    fun startVoiceControl() {
        isVoiceModalVisible.value = true
        voiceManager.startListening { intent ->
            handleVoiceIntent(intent)
        }
    }

    fun stopVoiceControl() {
        voiceManager.stopListening()
        isVoiceModalVisible.value = false
    }

    private fun handleVoiceIntent(intent: VoiceIntent) {
        when (intent) {
            is VoiceIntent.Play -> mediaController.resume()
            is VoiceIntent.Pause, is VoiceIntent.Stop -> mediaController.pause()
            is VoiceIntent.Next -> mediaController.playNext()
            is VoiceIntent.Previous -> mediaController.playPrevious()
            is VoiceIntent.ToggleShuffle -> mediaController.toggleShuffle()
            is VoiceIntent.ToggleRepeat -> mediaController.toggleRepeat()
            is VoiceIntent.AddFavorite -> {
                currentTrack.value?.let { toggleFavorite(it) }
            }
            is VoiceIntent.Search -> {
                searchMusic(intent.query)
            }
            is VoiceIntent.PlaySong -> {
                searchMusic(intent.songName)
                viewModelScope.launch {
                    delay(800)
                    searchResults.value.firstOrNull()?.let { playTrack(it) }
                }
            }
            is VoiceIntent.VolumeUp -> { /* System volume handled by Android hardware keys */ }
            is VoiceIntent.VolumeDown -> { /* System volume handled by Android hardware keys */ }
            is VoiceIntent.ShowCurrentTrack -> { isFullPlayerVisible.value = true }
            is VoiceIntent.Unknown -> {}
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerMinutes.value = minutes
        sleepTimerJob?.cancel()
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                mediaController.pause()
                sleepTimerMinutes.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController.release()
        voiceManager.stopListening()
    }
}
