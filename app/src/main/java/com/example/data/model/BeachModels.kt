package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LitterCategory(val label: String) {
    PLASTIC("Plastics"),
    METAL("Metals"),
    FOAM("Styrofoam"),
    TOXIC("Hazardous"),
    FISHING_GEAR("Ghost Gear")
}

enum class LitterType(
    val displayName: String,
    val points: Int,
    val weightKg: Float,
    val category: LitterCategory,
    val degradationYears: Int,
    val marineImpact: String,
    val emoji: String
) {
    PLASTIC_BOTTLE(
        displayName = "Plastic Beverage Bottle",
        points = 15,
        weightKg = 0.045f,
        category = LitterCategory.PLASTIC,
        degradationYears = 450,
        marineImpact = "Breaks down into toxic microplastics that enter the marine food web.",
        emoji = "🧴"
    ),
    SIX_PACK_RINGS(
        displayName = "Six-Pack Ring",
        points = 25,
        weightKg = 0.015f,
        category = LitterCategory.PLASTIC,
        degradationYears = 400,
        marineImpact = "Entangles seabirds, sea turtles, and fish, causing fatal asphyxiation.",
        emoji = "⭕"
    ),
    STYROFOAM_CUP(
        displayName = "Styrofoam Cup",
        points = 20,
        weightKg = 0.010f,
        category = LitterCategory.FOAM,
        degradationYears = 500,
        marineImpact = "Crumples into buoyant polystyrene beads mistaken for fish eggs.",
        emoji = "☕"
    ),
    ALUMINUM_CAN(
        displayName = "Crushed Soda Can",
        points = 10,
        weightKg = 0.014f,
        category = LitterCategory.METAL,
        degradationYears = 200,
        marineImpact = "Sharp edges cut marine wildlife and leach heavy metals into tidepools.",
        emoji = "🥫"
    ),
    GHOST_NET(
        displayName = "Abandoned Ghost Net",
        points = 60,
        weightKg = 0.850f,
        category = LitterCategory.FISHING_GEAR,
        degradationYears = 600,
        marineImpact = "Drifts indefinitely, trapping whales, rays, turtles, and tearing corals.",
        emoji = "🕸️"
    ),
    MICROPLASTICS(
        displayName = "Microplastic Granules",
        points = 30,
        weightKg = 0.005f,
        category = LitterCategory.PLASTIC,
        degradationYears = 1000,
        marineImpact = "Absorbs PCBs and pesticides, ingested by plankton, bioaccumulating in seafood.",
        emoji = "✨"
    ),
    CIGARETTE_BUTT(
        displayName = "Toxic Cigarette Filter",
        points = 12,
        weightKg = 0.003f,
        category = LitterCategory.TOXIC,
        degradationYears = 10,
        marineImpact = "Contains cellulose acetate and leaches nicotine, arsenic, and lead into water.",
        emoji = "🚬"
    ),
    PLASTIC_BAG(
        displayName = "Single-Use Plastic Bag",
        points = 20,
        weightKg = 0.008f,
        category = LitterCategory.PLASTIC,
        degradationYears = 300,
        marineImpact = "Resembles floating jellyfish, leading sea turtles to ingest and suffocate.",
        emoji = "🛍️"
    ),
    FLIP_FLOP(
        displayName = "Discarded Rubber Flip-Flop",
        points = 22,
        weightKg = 0.180f,
        category = LitterCategory.FOAM,
        degradationYears = 80,
        marineImpact = "EVA foam sheds toxic particles and suffocates coastal crab burrows.",
        emoji = "🩴"
    )
}

enum class BeachZone(
    val title: String,
    val subtitle: String,
    val description: String,
    val iconEmoji: String
) {
    SHORELINE(
        title = "Sunlit Shoreline",
        subtitle = "Open High-Tide Sand",
        description = "Where fresh ocean flotsam and plastic bottles wash ashore daily.",
        iconEmoji = "🏖️"
    ),
    TIDEPOOL(
        title = "Coral Tidepool",
        subtitle = "Intertidal Shallow Reef",
        description = "Sensitive nurseries vulnerable to six-pack rings and microplastics.",
        iconEmoji = "🪸"
    ),
    DUNES(
        title = "Coastal Dunes",
        subtitle = "Nesting Reserve",
        description = "Grassy dunes where sea turtles lay eggs and cigarette litter hides.",
        iconEmoji = "🌾"
    ),
    ROCKY_COVE(
        title = "Deep Rocky Cove",
        subtitle = "Wave Surge Trench",
        description = "Heavy debris vortex catching abandoned ghost nets and ropes.",
        iconEmoji = "🧗"
    )
}

@Entity(tableName = "litter_records")
data class LitterRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val litterType: String,
    val zone: String,
    val pointsEarned: Int,
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetType: String, // "ALL", "PLASTIC", "GHOST_NET", "MICROPLASTICS", "WEIGHT_KG"
    val targetAmount: Int,
    val currentAmount: Int = 0,
    val rewardBadgeId: String,
    val rewardPoints: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

@Entity(tableName = "artifacts")
data class ArtifactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val scientificName: String,
    val zoneFound: String,
    val description: String,
    val conservationLesson: String,
    val ancientOrigin: String,
    val isDiscovered: Boolean = false,
    val discoveredAt: Long? = null,
    val iconEmoji: String
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val title: String,
    val requirement: String,
    val rarity: String, // "Common", "Rare", "Epic", "Legendary"
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey val id: String,
    val rank: Int,
    val username: String,
    val countryTag: String,
    val totalKgCleaned: Float,
    val missionsCompleted: Int,
    val badgesCount: Int,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "EcoWave Ranger",
    val countryTag: String = "🌊",
    val totalPoints: Int = 0,
    val totalKgCleaned: Float = 0f,
    val totalItemsCleaned: Int = 0,
    val currentStreak: Int = 1,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)
