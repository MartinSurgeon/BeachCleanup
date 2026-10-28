package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeachZone
import com.example.game.ActiveBeachItem
import com.example.game.FloatingScoreText
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GoldenSand
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SeafoamGreen
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun BeachCanvasView(
    zone: BeachZone,
    items: List<ActiveBeachItem>,
    floatingScores: List<FloatingScoreText>,
    isTideFrenzy: Boolean,
    onItemClicked: (ActiveBeachItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean_waves")

    // Wave ebb & flow animation
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_surge"
    )

    val itemBobbing by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "item_bob"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .testTag("beach_canvas_area")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Background Coastal Rendering (Sky, Waves, Sand)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Zone-specific sand and water coloring
            val sandTopColor = when (zone) {
                BeachZone.SHORELINE -> Color(0xFFFDE68A)
                BeachZone.TIDEPOOL -> Color(0xFFE0F2FE)
                BeachZone.DUNES -> Color(0xFFFCD34D)
                BeachZone.ROCKY_COVE -> Color(0xFF94A3B8)
            }
            val sandBottomColor = when (zone) {
                BeachZone.SHORELINE -> Color(0xFFF59E0B)
                BeachZone.TIDEPOOL -> Color(0xFF38BDF8)
                BeachZone.DUNES -> Color(0xFFD97706)
                BeachZone.ROCKY_COVE -> Color(0xFF475569)
            }

            // Ocean water at top
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF005B7F),
                        Color(0xFF007A99),
                        Color(0xFF0EA5E9)
                    ),
                    startY = 0f,
                    endY = heightPx * 0.45f
                ),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(widthPx, heightPx * 0.45f)
            )

            // Beach Sand Gradient at bottom
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(sandTopColor, sandBottomColor),
                    startY = heightPx * 0.28f,
                    endY = heightPx
                ),
                topLeft = Offset(0f, heightPx * 0.28f),
                size = androidx.compose.ui.geometry.Size(widthPx, heightPx * 0.72f)
            )

            // Dynamic rolling wave surf line
            val waveY = heightPx * 0.30f + waveOffset
            val wavePath = Path().apply {
                moveTo(0f, waveY)
                val step = 40f
                var x = 0f
                while (x <= widthPx) {
                    val y = waveY + sin(x * 0.035f + waveOffset * 0.1f) * 12f
                    lineTo(x, y)
                    x += step
                }
                lineTo(widthPx, 0f)
                lineTo(0f, 0f)
                close()
            }

            // Translucent turquoise ocean wave
            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x990D9488),
                        Color(0xCC14B8A6),
                        Color(0xFFCCFBF1)
                    )
                )
            )

            // White seafoam shoreline crest
            val foamPath = Path().apply {
                val fY = waveY + 8f
                moveTo(0f, fY)
                var x = 0f
                while (x <= widthPx) {
                    val y = fY + sin(x * 0.05f + waveOffset * 0.12f) * 6f
                    lineTo(x, y)
                    x += 30f
                }
            }
            drawPath(
                path = foamPath,
                color = Color.White.copy(alpha = 0.85f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
            )

            // High tide frenzy golden particles / shimmer
            if (isTideFrenzy) {
                val goldBrush = Brush.radialGradient(
                    colors = listOf(Color(0x33F59E0B), Color(0x00F59E0B)),
                    center = Offset(widthPx * 0.5f, heightPx * 0.5f),
                    radius = widthPx * 0.7f
                )
                drawRect(brush = goldBrush)
            }
        }

        // Active Interactive Items Scattered on the Beach
        items.forEach { item ->
            if (!item.isCollected) {
                val xPos = (widthPx * item.xPercent).roundToInt()
                val yPos = ((heightPx * item.yPercent) + itemBobbing).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPos - 36, yPos - 36) }
                        .size(68.dp)
                        .scale(item.scale)
                        .rotate(item.rotation)
                        .clip(CircleShape)
                        .clickable { onItemClicked(item) }
                        .testTag("beach_item_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isArtifact) {
                        // Ancient Artifact styling (Glowing halo)
                        Surface(
                            modifier = Modifier
                                .size(58.dp)
                                .shadow(8.dp, CircleShape),
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(2.dp, GoldenSand)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⭐",
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                )
                                Text(
                                    text = when (item.artifactId) {
                                        "art_turtle_trace" -> "🐢"
                                        "art_nautilus" -> "🐚"
                                        "art_sea_glass" -> "💎"
                                        "art_shark_tooth" -> "🦈"
                                        "art_driftwood" -> "🪵"
                                        "art_coral_polyp" -> "🪸"
                                        "art_horseshoe_crab" -> "🦀"
                                        "art_ambergris" -> "🐋"
                                        else -> "✨"
                                    },
                                    fontSize = 28.sp
                                )
                            }
                        }
                    } else {
                        // Litter item styling
                        val litter = item.litterType
                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(4.dp, CircleShape),
                            shape = CircleShape,
                            color = if (item.isBonusGolden) Color(0xFFFEF08A) else Color.White.copy(alpha = 0.92f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (item.isBonusGolden) GoldenSand else Color(0xFFCBD5E1)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = litter?.emoji ?: "🗑️",
                                    fontSize = 26.sp
                                )
                                if (item.isBonusGolden) {
                                    Text(
                                        text = "2x",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldenSand,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Animated Scores / Feedback labels
        floatingScores.forEach { score ->
            val xPos = (widthPx * score.xPercent).roundToInt()
            val yPos = (heightPx * score.yPercent - 25f).roundToInt()

            Surface(
                modifier = Modifier
                    .offset { IntOffset(xPos - 50, yPos) }
                    .shadow(6.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = if (score.isBonus) GoldenSand else OceanBluePrimary
            ) {
                Text(
                    text = score.text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // High Tide Banner indicator at top right of canvas
        if (isTideFrenzy) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CoralRed,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "🌊 HIGH TIDE FRENZY 2X",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
