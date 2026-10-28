package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BeachAudioHapticsEngine
import com.example.data.AppDatabase
import com.example.data.model.ArtifactEntity
import com.example.data.model.BadgeEntity
import com.example.data.model.BeachZone
import com.example.data.model.LeaderboardEntryEntity
import com.example.data.model.LitterType
import com.example.data.model.MissionEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.BeachRepository
import com.example.game.ActiveBeachItem
import com.example.game.CleanupTool
import com.example.game.FloatingScoreText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class AppNavScreen(val label: String, val iconEmoji: String) {
    PLAY("Patrol", "🏖️"),
    MISSIONS("Missions", "🎯"),
    SANCTUARY("Sanctuary", "🐚"),
    LEADERBOARD("Ranks", "🏆")
}

data class CelebrationData(
    val mission: MissionEntity? = null,
    val badge: BadgeEntity? = null,
    val title: String,
    val message: String,
    val pointsAwarded: Int = 0
)

class BeachGameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = BeachRepository(db.beachDao())
    val audioEngine = BeachAudioHapticsEngine(application)

    // Navigation
    private val _currentScreen = MutableStateFlow(AppNavScreen.PLAY)
    val currentScreen: StateFlow<AppNavScreen> = _currentScreen.asStateFlow()

    // Zone & Tool
    private val _currentZone = MutableStateFlow(BeachZone.SHORELINE)
    val currentZone: StateFlow<BeachZone> = _currentZone.asStateFlow()

    private val _selectedTool = MutableStateFlow(CleanupTool.ECO_GRABBER)
    val selectedTool: StateFlow<CleanupTool> = _selectedTool.asStateFlow()

    // Interactive Game State
    private val _activeBeachItems = MutableStateFlow<List<ActiveBeachItem>>(emptyList())
    val activeBeachItems: StateFlow<List<ActiveBeachItem>> = _activeBeachItems.asStateFlow()

    private val _floatingScores = MutableStateFlow<List<FloatingScoreText>>(emptyList())
    val floatingScores: StateFlow<List<FloatingScoreText>> = _floatingScores.asStateFlow()

    // Tide Frenzy & Combo
    private val _isTideFrenzy = MutableStateFlow(false)
    val isTideFrenzy: StateFlow<Boolean> = _isTideFrenzy.asStateFlow()

    private val _tideTimeLeftSeconds = MutableStateFlow(0)
    val tideTimeLeftSeconds: StateFlow<Int> = _tideTimeLeftSeconds.asStateFlow()

    private val _comboStreak = MutableStateFlow(0)
    val comboStreak: StateFlow<Int> = _comboStreak.asStateFlow()

    private var comboResetJob: Job? = null
    private var tideFrenzyJob: Job? = null

    // Modals / Dialogs
    private val _inspectedArtifact = MutableStateFlow<ArtifactEntity?>(null)
    val inspectedArtifact: StateFlow<ArtifactEntity?> = _inspectedArtifact.asStateFlow()

    private val _inspectedBadge = MutableStateFlow<BadgeEntity?>(null)
    val inspectedBadge: StateFlow<BadgeEntity?> = _inspectedBadge.asStateFlow()

    private val _celebration = MutableStateFlow<CelebrationData?>(null)
    val celebration: StateFlow<CelebrationData?> = _celebration.asStateFlow()

    // Repository Flows
    val missions: StateFlow<List<MissionEntity>> = repository.allMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artifacts: StateFlow<List<ArtifactEntity>> = repository.allArtifacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<BadgeEntity>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntryEntity>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalLitterCount: StateFlow<Int> = repository.totalLitterCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalLitterWeight: StateFlow<Float> = repository.totalLitterWeight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init {
        // Initial setup and spawn items
        viewModelScope.launch {
            // Give Room database time to seed if first start
            delay(150)
            spawnItemsForZone(_currentZone.value)
            audioEngine.startOceanAmbient()
        }
    }

    fun setScreen(screen: AppNavScreen) {
        _currentScreen.value = screen
        if (screen == AppNavScreen.PLAY) {
            audioEngine.startOceanAmbient()
        } else {
            audioEngine.stopOceanAmbient()
        }
    }

    fun selectZone(zone: BeachZone) {
        if (_currentZone.value != zone) {
            _currentZone.value = zone
            audioEngine.playWaveSweep()
            audioEngine.vibrateLitterCollected()
            spawnItemsForZone(zone)
        }
    }

    fun selectTool(tool: CleanupTool) {
        _selectedTool.value = tool
        audioEngine.vibrateLitterCollected()
    }

    fun toggleAudio() {
        val newState = !audioEngine.isSfxEnabled
        audioEngine.isSfxEnabled = newState
        viewModelScope.launch {
            repository.updateAudioSettings(newState, audioEngine.isMusicEnabled, audioEngine.isHapticsEnabled)
        }
    }

    fun toggleMusic() {
        val newState = !audioEngine.isMusicEnabled
        audioEngine.isMusicEnabled = newState
        if (newState && _currentScreen.value == AppNavScreen.PLAY) {
            audioEngine.startOceanAmbient()
        } else {
            audioEngine.stopOceanAmbient()
        }
        viewModelScope.launch {
            repository.updateAudioSettings(audioEngine.isSfxEnabled, newState, audioEngine.isHapticsEnabled)
        }
    }

    fun toggleHaptics() {
        val newState = !audioEngine.isHapticsEnabled
        audioEngine.isHapticsEnabled = newState
        viewModelScope.launch {
            repository.updateAudioSettings(audioEngine.isSfxEnabled, audioEngine.isMusicEnabled, newState)
        }
    }

    /**
     * Taps an item on the beach
     */
    fun onBeachItemClicked(item: ActiveBeachItem) {
        if (item.isCollected) return

        // Mark as collected immediately in UI
        _activeBeachItems.value = _activeBeachItems.value.map {
            if (it.id == item.id) it.copy(isCollected = true) else it
        }

        if (item.isArtifact) {
            // Discovered a beach artifact!
            val artId = item.artifactId ?: return
            viewModelScope.launch {
                val discovered = repository.discoverArtifact(artId)
                if (discovered != null) {
                    audioEngine.playArtifactDiscovered()
                    audioEngine.vibrateArtifactFound()
                    _inspectedArtifact.value = discovered

                    addFloatingScore(
                        text = "⭐ Artifact Uncovered!",
                        x = item.xPercent,
                        y = item.yPercent,
                        isBonus = true
                    )
                }
            }
        } else {
            // Collected a litter item!
            val litterType = item.litterType ?: return
            handleLitterCollected(item, litterType)
        }

        // Check if all items in zone collected; auto replenish with fresh tide wave!
        val remaining = _activeBeachItems.value.count { !it.isCollected }
        if (remaining <= 1) {
            viewModelScope.launch {
                delay(800)
                audioEngine.playWaveSweep()
                spawnItemsForZone(_currentZone.value)
            }
        }
    }

    private fun handleLitterCollected(item: ActiveBeachItem, litterType: LitterType) {
        // Combo multiplier
        val newStreak = _comboStreak.value + 1
        _comboStreak.value = newStreak

        comboResetJob?.cancel()
        comboResetJob = viewModelScope.launch {
            delay(5000)
            _comboStreak.value = 0
        }

        // Tool multiplier check
        val tool = _selectedTool.value
        var toolMultiplier = 1
        if (tool == CleanupTool.SAND_SIFTER && litterType == LitterType.MICROPLASTICS) {
            toolMultiplier = 2
        } else if (tool == CleanupTool.NET_CUTTER && litterType == LitterType.GHOST_NET) {
            toolMultiplier = 2
        } else if (tool == CleanupTool.ECO_GRABBER && (litterType.category.name == "PLASTIC" || litterType.category.name == "METAL")) {
            toolMultiplier = 1
        }

        val tideMultiplier = if (_isTideFrenzy.value) 2 else 1
        val finalMultiplier = (1 + (newStreak / 4).coerceAtMost(3)) * toolMultiplier * tideMultiplier

        // Audio & Tactile feedback
        audioEngine.playLitterCollect(multiplier = (newStreak % 5) + 1)
        audioEngine.vibrateLitterCollected()

        // Floating points popup
        val earnedPts = litterType.points * finalMultiplier
        val comboTag = if (finalMultiplier > 1) " (${finalMultiplier}x Combo!)" else ""
        addFloatingScore(
            text = "+$earnedPts pts$comboTag",
            x = item.xPercent,
            y = item.yPercent,
            isBonus = finalMultiplier > 1
        )

        // Database & Mission checking
        viewModelScope.launch {
            val result = repository.recordLitterPickup(litterType, _currentZone.value, finalMultiplier)

            // Did this complete any missions?
            if (result.newlyCompletedMissions.isNotEmpty()) {
                val mission = result.newlyCompletedMissions.first()
                val badge = result.newlyUnlockedBadges.firstOrNull()

                audioEngine.playMissionCompleteMusic()
                audioEngine.vibrateMissionSuccess()

                _celebration.value = CelebrationData(
                    mission = mission,
                    badge = badge,
                    title = "Mission Completed!",
                    message = "${mission.title} successfully accomplished! ${mission.rewardPoints} bonus conservation points awarded.",
                    pointsAwarded = mission.rewardPoints
                )
            } else if (result.newlyUnlockedBadges.isNotEmpty()) {
                val badge = result.newlyUnlockedBadges.first()
                audioEngine.playBadgeUnlocked()
                audioEngine.vibrateMissionSuccess()

                _celebration.value = CelebrationData(
                    badge = badge,
                    title = "Digital Badge Earned!",
                    message = "You unlocked the '${badge.name}' (${badge.rarity}) badge for coastal protection!",
                    pointsAwarded = 200
                )
            }
        }
    }

    fun triggerTideFrenzy() {
        if (_isTideFrenzy.value) return
        _isTideFrenzy.value = true
        _tideTimeLeftSeconds.value = 25
        audioEngine.playWaveSweep()
        audioEngine.vibrateMissionSuccess()

        // Spawn bonus items
        spawnItemsForZone(_currentZone.value, isFrenzy = true)

        tideFrenzyJob?.cancel()
        tideFrenzyJob = viewModelScope.launch {
            while (_tideTimeLeftSeconds.value > 0) {
                delay(1000)
                _tideTimeLeftSeconds.value -= 1
            }
            _isTideFrenzy.value = false
        }
    }

    fun inspectArtifact(artifact: ArtifactEntity) {
        _inspectedArtifact.value = artifact
        audioEngine.playArtifactDiscovered()
    }

    fun dismissArtifactDialog() {
        _inspectedArtifact.value = null
    }

    fun inspectBadge(badge: BadgeEntity) {
        _inspectedBadge.value = badge
        audioEngine.playLitterCollect(3)
    }

    fun dismissBadgeDialog() {
        _inspectedBadge.value = null
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    fun updatePlayerProfile(name: String, tag: String) {
        viewModelScope.launch {
            repository.updateUsername(name, tag)
            audioEngine.vibrateLitterCollected()
        }
    }

    private fun addFloatingScore(text: String, x: Float, y: Float, isBonus: Boolean) {
        val score = FloatingScoreText(
            id = System.nanoTime(),
            text = text,
            xPercent = x,
            yPercent = y,
            isBonus = isBonus
        )
        _floatingScores.value = _floatingScores.value + score
        viewModelScope.launch {
            delay(1200)
            _floatingScores.value = _floatingScores.value.filter { it.id != score.id }
        }
    }

    private fun spawnItemsForZone(zone: BeachZone, isFrenzy: Boolean = false) {
        val count = if (isFrenzy) 10 else 7
        val items = mutableListOf<ActiveBeachItem>()

        // Zone-specific litter biases
        val possibleLitter = when (zone) {
            BeachZone.SHORELINE -> listOf(
                LitterType.PLASTIC_BOTTLE,
                LitterType.ALUMINUM_CAN,
                LitterType.PLASTIC_BAG,
                LitterType.FLIP_FLOP
            )
            BeachZone.TIDEPOOL -> listOf(
                LitterType.MICROPLASTICS,
                LitterType.SIX_PACK_RINGS,
                LitterType.STYROFOAM_CUP,
                LitterType.MICROPLASTICS
            )
            BeachZone.DUNES -> listOf(
                LitterType.CIGARETTE_BUTT,
                LitterType.PLASTIC_BAG,
                LitterType.STYROFOAM_CUP,
                LitterType.PLASTIC_BOTTLE
            )
            BeachZone.ROCKY_COVE -> listOf(
                LitterType.GHOST_NET,
                LitterType.ALUMINUM_CAN,
                LitterType.FLIP_FLOP,
                LitterType.GHOST_NET
            )
        }

        // Zone-specific artifact mapping
        val zoneArtifactMap = mapOf(
            BeachZone.SHORELINE to listOf("art_sea_glass", "art_shark_tooth"),
            BeachZone.TIDEPOOL to listOf("art_coral_polyp", "art_horseshoe_crab"),
            BeachZone.DUNES to listOf("art_turtle_trace", "art_driftwood"),
            BeachZone.ROCKY_COVE to listOf("art_nautilus", "art_ambergris")
        )

        // Artifact spawn chance (about 1 in each fresh batch has an artifact if undiscovered)
        val candidateArtifacts = zoneArtifactMap[zone] ?: emptyList()
        var spawnedArtifact = false

        for (i in 0 until count) {
            val shouldSpawnArtifact = !spawnedArtifact && candidateArtifacts.isNotEmpty() && (Random.nextFloat() < 0.35f || i == 0)
            val posX = 0.08f + (i % 3) * 0.28f + (Random.nextFloat() * 0.12f - 0.06f)
            val posY = 0.22f + (i / 3) * 0.22f + (Random.nextFloat() * 0.10f - 0.05f)
            val rot = Random.nextFloat() * 50f - 25f

            if (shouldSpawnArtifact) {
                val chosenArtId = candidateArtifacts.random()
                items.add(
                    ActiveBeachItem(
                        id = UUID.randomUUID().toString(),
                        isArtifact = true,
                        artifactId = chosenArtId,
                        xPercent = posX.coerceIn(0.08f, 0.88f),
                        yPercent = posY.coerceIn(0.18f, 0.78f),
                        rotation = rot,
                        scale = 1.15f
                    )
                )
                spawnedArtifact = true
            } else {
                val litter = possibleLitter.random()
                items.add(
                    ActiveBeachItem(
                        id = UUID.randomUUID().toString(),
                        isArtifact = false,
                        litterType = litter,
                        xPercent = posX.coerceIn(0.08f, 0.88f),
                        yPercent = posY.coerceIn(0.18f, 0.78f),
                        rotation = rot,
                        scale = 1.0f,
                        isBonusGolden = isFrenzy
                    )
                )
            }
        }
        _activeBeachItems.value = items
    }
}
