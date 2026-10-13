package com.example.data.provider

import com.example.data.model.Track

sealed class ProviderResult<out T> {
    data class Success<T>(val data: T) : ProviderResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ProviderResult<Nothing>()
}

data class ProviderDiagnostics(
    val providerId: String,
    val name: String,
    val isAvailable: Boolean,
    val latencyMs: Long,
    val version: String,
    val note: String
)

interface MusicProvider {
    val id: String
    val name: String

    suspend fun search(query: String): ProviderResult<List<Track>>
    suspend fun resolveStream(trackId: String): ProviderResult<String>
    suspend fun getDiagnostics(): ProviderDiagnostics
    fun cancel(operationId: String) {}
}
