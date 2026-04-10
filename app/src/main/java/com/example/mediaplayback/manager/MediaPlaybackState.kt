package com.example.mediaplayback.manager

data class MediaPlaybackState(
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val totalDuration: Long = 0L
)

enum class PlaybackStatus {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    ENDED
}