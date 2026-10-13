package com.example

import com.example.data.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AuraTrackQueueTest {

    @Test
    fun testTrackDurationFormatting() {
        val track1 = Track(
            id = "t1",
            title = "Ambient Echo",
            mediaUri = "https://example.com/audio1.mp3",
            durationMs = 214000L
        )
        assertEquals("3:34", track1.formattedDuration)

        val trackZero = Track(
            id = "t2",
            title = "Zero Stream",
            mediaUri = "https://example.com/stream.mp3",
            durationMs = 0L
        )
        assertEquals("--:--", trackZero.formattedDuration)
    }

    @Test
    fun testQueueReordering() {
        val tracks = mutableListOf(
            Track(id = "1", title = "Song A", mediaUri = "uri1"),
            Track(id = "2", title = "Song B", mediaUri = "uri2"),
            Track(id = "3", title = "Song C", mediaUri = "uri3")
        )

        // Move item at index 0 to index 2
        val item = tracks.removeAt(0)
        tracks.add(2, item)

        assertEquals("2", tracks[0].id)
        assertEquals("3", tracks[1].id)
        assertEquals("1", tracks[2].id)
    }
}
