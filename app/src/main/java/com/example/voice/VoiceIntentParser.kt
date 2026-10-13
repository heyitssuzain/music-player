package com.example.voice

class VoiceIntentParser {

    fun parse(transcript: String): VoiceIntent {
        val clean = transcript.trim().lowercase()
        if (clean.isEmpty()) return VoiceIntent.Unknown(transcript)

        // 1. Play / Resume / Bajao
        if (clean == "play" || clean == "resume" || clean == "gana bajao" || clean == "chalao" || clean == "shuru karo") {
            return VoiceIntent.Play
        }

        // 2. Pause / Stop / Roko
        if (clean == "pause" || clean == "roko" || clean == "rok do" || clean == "ruk jao" || clean == "hold") {
            return VoiceIntent.Pause
        }

        if (clean == "stop" || clean == "band karo" || clean == "khatam") {
            return VoiceIntent.Stop
        }

        // 3. Next / Skip / Agla
        if (clean == "next" || clean == "skip" || clean == "agla" || clean == "agla gana" || clean == "next song") {
            return VoiceIntent.Next
        }

        // 4. Previous / Pichhla
        if (clean == "previous" || clean == "back" || clean == "pichhla" || clean == "pichhla gana" || clean == "previous song") {
            return VoiceIntent.Previous
        }

        // 5. Volume
        if (clean.contains("volume up") || clean.contains("awaz badhao") || clean.contains("louder")) {
            return VoiceIntent.VolumeUp
        }
        if (clean.contains("volume down") || clean.contains("awaz kam karo") || clean.contains("softer")) {
            return VoiceIntent.VolumeDown
        }

        // 6. Favorite
        if (clean.contains("favorite") || clean.contains("pasand") || clean.contains("like") || clean.contains("heart")) {
            return VoiceIntent.AddFavorite
        }

        // 7. Shuffle & Repeat
        if (clean.contains("shuffle")) {
            return VoiceIntent.ToggleShuffle()
        }
        if (clean.contains("repeat") || clean.contains("dobara")) {
            return VoiceIntent.ToggleRepeat()
        }

        // 8. Show current track
        if (clean.contains("what is playing") || clean.contains("current song") || clean.contains("kaun sa gana")) {
            return VoiceIntent.ShowCurrentTrack
        }

        // 9. Play specific song
        val playPrefixes = listOf("play ", "bajao ", "sunao ")
        for (prefix in playPrefixes) {
            if (clean.startsWith(prefix)) {
                val song = clean.removePrefix(prefix).trim()
                if (song.isNotEmpty()) return VoiceIntent.PlaySong(song)
            }
        }
        if (clean.endsWith(" bajao") || clean.endsWith(" sunao")) {
            val song = clean.removeSuffix(" bajao").removeSuffix(" sunao").trim()
            if (song.isNotEmpty()) return VoiceIntent.PlaySong(song)
        }

        // 10. Search
        val searchPrefixes = listOf("search for ", "search ", "find ", "khojo ", "dhoondo ")
        for (prefix in searchPrefixes) {
            if (clean.startsWith(prefix)) {
                val query = clean.removePrefix(prefix).trim()
                if (query.isNotEmpty()) return VoiceIntent.Search(query)
            }
        }

        return VoiceIntent.Unknown(transcript)
    }
}
