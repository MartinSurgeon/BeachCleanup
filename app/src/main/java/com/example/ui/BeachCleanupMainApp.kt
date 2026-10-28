package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ArtifactDetailDialog
import com.example.ui.components.BadgeDetailDialog
import com.example.ui.components.MissionCompleteCelebration
import com.example.ui.screens.ArtifactSanctuaryScreen
import com.example.ui.screens.BeachPlayScreen
import com.example.ui.screens.GlobalLeaderboardScreen
import com.example.ui.screens.MissionsBadgesScreen
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SeafoamGreen
import com.example.ui.viewmodel.AppNavScreen
import com.example.ui.viewmodel.BeachGameViewModel

@Composable
fun BeachCleanupMainApp(
    viewModel: BeachGameViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val inspectedArtifact by viewModel.inspectedArtifact.collectAsStateWithLifecycle()
    val inspectedBadge by viewModel.inspectedBadge.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()

    // Manage ambient audio on app lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (currentScreen == AppNavScreen.PLAY) {
                    viewModel.audioEngine.startOceanAmbient()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.audioEngine.stopOceanAmbient()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.audioEngine.stopOceanAmbient()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                AppNavScreen.values().forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setScreen(screen) },
                        icon = {
                            Text(
                                text = screen.iconEmoji,
                                fontSize = if (selected) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OceanBluePrimary,
                            selectedTextColor = OceanBluePrimary,
                            indicatorColor = SeafoamGreen.copy(alpha = 0.2f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppNavScreen.PLAY -> BeachPlayScreen(viewModel = viewModel)
                    AppNavScreen.MISSIONS -> MissionsBadgesScreen(viewModel = viewModel)
                    AppNavScreen.SANCTUARY -> ArtifactSanctuaryScreen(viewModel = viewModel)
                    AppNavScreen.LEADERBOARD -> GlobalLeaderboardScreen(viewModel = viewModel)
                }
            }

            // Dialogs & Modals
            inspectedArtifact?.let { artifact ->
                ArtifactDetailDialog(
                    artifact = artifact,
                    onDismiss = { viewModel.dismissArtifactDialog() }
                )
            }

            inspectedBadge?.let { badge ->
                BadgeDetailDialog(
                    badge = badge,
                    onDismiss = { viewModel.dismissBadgeDialog() }
                )
            }

            celebration?.let { event ->
                MissionCompleteCelebration(
                    celebration = event,
                    onDismiss = { viewModel.dismissCelebration() }
                )
            }
        }
    }
}
