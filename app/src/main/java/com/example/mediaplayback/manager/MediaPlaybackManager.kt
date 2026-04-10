package com.example.mediaplayback.manager

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class MediaPlaybackManager @Inject constructor (
    private val context: Context
) {
    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            _playbackState.update {
                it.copy(
                    status = player.playbackState.toPlaybackStatus(player.isPlaying),
                    totalDuration = player.duration.coerceAtLeast(0L)
                )
            }
        }
    }

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(listener)
        }
    }

    private val _playbackState = MutableStateFlow(MediaPlaybackState())
    val playbackState = _playbackState.asStateFlow()

    val currentPosition: Long
        get() = exoPlayer.currentPosition.coerceAtLeast(0)

    fun setup(
        url: String,
        playWhenReady: Boolean = true,
        speed: Float = 1f,
    ) {
        val mediaItem = MediaItem.fromUri(url)

        exoPlayer.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            this.playbackParameters = PlaybackParameters(speed)
            prepare()
            this.playWhenReady = playWhenReady
        }
    }

    fun resume() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun release() {
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }

    private fun Int.toPlaybackStatus(isPlaying: Boolean): PlaybackStatus = when (this) {
        Player.STATE_IDLE -> PlaybackStatus.IDLE
        Player.STATE_BUFFERING -> PlaybackStatus.LOADING
        Player.STATE_READY -> {
            if (isPlaying) PlaybackStatus.PLAYING
            else PlaybackStatus.PAUSED
        }
        Player.STATE_ENDED -> PlaybackStatus.ENDED
        else -> PlaybackStatus.IDLE
    }
}