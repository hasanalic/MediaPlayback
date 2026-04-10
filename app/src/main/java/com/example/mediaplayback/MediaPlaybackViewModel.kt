package com.example.mediaplayback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplayback.common.PlaybackAction
import com.example.mediaplayback.common.PlaybackUiState
import com.example.mediaplayback.manager.MediaPlaybackManager
import com.example.mediaplayback.manager.PlaybackStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class MediaPlaybackViewModel @Inject constructor(
    private val playbackManager: MediaPlaybackManager
) : ViewModel() {

    private val _playbackPosition = MutableStateFlow(0L)
    private val _userSeekPosition = MutableStateFlow<Long?>(null)
    private val _pendingSeekPosition = MutableStateFlow<Long?>(null)

    val playbackUiState: StateFlow<PlaybackUiState> =
        combine(
            _playbackPosition,
            _userSeekPosition,
            _pendingSeekPosition,
            playbackManager.playbackState
        ) { playbackPos, userSeekPos, pendingSeekPos, state ->
            val currentMs = userSeekPos ?: pendingSeekPos ?: playbackPos
            PlaybackUiState(
                status = state.status,
                currentMs = currentMs,
                totalMs = state.totalDuration
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaybackUiState()
        )

    init {
        startPlayback()
        trackPlaybackPosition()
    }

    private fun trackPlaybackPosition() {
        viewModelScope.launch {
            while (isActive) {
                val currentPosition = playbackManager.currentPosition
                _playbackPosition.value = currentPosition

                val pendingSeek = _pendingSeekPosition.value
                if (pendingSeek != null) {
                    val difference = abs(currentPosition - pendingSeek)
                    if (difference <= 300L) {
                        _pendingSeekPosition.value = null
                    }
                }

                delay(150)
            }
        }
    }

    fun onPlaybackAction(action: PlaybackAction) {
        when (action) {
            PlaybackAction.PlayPauseClick -> onPlayPauseClick()
            is PlaybackAction.SeekChange -> onSeekChanged(action.position)
            is PlaybackAction.SeekFinish -> onSeekFinished(action.position)
            PlaybackAction.Backward -> onBackwardClick()
            PlaybackAction.Forward -> onForwardClick()
        }
    }

    private fun onPlayPauseClick() {
        when(playbackUiState.value.status) {
            PlaybackStatus.PLAYING -> pausePlayback()
            PlaybackStatus.ENDED -> startPlayback()
            PlaybackStatus.PAUSED -> resumePlayback()
            PlaybackStatus.IDLE, PlaybackStatus.LOADING -> Unit
        }
    }

    private fun startPlayback() {
        initializePlayback(
            url = "https://dn711403.ca.archive.org/0/items/JimenezylosSantos/WrongHoleSurprise.mp3",
            playWhenReady = true
        )
    }

    private fun initializePlayback(url: String, playWhenReady: Boolean = true) {
        viewModelScope.launch {
            _playbackPosition.value = 0L
            _userSeekPosition.value = null
            _pendingSeekPosition.value = null
            playbackManager.setup(url = url, playWhenReady = playWhenReady)
        }
    }

    private fun resumePlayback() {
        playbackManager.resume()
    }

    private fun pausePlayback() {
        playbackManager.pause()
    }

    fun onSeekChanged(position: Long) {
        _userSeekPosition.value = position
    }

    fun onSeekFinished(position: Long) {
        _pendingSeekPosition.value = position
        _userSeekPosition.value = null
        playbackManager.seekTo(position)
    }

    private fun clearPlayback() {
        playbackManager.release()
    }

    private fun onBackwardClick() {
        val totalMs = playbackUiState.value.totalMs
        val currentMs = _playbackPosition.value
        val newPosition = (currentMs - JUMP_OFFSET).coerceIn(minimumValue = 0L, maximumValue = totalMs)
        onSeekFinished(newPosition)
    }

    private fun onForwardClick() {
        val totalMs = playbackUiState.value.totalMs
        val currentMs = _playbackPosition.value
        val newPosition = (currentMs + JUMP_OFFSET).coerceIn(minimumValue = 0L, maximumValue = totalMs)
        onSeekFinished(newPosition)
    }

    override fun onCleared() {
        super.onCleared()
        clearPlayback()
    }

    companion object {
        private const val JUMP_OFFSET = 10_000
    }
}