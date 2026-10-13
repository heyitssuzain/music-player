package com.example.data.provider

import android.content.Context
import com.example.data.model.Track

class ProviderRegistry(context: Context? = null) {
    private val pipedProvider = PipedProvider()
    private val ytdlpProvider = YtDlpProvider(pipedProvider)
    private val directProvider = DirectStreamProvider(context)

    val providers: List<MusicProvider> = listOf(
        pipedProvider,
        ytdlpProvider,
        directProvider
    )

    // Default active provider is Piped (YouTube search and streaming)
    private var activeProviderIndex = 0

    val activeProvider: MusicProvider
        get() = providers[activeProviderIndex]

    fun setActiveProvider(index: Int) {
        if (index in providers.indices) {
            activeProviderIndex = index
        }
    }

    fun getProvider(sourceProviderId: String): MusicProvider? {
        val normalized = sourceProviderId.lowercase()
        return providers.firstOrNull {
            it.id.equals(normalized, ignoreCase = true) ||
            normalized.contains(it.id.removePrefix("aura_")) ||
            it.id.contains(normalized)
        }
    }

    suspend fun search(query: String): ProviderResult<List<Track>> {
        if (query.isBlank()) {
            return ProviderResult.Success(emptyList())
        }
        // Primary search through active provider (defaults to YouTube / Piped)
        val result = activeProvider.search(query)
        if (result is ProviderResult.Success && result.data.isNotEmpty()) {
            return result
        }

        // If active provider fails, try alternative online provider before returning
        for (provider in providers) {
            if (provider.id != activeProvider.id && provider !is DirectStreamProvider) {
                val fallback = provider.search(query)
                if (fallback is ProviderResult.Success && fallback.data.isNotEmpty()) {
                    return fallback
                }
            }
        }
        return result
    }

    suspend fun resolveStream(track: Track): ProviderResult<String> {
        // Dispatch resolution to the provider that owns the result
        val provider = getProvider(track.sourceProvider) ?: activeProvider
        return provider.resolveStream(track.id)
    }

    suspend fun getAllDiagnostics(): List<ProviderDiagnostics> {
        return providers.map { it.getDiagnostics() }
    }
}
