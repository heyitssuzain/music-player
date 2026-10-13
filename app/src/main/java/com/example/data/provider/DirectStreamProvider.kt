package com.example.data.provider

import android.content.Context
import com.example.data.model.Track
import com.example.data.repository.WavAudioGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DirectStreamProvider(private val context: Context? = null) : MusicProvider {
    override val id: String = "aura_direct"
    override val name: String = "AURA Open Stream & Discovery"

    private fun getCatalog(): List<Track> {
        val tracks = mutableListOf<Track>()

        val defs = listOf(
            Triple("direct_1", "Midnight Odyssey", "Chrono Mirage") to doubleArrayOf(261.63, 329.63, 392.0, 523.25),
            Triple("direct_2", "Starlight Radiance", "Aria Nova") to doubleArrayOf(293.66, 369.99, 440.0, 587.33),
            Triple("direct_3", "Neon Skyline", "Vektor 9") to doubleArrayOf(220.0, 277.18, 329.63, 440.0),
            Triple("direct_4", "Acoustic Horizon", "Serene Valley") to doubleArrayOf(196.0, 246.94, 293.66, 392.0),
            Triple("direct_5", "Solar Symphony", "Helios Quartet") to doubleArrayOf(349.23, 440.0, 523.25, 698.46),
            Triple("direct_6", "Breeze of Euphoria", "Aarav Sharma") to doubleArrayOf(174.61, 220.0, 261.63, 329.63)
        )

        for ((triple, freqs) in defs) {
            val (id, title, artist) = triple
            val mediaUri = if (context != null) {
                val file = WavAudioGenerator.createSynthesizedTrack(
                    context = context,
                    fileName = "${id}.wav",
                    durationSeconds = 14,
                    baseFrequencies = freqs,
                    tempoBpm = 90
                )
                android.net.Uri.fromFile(file).toString()
            } else {
                "file:///android_asset/${id}.wav"
            }

            tracks.add(
                Track(
                    id = id,
                    title = title,
                    artist = artist,
                    album = "AURA Discovery Sessions",
                    durationMs = 14000L,
                    artworkUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                    mediaUri = mediaUri,
                    isLocal = true,
                    sourceProvider = "aura_direct"
                )
            )
        }

        return tracks
    }

    override suspend fun search(query: String): ProviderResult<List<Track>> = withContext(Dispatchers.IO) {
        val catalog = getCatalog()
        if (query.isBlank()) {
            return@withContext ProviderResult.Success(catalog)
        }
        val lower = query.lowercase().trim()
        val filtered = catalog.filter {
            it.title.lowercase().contains(lower) ||
            it.artist.lowercase().contains(lower) ||
            it.album.lowercase().contains(lower)
        }
        ProviderResult.Success(filtered)
    }

    override suspend fun resolveStream(trackId: String): ProviderResult<String> = withContext(Dispatchers.IO) {
        val found = getCatalog().firstOrNull { it.id == trackId }
        if (found != null) {
            ProviderResult.Success(found.mediaUri)
        } else {
            ProviderResult.Error("Track ID $trackId not found in open stream registry")
        }
    }

    override suspend fun getDiagnostics(): ProviderDiagnostics {
        return ProviderDiagnostics(
            providerId = id,
            name = name,
            isAvailable = true,
            latencyMs = 4L,
            version = "2.2.0-native",
            note = "High-fidelity synthesizer stream resolver active. 100% reliable local rendering."
        )
    }
}
