package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ArtifactEntity
import com.example.data.model.BadgeEntity
import com.example.data.model.LeaderboardEntryEntity
import com.example.data.model.LitterRecordEntity
import com.example.data.model.MissionEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeachDao {

    // --- LITTER RECORDS ---
    @Insert
    suspend fun insertLitterRecord(record: LitterRecordEntity): Long

    @Query("SELECT * FROM litter_records ORDER BY timestamp DESC LIMIT 30")
    fun getRecentLitterRecords(): Flow<List<LitterRecordEntity>>

    @Query("SELECT COUNT(*) FROM litter_records")
    fun getTotalLitterCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(weightKg), 0.0) FROM litter_records")
    fun getTotalLitterWeight(): Flow<Float>

    // --- MISSIONS ---
    @Query("SELECT * FROM missions ORDER BY id ASC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Query("SELECT * FROM missions WHERE id = :id LIMIT 1")
    suspend fun getMissionById(id: String): MissionEntity?

    // --- ARTIFACTS ---
    @Query("SELECT * FROM artifacts ORDER BY id ASC")
    fun getAllArtifacts(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM artifacts WHERE isDiscovered = 1 ORDER BY discoveredAt DESC")
    fun getDiscoveredArtifacts(): Flow<List<ArtifactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtifacts(artifacts: List<ArtifactEntity>)

    @Query("UPDATE artifacts SET isDiscovered = 1, discoveredAt = :timestamp WHERE id = :id")
    suspend fun markArtifactDiscovered(id: String, timestamp: Long = System.currentTimeMillis())

    // --- BADGES ---
    @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, id ASC")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedBadges(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Query("UPDATE badges SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockBadge(id: String, timestamp: Long = System.currentTimeMillis())

    // --- LEADERBOARD ---
    @Query("SELECT * FROM leaderboard_entries ORDER BY totalKgCleaned DESC")
    fun getLeaderboard(): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntryEntity>)

    @Update
    suspend fun updateLeaderboardEntry(entry: LeaderboardEntryEntity)

    // --- USER PROFILE ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)
}
