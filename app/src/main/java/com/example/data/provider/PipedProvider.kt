package com.example.data.provider

import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class PipedProvider : MusicProvider {
    override val id: String = "aura_piped"
    override val name: String = "YouTube Online Stream Extractor (Piped)"

    private val client = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Public privacy-friendly instance endpoints
    private val instances = listOf(
        "https://pipedapi.kavin.rocks",
        "https://api.piped.private.coffee",
        "https://piped-api.lunar.icu",
        "https://pipedapi.leptons.xyz"
    )

    override suspend fun search(query: String): ProviderResult<List<Track>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext ProviderResult.Success(emptyList())

        val encodedQuery = try {
            URLEncoder.encode(trimmed, "UTF-8")
        } catch (e: Exception) {
            trimmed
        }

        var lastErrorMessage = "Network unreachable or instances unavailable"

        for (instance in instances) {
            try {
                val url = "$instance/search?q=$encodedQuery&filter=music_songs"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AURA/1.0")
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val items = json.optJSONArray("items") ?: JSONArray()
                    val tracks = mutableListOf<Track>()

                    for (i in 0 until items.length().coerceAtMost(30)) {
                        val item = items.getJSONObject(i)
                        val type = item.optString("type")
                        if (type == "stream" || type == "music_song" || type == "video") {
                            val urlPart = item.optString("url", "")
                            val videoId = urlPart.removePrefix("/watch?v=").removePrefix("/")
                            if (videoId.isBlank()) continue

                            val title = item.optString("title", "Unknown Title")
                            val uploader = item.optString("uploaderName", "Unknown Artist")
                            val durationSec = item.optLong("duration", 0L)
                            val thumbnail = item.optString("thumbnail").takeIf { it.isNotBlank() }

                            tracks.add(
                                Track(
                                    id = "piped_$videoId",
                                    title = title,
                                    artist = uploader,
                                    album = "YouTube Single",
                                    durationMs = durationSec * 1000L,
                                    artworkUri = thumbnail,
                                    mediaUri = "$instance/streams/$videoId",
                                    isLocal = false,
                                    sourceProvider = "aura_piped"
                                )
                            )
                        }
                    }

                    if (tracks.isNotEmpty()) {
                        return@withContext ProviderResult.Success(tracks)
                    }
                } else {
                    lastErrorMessage = "HTTP ${response.code} from $instance"
                }
            } catch (e: Exception) {
                lastErrorMessage = e.message ?: "Failed connecting to $instance"
            }
        }

        ProviderResult.Error("Online search unavailable ($lastErrorMessage). Check network connection.")
    }

    override suspend fun resolveStream(trackId: String): ProviderResult<String> = withContext(Dispatchers.IO) {
        val videoId = trackId.removePrefix("piped_")
        var lastError = "Could not resolve stream URL"

        for (instance in instances) {
            try {
                val url = "$instance/streams/$videoId"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AURA/1.0")
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)

                    // 1. Check audio streams (prefer highest bitrate audio)
                    val audioStreams = json.optJSONArray("audioStreams")
                    if (audioStreams != null && audioStreams.length() > 0) {
                        var bestUrl = ""
                        var bestBitrate = 0
                        for (i in 0 until audioStreams.length()) {
                            val streamObj = audioStreams.getJSONObject(i)
                            val streamUrl = streamObj.optString("url")
                            val bitrate = streamObj.optInt("bitrate", 0)
                            if (streamUrl.isNotBlank() && (bitrate >= bestBitrate || bestUrl.isEmpty())) {
                                bestUrl = streamUrl
                                bestBitrate = bitrate
                            }
                        }
                        if (bestUrl.isNotBlank()) {
                            return@withContext ProviderResult.Success(bestUrl)
                        }
                    }

                    // 2. Fallback to general video streams if audio-only stream is not segregated
                    val videoStreams = json.optJSONArray("videoStreams")
                    if (videoStreams != null && videoStreams.length() > 0) {
                        val firstVideo = videoStreams.getJSONObject(0)
                        val vUrl = firstVideo.optString("url")
                        if (vUrl.isNotBlank()) {
                            return@withContext ProviderResult.Success(vUrl)
                        }
                    }
                } else {
                    lastError = "HTTP ${response.code} from $instance"
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Connection error to $instance"
            }
        }

        ProviderResult.Error("Unable to resolve audio stream for track $trackId ($lastError)")
    }

    override suspend fun getDiagnostics(): ProviderDiagnostics {
        var isLive = false
        var latency = 0L
        val start = System.currentTimeMillis()
        try {
            val req = Request.Builder().url("${instances.first()}/feed/unauthenticated").build()
            val resp = client.newCall(req).execute()
            latency = System.currentTimeMillis() - start
            isLive = resp.isSuccessful
        } catch (e: Exception) {
            isLive = false
            latency = System.currentTimeMillis() - start
        }

        return ProviderDiagnostics(
            providerId = id,
            name = name,
            isAvailable = isLive,
            latencyMs = latency,
            version = "3.4.1",
            note = if (isLive) "Operational. Direct HTTPS YouTube audio extraction active." else "Rate limited or network offline."
        )
    }
}
