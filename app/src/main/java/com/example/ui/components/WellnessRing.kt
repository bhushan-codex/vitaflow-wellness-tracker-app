package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VitaPrimary
import com.example.ui.theme.VitaSecondary
import com.example.ui.theme.VitaSuccess

@Composable
fun WellnessRing(
    score: Int, // 0..100
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 14.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        val target = (score.coerceIn(0, 100) / 100f)
        animatedProgress.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("wellness_ring_box")
    ) {
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val primaryColor = VitaPrimary
        val secondaryColor = VitaSecondary
        val successColor = VitaSuccess

        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

            // Background Track
            drawArc(
                color = surfaceVariant.copy(alpha = 0.5f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = stroke
            )

            // Progress Arc
            val sweep = 260f * animatedProgress.value
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryColor,
                        0.5f to secondaryColor,
                        1.0f to successColor
                    ),
                    startAngle = 140f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = stroke
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "of 100",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Daily balance",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = VitaPrimary
            )
        }
    }
}
