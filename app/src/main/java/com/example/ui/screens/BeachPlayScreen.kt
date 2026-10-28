package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BeachZone
import com.example.game.CleanupTool
import com.example.ui.components.BeachCanvasView
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GoldenSand
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SeafoamGreen
import com.example.ui.viewmodel.BeachGameViewModel

@Composable
fun BeachPlayScreen(
    viewModel: BeachGameViewModel,
    modifier: Modifier = Modifier
) {
    val currentZone by viewModel.currentZone.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    val activeItems by viewModel.activeBeachItems.collectAsStateWithLifecycle()
    val floatingScores by viewModel.floatingScores.collectAsStateWithLifecycle()
    val isTideFrenzy by viewModel.isTideFrenzy.collectAsStateWithLifecycle()
    val tideSeconds by viewModel.tideTimeLeftSeconds.collectAsStateWithLifecycle()
    val comboStreak by viewModel.comboStreak.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val missions by viewModel.missions.collectAsStateWithLifecycle()
    val activeMission = missions.firstOrNull { !it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("beach_play_screen")
    ) {
        // TOP BAR: Player Stats & Audio/Tactile Quick Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Level / Stats badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = OceanBluePrimary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userProfile?.countryTag ?: "🌊",
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = userProfile?.username ?: "EcoWave Ranger",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${userProfile?.totalPoints ?: 0} pts",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = GoldenSand
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• %.2f kg cleaned".format(userProfile?.totalKgCleaned ?: 0f),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Audio & Haptic Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.toggleAudio() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_sfx_button")
                ) {
                    Icon(
                        imageVector = if (viewModel.audioEngine.isSfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Toggle SFX",
                        tint = if (viewModel.audioEngine.isSfxEnabled) OceanBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleMusic() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_music_button")
                ) {
                    Icon(
                        imageVector = if (viewModel.audioEngine.isMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = "Toggle Ocean Music",
                        tint = if (viewModel.audioEngine.isMusicEnabled) SeafoamGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleHaptics() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_haptics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Toggle Haptics",
                        tint = if (viewModel.audioEngine.isHapticsEnabled) GoldenSand else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ZONE SELECTOR ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BeachZone.values().forEach { zone ->
                val isSelected = currentZone == zone
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) OceanBluePrimary else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) OceanBluePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .clickable { viewModel.selectZone(zone) }
                        .testTag("zone_button_${zone.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = zone.iconEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = zone.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LIVE COMBO & HIGH TIDE STATUS BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Combo multiplier pill
            AnimatedVisibility(visible = comboStreak > 1, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldenSand.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldenSand)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${comboStreak}x Streak (+${(comboStreak / 3) + 1}x Multiplier)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldenSand
                        )
                    }
                }
            }

            // Incoming Tide Event Trigger
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isTideFrenzy) CoralRed else SeafoamGreen.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isTideFrenzy) CoralRed else SeafoamGreen
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.triggerTideFrenzy() }
                    .testTag("trigger_tide_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isTideFrenzy) Icons.Default.ElectricBolt else Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = if (isTideFrenzy) Color.White else OceanBluePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTideFrenzy) "High Tide: ${tideSeconds}s" else "Summon Tide (2x Bonus)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isTideFrenzy) Color.White else OceanBlueDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // INTERACTIVE BEACH CANVAS (Main Game Area)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BeachCanvasView(
                zone = currentZone,
                items = activeItems,
                floatingScores = floatingScores,
                isTideFrenzy = isTideFrenzy,
                onItemClicked = { viewModel.onBeachItemClicked(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CLEANUP TOOL SELECTOR
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "SELECT CLEANUP TOOL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CleanupTool.values().forEach { tool ->
                        val isSelected = selectedTool == tool
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) OceanBluePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) OceanBluePrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.selectTool(tool) }
                                .testTag("tool_${tool.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = tool.iconEmoji, fontSize = 20.sp)
                                Text(
                                    text = tool.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) OceanBluePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (tool.multiplier > 1) "${tool.multiplier}x ${tool.bonusTarget.take(7)}" else "Standard",
                                    fontSize = 9.sp,
                                    color = if (tool.multiplier > 1) GoldenSand else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ACTIVE MISSION TRACKER BANNER
        if (activeMission != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_mission_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎯", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = activeMission.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeMission.currentAmount}/${activeMission.targetAmount}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val progress = (activeMission.currentAmount.toFloat() / activeMission.targetAmount).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SeafoamGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
