package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BeachDao
import com.example.data.model.ArtifactEntity
import com.example.data.model.BadgeEntity
import com.example.data.model.LeaderboardEntryEntity
import com.example.data.model.LitterRecordEntity
import com.example.data.model.MissionEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        LitterRecordEntity::class,
        MissionEntity::class,
        ArtifactEntity::class,
        BadgeEntity::class,
        LeaderboardEntryEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun beachDao(): BeachDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beach_cleanup_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default data in background coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).beachDao()
                            seedInitialData(dao)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(dao: BeachDao) {
            // Seed Missions
            val initialMissions = listOf(
                MissionEntity(
                    id = "mission_1",
                    title = "Shoreline Cleanup Kickoff",
                    description = "Collect 8 plastic bottles & cans washing up on Sunlit Shoreline.",
                    targetType = "PLASTIC",
                    targetAmount = 8,
                    rewardBadgeId = "badge_first_wave",
                    rewardPoints = 150
                ),
                MissionEntity(
                    id = "mission_2",
                    title = "Turtle Nesting Shield",
                    description = "Remove 12 hazardous items from Coastal Dunes to protect nesting sea turtles.",
                    targetType = "DUNES_ITEMS",
                    targetAmount = 12,
                    rewardBadgeId = "badge_turtle_savior",
                    rewardPoints = 250
                ),
                MissionEntity(
                    id = "mission_3",
                    title = "Ghost Gear Rescue",
                    description = "Locate and cut free 3 abandoned ghost fishing nets in Deep Rocky Cove.",
                    targetType = "GHOST_NET",
                    targetAmount = 3,
                    rewardBadgeId = "badge_ghost_buster",
                    rewardPoints = 350
                ),
                MissionEntity(
                    id = "mission_4",
                    title = "Microplastic Sifting",
                    description = "Sift and remove 15 microplastic granules from the Coral Tidepools.",
                    targetType = "MICROPLASTICS",
                    targetAmount = 15,
                    rewardBadgeId = "badge_microplastic_master",
                    rewardPoints = 300
                ),
                MissionEntity(
                    id = "mission_5",
                    title = "Grand Coastal Blitz",
                    description = "Collect a cumulative 5.0 kg of marine debris across all coastal zones.",
                    targetType = "WEIGHT_KG",
                    targetAmount = 5,
                    rewardBadgeId = "badge_clean_wave_legend",
                    rewardPoints = 600
                )
            )
            dao.insertMissions(initialMissions)

            // Seed Badges
            val initialBadges = listOf(
                BadgeEntity(
                    id = "badge_first_wave",
                    name = "First Wave Ranger",
                    title = "Coastal Apprentice",
                    requirement = "Complete 'Shoreline Cleanup Kickoff' by collecting 8 plastic items.",
                    rarity = "Common",
                    iconEmoji = "🌊"
                ),
                BadgeEntity(
                    id = "badge_turtle_savior",
                    name = "Sea Turtle Defender",
                    title = "Hatchling Guardian",
                    requirement = "Complete 'Turtle Nesting Shield' by clearing dune nesting grounds.",
                    rarity = "Rare",
                    iconEmoji = "🐢"
                ),
                BadgeEntity(
                    id = "badge_ghost_buster",
                    name = "Ghost Gear Eliminator",
                    title = "Reef Liberator",
                    requirement = "Complete 'Ghost Gear Rescue' by extracting 3 drifting ghost nets.",
                    rarity = "Epic",
                    iconEmoji = "🕸️"
                ),
                BadgeEntity(
                    id = "badge_microplastic_master",
                    name = "Microplastic Sleuth",
                    title = "Tidepool Chemist",
                    requirement = "Complete 'Microplastic Sifting' by gathering 15 micro-fragments.",
                    rarity = "Rare",
                    iconEmoji = "🔬"
                ),
                BadgeEntity(
                    id = "badge_artifact_archivist",
                    name = "Ocean Antiquarian",
                    title = "Master Naturalist",
                    requirement = "Discover 4 ancient coastal artifacts while cleaning the beach.",
                    rarity = "Epic",
                    iconEmoji = "🏛️"
                ),
                BadgeEntity(
                    id = "badge_clean_wave_legend",
                    name = "Apex Ocean Champion",
                    title = "Legend of the Seven Seas",
                    requirement = "Collect over 5.0 kg of coastal trash and achieve global renown.",
                    rarity = "Legendary",
                    iconEmoji = "🏆"
                )
            )
            dao.insertBadges(initialBadges)

            // Seed Educational Ocean Conservation Artifacts
            val initialArtifacts = listOf(
                ArtifactEntity(
                    id = "art_turtle_trace",
                    name = "Hawksbill Turtle Shell Fragment",
                    scientificName = "Eretmochelys imbricata",
                    zoneFound = "DUNES",
                    description = "A smooth keratin scute shed naturally by a nesting sea turtle.",
                    conservationLesson = "Critically endangered hawksbill turtles feed almost exclusively on sponges and jellyfish. Floating plastic bags mimic jellyfish shape perfectly, causing severe intestinal impaction and starvation. Clearing beach trash saves turtle generations.",
                    ancientOrigin = "Sea turtles have navigated Earth's magnetic currents for over 110 million years.",
                    iconEmoji = "🐢"
                ),
                ArtifactEntity(
                    id = "art_nautilus",
                    name = "Chambered Nautilus Shell",
                    scientificName = "Nautilus pompilius",
                    zoneFound = "ROCKY_COVE",
                    description = "A spiral calcified shell showcasing sacred logarithmic geometry.",
                    conservationLesson = "Nautiluses survive in deep twilight ocean trenches. Ocean acidification from carbon emissions weakens their shell calcification, while drifting marine debris traps them during nightly vertical migrations.",
                    ancientOrigin = "Unchanged living fossils swimming through prehistoric oceans for 500 million years.",
                    iconEmoji = "🐚"
                ),
                ArtifactEntity(
                    id = "art_sea_glass",
                    name = "Tumbled Cobalt Sea Glass",
                    scientificName = "Hydrated Beach Silica",
                    zoneFound = "SHORELINE",
                    description = "Vintage bottle glass softened into frosted gemstone by 60 years of wave surf.",
                    conservationLesson = "Glass takes approximately 1,000,000 years to decompose naturally, yet its silica composition is far safer than non-biodegradable polymers that shed endocrine-disrupting microplastics.",
                    ancientOrigin = "Likely from an early 20th-century coastal fishing vessel or apothecary bottle.",
                    iconEmoji = "💎"
                ),
                ArtifactEntity(
                    id = "art_shark_tooth",
                    name = "Fossilized Shark Tooth",
                    scientificName = "Otodus megalodon",
                    zoneFound = "SHORELINE",
                    description = "A serrated, mineralized tooth weathered out of submarine cliffs.",
                    conservationLesson = "Sharks are essential apex predators that cull sick organisms and maintain coral health. Commercial fishing nets kill over 100 million sharks annually as discarded bycatch.",
                    ancientOrigin = "Megalodon apex predator tooth dating back 3.6 to 23 million years ago.",
                    iconEmoji = "🦈"
                ),
                ArtifactEntity(
                    id = "art_driftwood",
                    name = "Polynesian Driftwood Talisman",
                    scientificName = "Carved Coastal Cedar",
                    zoneFound = "DUNES",
                    description = "Intricately carved ocean symbol preserved in saltwater for decades.",
                    conservationLesson = "Traditional coastal cultures revered ocean balance through 'kaitiakitanga' (ocean guardianship). Restoring indigenous coastal reverence helps reverse modern throwaway consumerism.",
                    ancientOrigin = "Hand-carved navigation relic swept thousands of miles by equatorial currents.",
                    iconEmoji = "🪵"
                ),
                ArtifactEntity(
                    id = "art_coral_polyp",
                    name = "Bioluminescent Coral Branch",
                    scientificName = "Gorgonacea noctiluca",
                    zoneFound = "TIDEPOOL",
                    description = "A deep-water sea fan branch that emits an ethereal azure glow.",
                    conservationLesson = "Reefs host 25% of all marine life. Ghost nets drag along shallow reefs, violently snapping fragile century-old coral branches and triggering widespread colony collapse.",
                    ancientOrigin = "Harvested from a submarine canyon drop-off during storm surges.",
                    iconEmoji = "🪸"
                ),
                ArtifactEntity(
                    id = "art_horseshoe_crab",
                    name = "Horseshoe Crab Carapace",
                    scientificName = "Limulus polyphemus",
                    zoneFound = "TIDEPOOL",
                    description = "The naturally molted exoskeleton of an ancient arthropod.",
                    conservationLesson = "Horseshoe crab eggs sustain millions of migrating shorebirds. Plastic waste on sand flats prevents females from digging nests, threatening migratory bird survival routes worldwide.",
                    ancientOrigin = "Pre-dates the earliest dinosaurs by over 200 million years.",
                    iconEmoji = "🦀"
                ),
                ArtifactEntity(
                    id = "art_ambergris",
                    name = "Pelagic Ambergris Sample",
                    scientificName = "Physeter macrocephalus",
                    zoneFound = "ROCKY_COVE",
                    description = "Wax-like aromatic compound cured by years of sun and sea foam.",
                    conservationLesson = "Created in the digestive tract of sperm whales. Whales store massive amounts of ocean carbon; ghost gear entanglements kill thousands of majestic cetaceans each year.",
                    ancientOrigin = "Prized natural treasure drifting across global ocean gyres.",
                    iconEmoji = "🐋"
                )
            )
            dao.insertArtifacts(initialArtifacts)

            // Seed Global Leaderboard with realistic worldwide eco-warriors
            val initialLeaderboard = listOf(
                LeaderboardEntryEntity(
                    id = "lb_1",
                    rank = 1,
                    username = "Kai Pacifica",
                    countryTag = "🌺 Hawaii",
                    totalKgCleaned = 18.42f,
                    missionsCompleted = 5,
                    badgesCount = 6,
                    isCurrentUser = false
                ),
                LeaderboardEntryEntity(
                    id = "lb_2",
                    rank = 2,
                    username = "Elena Costa",
                    countryTag = "🇵🇹 Portugal",
                    totalKgCleaned = 14.85f,
                    missionsCompleted = 5,
                    badgesCount = 5,
                    isCurrentUser = false
                ),
                LeaderboardEntryEntity(
                    id = "lb_3",
                    rank = 3,
                    username = "Yuki Reefkeeper",
                    countryTag = "🇯🇵 Okinawa",
                    totalKgCleaned = 12.30f,
                    missionsCompleted = 4,
                    badgesCount = 5,
                    isCurrentUser = false
                ),
                LeaderboardEntryEntity(
                    id = "lb_4",
                    rank = 4,
                    username = "Captain Moana",
                    countryTag = "🇫🇯 Fiji",
                    totalKgCleaned = 9.75f,
                    missionsCompleted = 4,
                    badgesCount = 4,
                    isCurrentUser = false
                ),
                LeaderboardEntryEntity(
                    id = "lb_5",
                    rank = 5,
                    username = "Marina Shore",
                    countryTag = "🇦🇺 Gold Coast",
                    totalKgCleaned = 6.80f,
                    missionsCompleted = 3,
                    badgesCount = 3,
                    isCurrentUser = false
                ),
                LeaderboardEntryEntity(
                    id = "lb_user",
                    rank = 6,
                    username = "EcoWave Ranger (You)",
                    countryTag = "🌊 Global",
                    totalKgCleaned = 0.0f,
                    missionsCompleted = 0,
                    badgesCount = 0,
                    isCurrentUser = true
                ),
                LeaderboardEntryEntity(
                    id = "lb_7",
                    rank = 7,
                    username = "Finnian Bay",
                    countryTag = "🇮🇪 Galway",
                    totalKgCleaned = 3.20f,
                    missionsCompleted = 2,
                    badgesCount = 2,
                    isCurrentUser = false
                ),
                LeaderboardEntryEntity(
                    id = "lb_8",
                    rank = 8,
                    username = "Sari Coral",
                    countryTag = "🇮🇩 Bali",
                    totalKgCleaned = 2.10f,
                    missionsCompleted = 1,
                    badgesCount = 2,
                    isCurrentUser = false
                )
            )
            dao.insertLeaderboard(initialLeaderboard)

            // Seed User Profile
            dao.insertUserProfile(
                UserProfileEntity(
                    id = 1,
                    username = "EcoWave Ranger",
                    countryTag = "🌊",
                    totalPoints = 0,
                    totalKgCleaned = 0f,
                    totalItemsCleaned = 0,
                    currentStreak = 1,
                    soundEnabled = true,
                    musicEnabled = true,
                    hapticsEnabled = true
                )
            )
        }
    }
}
