package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artworkUri: String?,
    val mediaUri: String,
    val isLocal: Boolean,
    val sourceProvider: String,
    val dateAdded: Long = System.currentTimeMillis()
) {
    fun toDomainTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        artworkUri = artworkUri,
        mediaUri = mediaUri,
        isLocal = isLocal,
        sourceProvider = sourceProvider,
        dateAdded = dateAdded
    )

    companion object {
        fun fromDomainTrack(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            artworkUri = track.artworkUri,
            mediaUri = track.mediaUri,
            isLocal = track.isLocal,
            sourceProvider = track.sourceProvider,
            dateAdded = track.dateAdded
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val trackCount: Int = 0
)

@Entity(
    tableName = "playlist_entries",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("trackId")]
)
data class PlaylistEntryEntity(
    val playlistId: Long,
    val trackId: String,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "history",
    indices = [Index("playedAt")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val downloadUrl: String,
    val localFilePath: String?,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: String = "QUEUED", // QUEUED, DOWNLOADING, COMPLETED, FAILED, CANCELLED
    val progress: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
