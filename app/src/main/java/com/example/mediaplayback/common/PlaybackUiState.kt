package com.example.mediaplayback.common

import com.example.mediaplayback.manager.PlaybackStatus

data class PlaybackUiState(
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentMs: Long = 0L,
    val totalMs: Long = 0L
) {
    val valueRange: ClosedFloatingPointRange<Float>
        get() = 0f..totalMs.toFloat()
}