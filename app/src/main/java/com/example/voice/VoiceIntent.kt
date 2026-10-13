package com.example.voice

sealed class VoiceIntent {
    object Play : VoiceIntent()
    object Pause : VoiceIntent()
    object Next : VoiceIntent()
    object Previous : VoiceIntent()
    object Stop : VoiceIntent()
    data class Search(val query: String) : VoiceIntent()
    data class PlaySong(val songName: String) : VoiceIntent()
    data class ToggleShuffle(val enable: Boolean? = null) : VoiceIntent()
    data class ToggleRepeat(val mode: String = "toggle") : VoiceIntent()
    object AddFavorite : VoiceIntent()
    object VolumeUp : VoiceIntent()
    object VolumeDown : VoiceIntent()
    object ShowCurrentTrack : VoiceIntent()
    data class Unknown(val rawTranscript: String) : VoiceIntent()
}
