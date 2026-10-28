package com.example.game

import com.example.data.model.BeachZone
import com.example.data.model.LitterType

enum class CleanupTool(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val bonusTarget: String,
    val multiplier: Int
) {
    ECO_GRABBER(
        title = "Eco-Grabber",
        description = "Standard tongs to pick bottles and cans without bending.",
        iconEmoji = "🦾",
        bonusTarget = "PLASTIC",
        multiplier = 1
    ),
    SAND_SIFTER(
        title = "Sand Sifter",
        description = "Fine mesh sieve separating toxic microplastics from sand.",
        iconEmoji = "🥢",
        bonusTarget = "MICROPLASTICS",
        multiplier = 2
    ),
    NET_CUTTER(
        title = "Ghost Net Blade",
        description = "Serrated emergency hook to safely slice entangled nets.",
        iconEmoji = "✂️",
        bonusTarget = "GHOST_NET",
        multiplier = 2
    )
}

data class ActiveBeachItem(
    val id: String,
    val isArtifact: Boolean,
    val litterType: LitterType? = null,
    val artifactId: String? = null,
    val xPercent: Float, // 0.08f to 0.88f
    val yPercent: Float, // 0.12f to 0.82f
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val isCollected: Boolean = false,
    val isBonusGolden: Boolean = false
)

data class FloatingScoreText(
    val id: Long,
    val text: String,
    val xPercent: Float,
    val yPercent: Float,
    val isBonus: Boolean = false
)
