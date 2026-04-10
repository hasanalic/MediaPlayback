package com.example.mediaplayback

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaplayback.common.PlaybackAction
import com.example.mediaplayback.common.PlaybackUiState
import com.example.mediaplayback.manager.PlaybackStatus
import com.example.mediaplayback.ui.theme.MediaPlaybackTheme

@Composable
fun MediaPlaybackScreen(
    soundResultViewModel: MediaPlaybackViewModel = hiltViewModel()
) {
    val playbackUiState by soundResultViewModel.playbackUiState.collectAsStateWithLifecycle()

    MediaPlaybackScreenContent(
        playbackUiState = playbackUiState,
        onAction = soundResultViewModel::onPlaybackAction
    )
}

@Composable
fun MediaPlaybackScreenContent(
    playbackUiState: PlaybackUiState,
    onAction: (PlaybackAction) -> Unit
) {
    Scaffold(
        containerColor = Color.Black,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.img_background),
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
            ) {
                CustomAnimation(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .height(400.dp),
                    circleSize = 300.dp
                )

                MediaPlaybackController(
                    modifier = Modifier
                        .padding(horizontal = 24.dp),
                    currentMs = playbackUiState.currentMs,
                    totalMs = playbackUiState.totalMs,
                    playbackStatus = playbackUiState.status,
                    valueRange = playbackUiState.valueRange,
                    onAction = { onAction(it) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun MediaPlaybackScreenContentPreview() {
    val playbackUiState = PlaybackUiState(
        status = PlaybackStatus.PLAYING,
        currentMs = 100L,
        totalMs = 200L
    )

    MediaPlaybackTheme {
        MediaPlaybackScreenContent(
            playbackUiState = playbackUiState,
            onAction = {}
        )
    }
}