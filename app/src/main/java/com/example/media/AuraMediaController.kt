package com.example.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.model.Track
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AuraMediaController(
    private val context: Context,
    private val externalScope: CoroutineScope
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private var progressJob: Job? = null
    private var pendingPlayTrack: Track? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        try {
            val serviceIntent = android.content.Intent(context, AuraMediaService::class.java)
            context.startService(serviceIntent)
        } catch (e: Exception) {
            // Service will be bound by MediaController if background restrictions apply
        }

        val sessionToken = SessionToken(
            context,
            ComponentName(context, AuraMediaService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    mediaController = controllerFuture?.get()
                    setupPlayerListener()
                    pendingPlayTrack?.let {
                        val track = it
                        pendingPlayTrack = null
                        playCurrentTrack(track)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AuraMediaController", "Failed to connect to MediaController", e)
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressTracking()
                } else {
                    stopProgressTracking()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    mediaController?.let {
                        _durationMs.value = if (it.duration > 0) it.duration else 0L
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _isShuffleEnabled.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _isPlaying.value = false
                stopProgressTracking()
            }
        })
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = externalScope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaController?.let {
                    _playbackPositionMs.value = it.currentPosition.coerceAtLeast(0L)
                    if (it.duration > 0) {
                        _durationMs.value = it.duration
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun playTrack(track: Track) {
        val currentQueue = _queue.value.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == track.id }
        if (index == -1) {
            currentQueue.add(track)
            _queue.value = currentQueue
            _currentIndex.value = currentQueue.size - 1
        } else {
            _currentIndex.value = index
        }
        playCurrentTrack(track)
    }

    fun setQueueAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        _queue.value = tracks
        val safeIndex = startIndex.coerceIn(0, tracks.size - 1)
        _currentIndex.value = safeIndex
        playCurrentTrack(tracks[safeIndex])
    }

    private fun playCurrentTrack(track: Track) {
        _currentTrack.value = track
        _playbackPositionMs.value = 0L
        _durationMs.value = track.durationMs

        val player = mediaController
        if (player == null) {
            pendingPlayTrack = track
            return
        }

        val parsedUri = when {
            track.mediaUri.startsWith("http://") || track.mediaUri.startsWith("https://") -> Uri.parse(track.mediaUri)
            track.mediaUri.startsWith("content://") -> Uri.parse(track.mediaUri)
            track.mediaUri.startsWith("file://") -> Uri.parse(track.mediaUri)
            else -> Uri.fromFile(java.io.File(track.mediaUri))
        }

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .apply {
                track.artworkUri?.let { setArtworkUri(Uri.parse(it)) }
            }
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(parsedUri)
            .setMediaMetadata(mediaMetadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs
        mediaController?.seekTo(positionMs)
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return
        val nextIndex = when {
            _isShuffleEnabled.value -> (q.indices).random()
            _repeatMode.value == Player.REPEAT_MODE_ONE -> _currentIndex.value
            _currentIndex.value + 1 < q.size -> _currentIndex.value + 1
            _repeatMode.value == Player.REPEAT_MODE_ALL -> 0
            else -> -1
        }

        if (nextIndex in q.indices) {
            _currentIndex.value = nextIndex
            playCurrentTrack(q[nextIndex])
        }
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return
        if (_playbackPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val prevIndex = if (_currentIndex.value - 1 >= 0) {
            _currentIndex.value - 1
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
            q.size - 1
        } else {
            0
        }
        if (prevIndex in q.indices) {
            _currentIndex.value = prevIndex
            playCurrentTrack(q[prevIndex])
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newShuffle
        mediaController?.shuffleModeEnabled = newShuffle
    }

    fun toggleRepeat() {
        val nextRepeat = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextRepeat
        mediaController?.repeatMode = nextRepeat
    }

    fun removeFromQueue(index: Int) {
        val currentList = _queue.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _queue.value = currentList
            if (index < _currentIndex.value) {
                _currentIndex.value -= 1
            } else if (index == _currentIndex.value && currentList.isNotEmpty()) {
                val newIndex = index.coerceAtMost(currentList.size - 1)
                _currentIndex.value = newIndex
                playCurrentTrack(currentList[newIndex])
            }
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val currentList = _queue.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _queue.value = currentList
            if (_currentIndex.value == fromIndex) {
                _currentIndex.value = toIndex
            }
        }
    }

    fun release() {
        stopProgressTracking()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
    }
}
