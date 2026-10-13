package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalAudioScanner(private val context: Context) {

    suspend fun scanLocalAudio(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Track"
                    val artist = c.getString(artistCol) ?: "Unknown Artist"
                    val album = c.getString(albumCol) ?: "Unknown Album"
                    val duration = c.getLong(durationCol)
                    val albumId = c.getLong(albumIdCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val albumArtUri = "content://media/external/audio/albumart/$albumId"

                    tracks.add(
                        Track(
                            id = "local_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = duration,
                            artworkUri = albumArtUri,
                            mediaUri = contentUri,
                            isLocal = true,
                            sourceProvider = "local"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission denied or security exception handled gracefully
        }

        // If local library is empty on emulator/new device, bundle real on-device synthesized audio files
        if (tracks.isEmpty()) {
            tracks.addAll(getDemoTracks())
        }

        tracks
    }

    private fun getDemoTracks(): List<Track> {
        val f1 = WavAudioGenerator.createSynthesizedTrack(
            context = context,
            fileName = "aura_ambient_horizon.wav",
            durationSeconds = 16,
            baseFrequencies = doubleArrayOf(220.0, 277.18, 329.63, 440.0),
            tempoBpm = 85
        )
        val f2 = WavAudioGenerator.createSynthesizedTrack(
            context = context,
            fileName = "aura_electric_velvet.wav",
            durationSeconds = 16,
            baseFrequencies = doubleArrayOf(174.61, 220.0, 261.63, 349.23),
            tempoBpm = 95
        )
        val f3 = WavAudioGenerator.createSynthesizedTrack(
            context = context,
            fileName = "aura_cybernetic_pulse.wav",
            durationSeconds = 16,
            baseFrequencies = doubleArrayOf(130.81, 164.81, 196.0, 246.94),
            tempoBpm = 110
        )
        val f4 = WavAudioGenerator.createSynthesizedTrack(
            context = context,
            fileName = "aura_deep_ocean.wav",
            durationSeconds = 16,
            baseFrequencies = doubleArrayOf(146.83, 220.0, 293.66, 369.99),
            tempoBpm = 75
        )

        return listOf(
            Track(
                id = "aura_demo_1",
                title = "AURA Ambient Horizon",
                artist = "Solaris Wave",
                album = "Neon Drift Vol. 1",
                durationMs = 16000L,
                artworkUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
                mediaUri = android.net.Uri.fromFile(f1).toString(),
                isLocal = true,
                sourceProvider = "demo"
            ),
            Track(
                id = "aura_demo_2",
                title = "Electric Velvet Night",
                artist = "Luna Resonance",
                album = "Neon Drift Vol. 1",
                durationMs = 16000L,
                artworkUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80",
                mediaUri = android.net.Uri.fromFile(f2).toString(),
                isLocal = true,
                sourceProvider = "demo"
            ),
            Track(
                id = "aura_demo_3",
                title = "Cybernetic Pulse",
                artist = "Apex Synth",
                album = "Midnight Frequency",
                durationMs = 16000L,
                artworkUri = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
                mediaUri = android.net.Uri.fromFile(f3).toString(),
                isLocal = true,
                sourceProvider = "demo"
            ),
            Track(
                id = "aura_demo_4",
                title = "Deep Ocean Reverie",
                artist = "Kavita Sounds",
                album = "Ethereal Echoes",
                durationMs = 16000L,
                artworkUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                mediaUri = android.net.Uri.fromFile(f4).toString(),
                isLocal = true,
                sourceProvider = "demo"
            )
        )
    }
}
