package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * BeachAudioHapticsEngine generates real-time procedural sound effects,
 * victory music melodies, ambient ocean surf, and tactile haptic feedback.
 */
class BeachAudioHapticsEngine(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var ambientJob: Job? = null

    var isSfxEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // --- SOUND EFFECTS ---

    /**
     * Crisp bubble/pickup chime when collecting litter.
     */
    fun playLitterCollect(multiplier: Int = 1) {
        if (!isSfxEnabled) return
        audioScope.launch {
            val sampleRate = 44100
            val durationMs = 120
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            val baseFreq = 520.0 + (multiplier - 1) * 60.0
            val endFreq = 980.0 + (multiplier - 1) * 80.0

            for (i in 0 until totalSamples) {
                val progress = i.toDouble() / totalSamples
                val currentFreq = baseFreq + (endFreq - baseFreq) * progress
                val envelope = when {
                    progress < 0.15 -> progress / 0.15
                    else -> 1.0 - (progress - 0.15) / 0.85
                }
                val sampleVal = sin(2.0 * PI * currentFreq * i / sampleRate) * envelope * 0.8
                buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /**
     * Ethereal melodic chime when an ancient ocean artifact is uncovered.
     */
    fun playArtifactDiscovered() {
        if (!isSfxEnabled) return
        audioScope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            for (freq in notes) {
                playTone(freq, 110, 0.75)
                delay(60)
            }
        }
    }

    /**
     * Triumphant victory fanfare melody when a beach mission is completed.
     */
    fun playMissionCompleteMusic() {
        if (!isMusicEnabled && !isSfxEnabled) return
        audioScope.launch {
            // Melodic fanfare: G4 -> C5 -> E5 -> G5 (sustained chord)
            val notes = listOf(
                Pair(392.00, 140), // G4
                Pair(523.25, 140), // C5
                Pair(659.25, 160), // E5
                Pair(783.99, 380), // G5 sustained
                Pair(1046.50, 420) // C6 high bell
            )
            for ((freq, dur) in notes) {
                playTone(freq, dur, 0.85)
                delay((dur * 0.75).toLong())
            }
        }
    }

    /**
     * Prestigious fanfare when an achievement badge is unlocked.
     */
    fun playBadgeUnlocked() {
        if (!isSfxEnabled && !isMusicEnabled) return
        audioScope.launch {
            val chords = listOf(
                Pair(523.25, 120), // C5
                Pair(659.25, 120), // E5
                Pair(783.99, 140), // G5
                Pair(1046.50, 200), // C6
                Pair(1318.51, 350)  // E6
            )
            for ((freq, dur) in chords) {
                playTone(freq, dur, 0.8)
                delay((dur * 0.65).toLong())
            }
        }
    }

    /**
     * Ocean wave swoosh sound when switching beach zones or using coastal sifter.
     */
    fun playWaveSweep() {
        if (!isSfxEnabled) return
        audioScope.launch {
            val sampleRate = 22050
            val durationMs = 280
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val progress = i.toDouble() / totalSamples
                val envelope = sin(progress * PI)
                // Filtered soft ocean surf noise
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.5
                val lowSurf = sin(2.0 * PI * (120.0 + progress * 80.0) * i / sampleRate) * 0.5
                val sampleVal = (noise * 0.6 + lowSurf * 0.4) * envelope * 0.6
                buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    /**
     * Starts looping ambient ocean waves in the background.
     */
    fun startOceanAmbient() {
        if (!isMusicEnabled) return
        if (ambientJob?.isActive == true) return

        ambientJob = audioScope.launch {
            val sampleRate = 22050
            val waveDurationMs = 2500
            val totalSamples = (sampleRate * waveDurationMs) / 1000
            val buffer = ShortArray(totalSamples)

            while (isActive && isMusicEnabled) {
                for (i in 0 until totalSamples) {
                    val progress = i.toDouble() / totalSamples
                    // Ocean wave ebb & flow envelope
                    val envelope = sin(progress * PI)
                    val surfNoise = (Random.nextDouble() * 2.0 - 1.0) * 0.35
                    val subBass = sin(2.0 * PI * (65.0 + 20.0 * sin(progress * PI * 2)) * i / sampleRate) * 0.3
                    val sampleVal = (surfNoise + subBass) * envelope * 0.25
                    buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcmBuffer(buffer, sampleRate)
                delay(1200) // Brief pause between waves
            }
        }
    }

    fun stopOceanAmbient() {
        ambientJob?.cancel()
        ambientJob = null
    }

    // --- TACTILE HAPTIC FEEDBACK ---

    /**
     * Light crisp tactile pulse on collecting litter.
     */
    fun vibrateLitterCollected() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    /**
     * Double tactile pulse when an ocean artifact is found.
     */
    fun vibrateArtifactFound() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 45, 60, 65)
                val amplitudes = intArrayOf(0, 220, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 45, 60, 65), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Rhythmic celebration pattern when completing a mission or unlocking a badge.
     */
    fun vibrateMissionSuccess() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 50, 90, 60, 150)
                val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 60, 50, 90, 60, 150), -1)
            }
        } catch (_: Exception) {}
    }

    // --- INTERNAL SOUND SYNTHESIS HELPERS ---

    private fun playTone(frequencyHz: Double, durationMs: Int, volume: Double = 0.7) {
        val sampleRate = 44100
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            // Smooth ADSR envelope
            val envelope = when {
                progress < 0.1 -> progress / 0.1
                progress > 0.7 -> (1.0 - progress) / 0.3
                else -> 1.0
            }
            // Fundamental + 2nd harmonic for rich chime
            val wave = sin(2.0 * PI * frequencyHz * i / sampleRate) * 0.75 +
                    sin(4.0 * PI * frequencyHz * i / sampleRate) * 0.25
            val sampleVal = wave * envelope * volume
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcmBuffer(buffer, sampleRate)
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val trackBufferSize = maxOf(buffer.size * 2, minBufferSize)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                trackBufferSize,
                AudioTrack.MODE_STATIC,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playback finishes
            val durationMs = (buffer.size * 1000L) / sampleRate
            audioScope.launch {
                delay(durationMs + 60)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
