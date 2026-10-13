package com.example

import com.example.data.model.Track
import com.example.data.provider.DirectStreamProvider
import com.example.data.provider.MusicProvider
import com.example.data.provider.PipedProvider
import com.example.data.provider.ProviderRegistry
import com.example.data.provider.ProviderResult
import com.example.data.provider.YtDlpProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder

class ProviderRoutingTest {

    @Test
    fun testProviderRegistryInitialState() {
        val registry = ProviderRegistry(null)
        // Default active provider must be Piped for genuine online discovery
        assertEquals("aura_piped", registry.activeProvider.id)
        assertTrue(registry.providers.any { it is PipedProvider })
        assertTrue(registry.providers.any { it is YtDlpProvider })
        assertTrue(registry.providers.any { it is DirectStreamProvider })
    }

    @Test
    fun testProviderResolutionRouting() = runBlocking {
        val registry = ProviderRegistry(null)

        // Track with sourceProvider = "direct" or "aura_direct"
        val directTrack = Track(
            id = "direct_1",
            title = "Midnight Odyssey",
            mediaUri = "file:///android_asset/direct_1.wav",
            sourceProvider = "aura_direct"
        )
        val directResult = registry.resolveStream(directTrack)
        assertTrue(directResult is ProviderResult.Success)
        assertEquals("file:///android_asset/direct_1.wav", (directResult as ProviderResult.Success).data)

        // Track with unknown direct ID should fail gracefully
        val missingTrack = Track(
            id = "non_existent_id",
            title = "Missing",
            mediaUri = "",
            sourceProvider = "aura_direct"
        )
        val missingResult = registry.resolveStream(missingTrack)
        assertTrue(missingResult is ProviderResult.Error)
    }

    @Test
    fun testSearchQueryUrlEncoding() {
        val queryEng = "coldplay yellow"
        val encodedEng = URLEncoder.encode(queryEng, "UTF-8")
        assertEquals("coldplay+yellow", encodedEng)

        val queryHindi = "अरिजीत सिंह kesariya"
        val encodedHindi = URLEncoder.encode(queryHindi, "UTF-8")
        assertTrue(encodedHindi.contains("%"))
        assertTrue(encodedHindi.contains("kesariya"))
    }

    @Test
    fun testYtDlpProviderDiagnostics() = runBlocking {
        val ytdlp = YtDlpProvider()
        val diag = ytdlp.getDiagnostics()
        assertEquals("aura_ytdlp", diag.providerId)
        assertTrue(diag.isAvailable)
        assertTrue(diag.version.contains("android"))
    }
}

