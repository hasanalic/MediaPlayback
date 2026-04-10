package com.example.mediaplayback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediaplayback.common.PlaybackAction
import com.example.mediaplayback.extensions.toTimeText
import com.example.mediaplayback.manager.PlaybackStatus
import com.example.mediaplayback.ui.theme.MediaPlaybackTheme
import kotlinx.coroutines.launch

@Composable
fun MediaPlaybackController(
    currentMs: Long,
    totalMs: Long,
    playbackStatus: PlaybackStatus,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    onAction: (PlaybackAction) -> Unit
) {
    val currentTime by remember(currentMs) {
        derivedStateOf {
            (currentMs / 1000).toInt().toTimeText()
        }
    }

    val remainingTime by remember(currentMs, totalMs) {
        derivedStateOf {
            val currentSec = (currentMs / 1000).toInt()
            val totalSec = (totalMs / 1000).toInt()
            val remaining = (totalSec - currentSec).coerceAtLeast(0)
            remaining.toTimeText()
        }
    }

    Column(
        modifier = modifier
    ) {
        CustomSlider(
            value = currentMs.toFloat(),
            valueRange = valueRange,
            onSeekChanged = { onAction(PlaybackAction.SeekChange(it.toLong())) },
            onSeekFinished = { onAction(PlaybackAction.SeekFinish(it.toLong())) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentTime,
                color = Color(0xFFA8A8AC),
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.5.sp
                )
            )

            Text(
                text = remainingTime,
                color = Color(0xFFA8A8AC),
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_backward),
                    modifier = Modifier
                        .clickable {
                            onAction(PlaybackAction.Backward)
                        }
                        .size(24.dp),
                    tint = Color.White,
                    contentDescription = null
                )
                Text(
                    text = "10",
                    color = Color(0xFFA8A8AC),
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            PlayPauseButton(
                onClick = { onAction(PlaybackAction.PlayPauseClick) },
                iconRes = if (playbackStatus == PlaybackStatus.PLAYING) R.drawable.ic_pause else R.drawable.ic_play,
                isEnabled = playbackStatus != PlaybackStatus.LOADING
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_forward),
                    modifier = Modifier
                        .clickable {
                            onAction(PlaybackAction.Forward)
                        }
                        .size(24.dp),
                    tint = Color.White,
                    contentDescription = null
                )
                Text(
                    text = "10",
                    color = Color(0xFFA8A8AC),
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun PlayPauseButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    suspend fun animateClick() {
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing)
        )
    }

    val alpha = if (isEnabled) 1f else 0.6f

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
            }
            .scale(scale.value)
            .background(color = Color.White, shape = CircleShape)
            .clickable(enabled = isEnabled) {
                if (scale.isRunning.not()) {
                    scope.launch {
                        animateClick()
                    }
                }
                onClick()
            }
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Black
        )
    }
}

@Composable
fun CustomSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    onSeekChanged: (Float) -> Unit,
    onSeekFinished: (Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableStateOf<Float?>(null) }

    val density = LocalDensity.current

    val rangeStart = valueRange.start
    val rangeEnd = valueRange.endInclusive
    val rangeLength = rangeEnd - rangeStart

    val rawFraction = if (rangeLength == 0f || value.isNaN()) {
        0f
    } else {
        ((value - rangeStart) / rangeLength).coerceIn(0f, 1f)
    }

    val animationSpec: AnimationSpec<Float> = when {
        isDragging -> snap()
        else -> tween(
            durationMillis = 180,
            easing = LinearEasing
        )
    }

    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = animationSpec
    )

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.4f else 1f
    )

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val newFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val newValue = rangeStart + rangeLength * newFraction

                    dragValue = null
                    onSeekFinished(newValue)
                }
            }
            .pointerInput(valueRange) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragValue = value
                    },
                    onDragEnd = {
                        if (isDragging) {
                            isDragging = false
                            onSeekFinished(dragValue ?: value)
                            dragValue = null
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        dragValue = null
                    }
                ) { change, _ ->
                    change.consume()
                    val newFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val newValue = rangeStart + rangeLength * newFraction

                    dragValue = newValue
                    onSeekChanged(newValue)
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val thumbRadius = 6.dp
        val thumbRadiusPx = with(density) { thumbRadius.toPx() }
        val thumbTranslationX = animatedFraction * widthPx - thumbRadiusPx

        // Base Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.CenterStart)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
        )

        // Active Track
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedFraction)
                .height(4.dp)
                .align(Alignment.CenterStart)
                .background(Color.White, RoundedCornerShape(2.dp))
        )

        // Thumb
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    translationX = thumbTranslationX
                    scaleX = thumbScale
                    scaleY = thumbScale
                }
                .size(12.dp)
                .background(Color.White, CircleShape)
        )
    }
}

@Preview
@Composable
private fun MediaPlaybackControllerPreview() {
    MediaPlaybackTheme {
        MediaPlaybackController(
            currentMs = 10L,
            totalMs = 20L,
            playbackStatus = PlaybackStatus.PLAYING,
            valueRange = 0f..20f,
            onAction = {}
        )
    }
}