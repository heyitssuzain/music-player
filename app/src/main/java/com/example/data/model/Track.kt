package com.example.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val durationMs: Long = 0L,
    val artworkUri: String? = null,
    val mediaUri: String,
    val isLocal: Boolean = false,
    val sourceProvider: String = "local",
    val dateAdded: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0L) return "--:--"
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
