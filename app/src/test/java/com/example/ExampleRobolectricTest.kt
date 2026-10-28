package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.ArtifactEntity
import com.example.data.model.BadgeEntity
import com.example.data.model.LitterRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Beach Cleanup", appName)
    }

    @Test
    fun `room database tracks litter count and weight`() = runBlocking {
        val dao = db.beachDao()

        dao.insertLitterRecord(
            LitterRecordEntity(
                litterType = "PLASTIC_BOTTLE",
                zone = "SHORELINE",
                pointsEarned = 15,
                weightKg = 0.045f
            )
        )
        dao.insertLitterRecord(
            LitterRecordEntity(
                litterType = "GHOST_NET",
                zone = "ROCKY_COVE",
                pointsEarned = 60,
                weightKg = 0.850f
            )
        )

        val count = dao.getTotalLitterCount().first()
        val weight = dao.getTotalLitterWeight().first()

        assertEquals(2, count)
        assertEquals(0.895f, weight, 0.001f)
    }

    @Test
    fun `room database stores and unlocks badges and artifacts`() = runBlocking {
        val dao = db.beachDao()

        val artifact = ArtifactEntity(
            id = "art_test_shell",
            name = "Test Nautilus Shell",
            scientificName = "Nautilus test",
            zoneFound = "ROCKY_COVE",
            description = "Test description",
            conservationLesson = "Test conservation lesson",
            ancientOrigin = "Test origin",
            iconEmoji = "🐚"
        )
        dao.insertArtifacts(listOf(artifact))

        val badge = BadgeEntity(
            id = "badge_test_ranger",
            name = "Test Ranger",
            title = "Apprentice",
            requirement = "Clean test beach",
            rarity = "Common",
            iconEmoji = "🌊"
        )
        dao.insertBadges(listOf(badge))

        dao.markArtifactDiscovered("art_test_shell")
        dao.unlockBadge("badge_test_ranger")

        val discovered = dao.getDiscoveredArtifacts().first()
        val unlockedBadges = dao.getUnlockedBadges().first()

        assertEquals(1, discovered.size)
        assertEquals("art_test_shell", discovered[0].id)
        assertTrue(discovered[0].isDiscovered)

        assertEquals(1, unlockedBadges.size)
        assertEquals("badge_test_ranger", unlockedBadges[0].id)
        assertTrue(unlockedBadges[0].isUnlocked)
    }
}
