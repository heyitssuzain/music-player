package com.example

import com.example.voice.VoiceIntent
import com.example.voice.VoiceIntentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceIntentParserTest {

    private val parser = VoiceIntentParser()

    @Test
    fun testEnglishBasicPlaybackCommands() {
        assertEquals(VoiceIntent.Play, parser.parse("play"))
        assertEquals(VoiceIntent.Play, parser.parse("resume"))
        assertEquals(VoiceIntent.Pause, parser.parse("pause"))
        assertEquals(VoiceIntent.Stop, parser.parse("stop"))
        assertEquals(VoiceIntent.Next, parser.parse("next"))
        assertEquals(VoiceIntent.Previous, parser.parse("previous"))
    }

    @Test
    fun testHindiHinglishCommands() {
        assertEquals(VoiceIntent.Play, parser.parse("gana bajao"))
        assertEquals(VoiceIntent.Pause, parser.parse("rok do"))
        assertEquals(VoiceIntent.Next, parser.parse("agla gana"))
        assertEquals(VoiceIntent.Previous, parser.parse("pichhla gana"))
        assertEquals(VoiceIntent.AddFavorite, parser.parse("pasand karo"))
    }

    @Test
    fun testVolumeAndFavoriteCommands() {
        assertEquals(VoiceIntent.VolumeUp, parser.parse("volume up"))
        assertEquals(VoiceIntent.VolumeDown, parser.parse("volume down"))
        assertEquals(VoiceIntent.VolumeUp, parser.parse("awaz badhao"))
        assertEquals(VoiceIntent.VolumeDown, parser.parse("awaz kam karo"))
        assertEquals(VoiceIntent.AddFavorite, parser.parse("add to favorite"))
    }

    @Test
    fun testSearchAndPlayNamedSong() {
        val playIntent = parser.parse("play Midnight Horizon")
        assertTrue(playIntent is VoiceIntent.PlaySong)
        assertEquals("midnight horizon", (playIntent as VoiceIntent.PlaySong).songName)

        val searchIntent = parser.parse("search for Solaris Wave")
        assertTrue(searchIntent is VoiceIntent.Search)
        assertEquals("solaris wave", (searchIntent as VoiceIntent.Search).query)

        val hindiSearch = parser.parse("khojo Neon Drift")
        assertTrue(hindiSearch is VoiceIntent.Search)
        assertEquals("neon drift", (hindiSearch as VoiceIntent.Search).query)
    }
}
