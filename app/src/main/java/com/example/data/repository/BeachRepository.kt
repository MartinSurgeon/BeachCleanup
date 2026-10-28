package com.example.data.repository

import com.example.data.dao.BeachDao
import com.example.data.model.ArtifactEntity
import com.example.data.model.BadgeEntity
import com.example.data.model.BeachZone
import com.example.data.model.LeaderboardEntryEntity
import com.example.data.model.LitterRecordEntity
import com.example.data.model.LitterType
import com.example.data.model.MissionEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class CollectionResult(
    val litterType: LitterType,
    val zone: BeachZone,
    val pointsEarned: Int,
    val weightKg: Float,
    val newlyCompletedMissions: List<MissionEntity>,
    val newlyUnlockedBadges: List<BadgeEntity>
)

class BeachRepository(private val dao: BeachDao) {

    val allMissions: Flow<List<MissionEntity>> = dao.getAllMissions()
    val allArtifacts: Flow<List<ArtifactEntity>> = dao.getAllArtifacts()
    val allBadges: Flow<List<BadgeEntity>> = dao.getAllBadges()
    val leaderboard: Flow<List<LeaderboardEntryEntity>> = dao.getLeaderboard()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val totalLitterCount: Flow<Int> = dao.getTotalLitterCount()
    val totalLitterWeight: Flow<Float> = dao.getTotalLitterWeight()

    /**
     * Records a litter pickup, updates profile, checks mission targets & badge criteria.
     */
    suspend fun recordLitterPickup(
        litterType: LitterType,
        zone: BeachZone,
        multiplier: Int = 1
    ): CollectionResult {
        val earnedPoints = litterType.points * multiplier
        val weight = litterType.weightKg

        // 1. Insert record
        dao.insertLitterRecord(
            LitterRecordEntity(
                litterType = litterType.name,
                zone = zone.name,
                pointsEarned = earnedPoints,
                weightKg = weight
            )
        )

        // 2. Fetch current profile or create default
        val currentProfile = dao.getUserProfile().firstOrNull() ?: UserProfileEntity()
        val updatedTotalPoints = currentProfile.totalPoints + earnedPoints
        val updatedTotalKg = currentProfile.totalKgCleaned + weight
        val updatedTotalItems = currentProfile.totalItemsCleaned + 1

        val updatedProfile = currentProfile.copy(
            totalPoints = updatedTotalPoints,
            totalKgCleaned = updatedTotalKg,
            totalItemsCleaned = updatedTotalItems
        )
        dao.updateUserProfile(updatedProfile)

        // 3. Process Active Missions
        val activeMissions = dao.getAllMissions().firstOrNull() ?: emptyList()
        val newlyCompleted = mutableListOf<MissionEntity>()
        val newlyUnlockedBadges = mutableListOf<BadgeEntity>()

        for (mission in activeMissions) {
            if (mission.isCompleted) continue

            var increment = 0
            when (mission.targetType) {
                "ALL" -> increment = 1
                "PLASTIC" -> {
                    if (litterType == LitterType.PLASTIC_BOTTLE ||
                        litterType == LitterType.SIX_PACK_RINGS ||
                        litterType == LitterType.PLASTIC_BAG ||
                        litterType == LitterType.ALUMINUM_CAN
                    ) {
                        increment = 1
                    }
                }
                "DUNES_ITEMS" -> {
                    if (zone == BeachZone.DUNES) {
                        increment = 1
                    }
                }
                "GHOST_NET" -> {
                    if (litterType == LitterType.GHOST_NET) {
                        increment = 1
                    }
                }
                "MICROPLASTICS" -> {
                    if (litterType == LitterType.MICROPLASTICS) {
                        increment = 1
                    }
                }
                "WEIGHT_KG" -> {
                    // Compares against total kg
                    if (updatedTotalKg >= mission.targetAmount) {
                        increment = mission.targetAmount
                    }
                }
            }

            val newCount = if (mission.targetType == "WEIGHT_KG") {
                updatedTotalKg.toInt().coerceAtLeast(mission.currentAmount + increment)
            } else {
                mission.currentAmount + increment
            }

            val isNowCompleted = newCount >= mission.targetAmount
            if (increment > 0 || isNowCompleted) {
                val updatedMission = mission.copy(
                    currentAmount = newCount,
                    isCompleted = isNowCompleted,
                    completedAt = if (isNowCompleted) System.currentTimeMillis() else null
                )
                dao.updateMission(updatedMission)

                if (isNowCompleted) {
                    newlyCompleted.add(updatedMission)
                    // Unlock associated badge
                    if (mission.rewardBadgeId.isNotEmpty()) {
                        dao.unlockBadge(mission.rewardBadgeId)
                        val allBadgesList = dao.getAllBadges().firstOrNull() ?: emptyList()
                        val unlockedBadge = allBadgesList.find { it.id == mission.rewardBadgeId }
                        if (unlockedBadge != null) {
                            newlyUnlockedBadges.add(unlockedBadge.copy(isUnlocked = true))
                        }
                    }
                }
            }
        }

        // 4. Check Artifact Archivist badge if 4+ artifacts discovered
        val discoveredArtifacts = dao.getDiscoveredArtifacts().firstOrNull() ?: emptyList()
        if (discoveredArtifacts.size >= 4) {
            val allBadgesList = dao.getAllBadges().firstOrNull() ?: emptyList()
            val archivistBadge = allBadgesList.find { it.id == "badge_artifact_archivist" }
            if (archivistBadge != null && !archivistBadge.isUnlocked) {
                dao.unlockBadge(archivistBadge.id)
                newlyUnlockedBadges.add(archivistBadge.copy(isUnlocked = true))
            }
        }

        // 5. Update user position on the Leaderboard
        updateUserLeaderboard(updatedProfile)

        return CollectionResult(
            litterType = litterType,
            zone = zone,
            pointsEarned = earnedPoints,
            weightKg = weight,
            newlyCompletedMissions = newlyCompleted,
            newlyUnlockedBadges = newlyUnlockedBadges
        )
    }

    suspend fun discoverArtifact(artifactId: String): ArtifactEntity? {
        val allArtifactsList = dao.getAllArtifacts().firstOrNull() ?: emptyList()
        val artifact = allArtifactsList.find { it.id == artifactId } ?: return null

        if (!artifact.isDiscovered) {
            dao.markArtifactDiscovered(artifactId)

            // Check if user now has 4+ discovered to unlock badge
            val discoveredCount = (dao.getDiscoveredArtifacts().firstOrNull()?.size ?: 0) + 1
            if (discoveredCount >= 4) {
                dao.unlockBadge("badge_artifact_archivist")
            }
        }
        return artifact.copy(isDiscovered = true, discoveredAt = System.currentTimeMillis())
    }

    private suspend fun updateUserLeaderboard(profile: UserProfileEntity) {
        val currentEntries = dao.getLeaderboard().firstOrNull() ?: return
        val allBadgesList = dao.getAllBadges().firstOrNull() ?: emptyList()
        val unlockedCount = allBadgesList.count { it.isUnlocked }
        val allMissionsList = dao.getAllMissions().firstOrNull() ?: emptyList()
        val completedMissionsCount = allMissionsList.count { it.isCompleted }

        val updatedList = currentEntries.map { entry ->
            if (entry.isCurrentUser) {
                entry.copy(
                    username = "${profile.username} (You)",
                    countryTag = profile.countryTag,
                    totalKgCleaned = profile.totalKgCleaned,
                    missionsCompleted = completedMissionsCount,
                    badgesCount = unlockedCount
                )
            } else {
                entry
            }
        }.sortedByDescending { it.totalKgCleaned }

        // Recalculate ranks dynamically
        val reranked = updatedList.mapIndexed { index, item ->
            item.copy(rank = index + 1)
        }
        dao.insertLeaderboard(reranked)
    }

    suspend fun updateUsername(newUsername: String, newCountryTag: String) {
        val currentProfile = dao.getUserProfile().firstOrNull() ?: UserProfileEntity()
        val updated = currentProfile.copy(username = newUsername, countryTag = newCountryTag)
        dao.updateUserProfile(updated)
        updateUserLeaderboard(updated)
    }

    suspend fun updateAudioSettings(sound: Boolean, music: Boolean, haptics: Boolean) {
        val currentProfile = dao.getUserProfile().firstOrNull() ?: UserProfileEntity()
        dao.updateUserProfile(
            currentProfile.copy(
                soundEnabled = sound,
                musicEnabled = music,
                hapticsEnabled = haptics
            )
        )
    }
}
