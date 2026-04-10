package com.example.mediaplayback.common

sealed interface PlaybackAction {
    data object PlayPauseClick: PlaybackAction
    data class SeekChange(val position: Long) : PlaybackAction
    data class SeekFinish(val position: Long) : PlaybackAction
    data object Backward : PlaybackAction
    data object Forward : PlaybackAction
}
