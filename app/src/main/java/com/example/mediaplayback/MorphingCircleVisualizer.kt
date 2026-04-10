package com.example.mediaplayback

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.abs

@Immutable
private data class DentSpec(
    val baseAngle: Float,
    val width: Float,
    val amplitude: Float,
    val speed: Float,
    val phase: Float,
    val drift: Float
)

@Composable
fun CustomAnimation(
    modifier: Modifier = Modifier,
    circleSize: Dp = 180.dp,
    pointCount: Int = 180,
    dentCount: Int = 6,
    ringColor: Color = Color(0xFFB8A6FF),
    glowColor: Color = Color(0xFF8B5CF6),
    speedMultiplier: Float = 4f,
) {
    val dents = remember {
        List(dentCount) { index ->
            val base = index + 1
            DentSpec(
                baseAngle = (((base * 73f) % 360f) / 180f * PI).toFloat(),
                width = 0.22f + ((base * 13f) % 16f) / 100f,
                amplitude = 0.16f + ((base * 9f) % 5f) * 0.03f,
                speed = 1.8f + ((base * 5f) % 5f) * 0.45f,
                phase = (((base * 97f) % 360f) / 180f * PI).toFloat(),
                drift = 0.18f + ((base * 19f) % 8f) * 0.035f
            )
        }
    }

    var timeSeconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var startTimeNanos = 0L
        while (true) {
            withFrameNanos { frameTime ->
                if (startTimeNanos == 0L) startTimeNanos = frameTime
                timeSeconds = (frameTime - startTimeNanos) / 1_000_000_000f * speedMultiplier
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(circleSize)
        ) {
            val center = this.center
            val baseRadius = kotlin.math.min(this.size.width, this.size.height) * 0.36f

            val path = buildMorphingCirclePath(
                center = center,
                baseRadius = baseRadius,
                pointCount = pointCount,
                dents = dents,
                time = timeSeconds
            )

            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.22f),
                        glowColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.55f
                ),
                blendMode = BlendMode.Plus
            )

            drawPath(
                path = path,
                color = glowColor.copy(alpha = 0.28f),
                style = Stroke(width = 22f)
            )

            drawPath(
                path = path,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ringColor.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.95f),
                        ringColor.copy(alpha = 0.85f),
                        glowColor.copy(alpha = 0.95f),
                        ringColor.copy(alpha = 0.95f)
                    ),
                    center = center
                ),
                style = Stroke(width = 7f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 0.95f
                ),
                radius = baseRadius * 0.95f,
                center = center
            )
        }
    }
}

private fun buildMorphingCirclePath(
    center: Offset,
    baseRadius: Float,
    pointCount: Int,
    dents: List<DentSpec>,
    time: Float
): Path {
    val points = ArrayList<Offset>(pointCount)

    for (i in 0 until pointCount) {
        val angle = (i.toFloat() / pointCount.toFloat()) * 2f * PI.toFloat()

        var inwardOffset = 0f
        var outwardOffset = 0f

        dents.forEach { dent ->
            val movingCenter =
                dent.baseAngle +
                        sin(time * dent.drift + dent.phase) * 1.2f +
                        sin(time * dent.drift * 0.43f + dent.phase * 1.7f) * 0.55f

            val strengthPulse =
                0.55f +
                        0.30f * sin(time * dent.speed + dent.phase) +
                        0.15f * sin(time * dent.speed * 1.73f + dent.phase * 1.31f)

            val activeStrength = (strengthPulse.coerceAtLeast(0f)) * dent.amplitude * baseRadius

            val dynamicWidth =
                dent.width *
                        (
                                0.85f +
                                        0.25f * ((sin(time * dent.speed * 0.62f + dent.phase) + 1f) / 2f)
                                )

            val angularDistance = shortestAngularDistance(angle, normalizeAngle(movingCenter))
            val gaussian = exp(
                -0.5f * (angularDistance / dynamicWidth) * (angularDistance / dynamicWidth)
            )

            inwardOffset += activeStrength * gaussian
        }

        outwardOffset += sin(angle * 4f - time * 2.8f) * baseRadius * 0.028f
        outwardOffset += sin(angle * 9f + time * 3.6f) * baseRadius * 0.016f

        val microWave =
            sin(angle * 7f + time * 3.1f) * baseRadius * 0.020f +
                    sin(angle * 13f - time * 2.4f) * baseRadius * 0.014f +
                    sin(angle * 17f + time * 1.7f) * baseRadius * 0.010f

        val radius = (baseRadius - inwardOffset + outwardOffset + microWave)
            .coerceAtLeast(baseRadius * 0.48f)

        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        points.add(Offset(x, y))
    }

    return smoothClosedPath(points)
}

private fun normalizeAngle(angle: Float): Float {
    val twoPi = 2f * PI.toFloat()
    var result = angle % twoPi
    if (result < 0f) result += twoPi
    return result
}

private fun smoothClosedPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    val size = points.size
    path.moveTo(points[0].x, points[0].y)

    for (i in points.indices) {
        val p0 = points[(i - 1 + size) % size]
        val p1 = points[i]
        val p2 = points[(i + 1) % size]
        val p3 = points[(i + 2) % size]

        val c1 = Offset(
            x = p1.x + (p2.x - p0.x) / 6f,
            y = p1.y + (p2.y - p0.y) / 6f
        )
        val c2 = Offset(
            x = p2.x - (p3.x - p1.x) / 6f,
            y = p2.y - (p3.y - p1.y) / 6f
        )

        path.cubicTo(
            c1.x, c1.y,
            c2.x, c2.y,
            p2.x, p2.y
        )
    }

    path.close()
    return path
}

private fun shortestAngularDistance(a: Float, b: Float): Float {
    var diff = abs(a - b) % (2f * PI.toFloat())
    if (diff > PI) diff = 2f * PI.toFloat() - diff
    return diff
}