package com.example.data.repository

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object WavAudioGenerator {

    /**
     * Generates a 16-bit 44.1kHz stereo PCM WAV file with harmonious ambient chords.
     */
    fun createSynthesizedTrack(
        context: Context,
        fileName: String,
        durationSeconds: Int = 12,
        baseFrequencies: DoubleArray,
        tempoBpm: Int = 100
    ): File {
        val dir = File(context.filesDir, "demo_audio")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        if (file.exists() && file.length() > 44) {
            return file // Already generated
        }

        val sampleRate = 44100
        val numChannels = 2
        val bitsPerSample = 16
        val totalSamples = sampleRate * durationSeconds
        val dataSize = totalSamples * numChannels * (bitsPerSample / 8)
        val byteBuffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        // 1. RIFF Header
        byteBuffer.put("RIFF".toByteArray())
        byteBuffer.putInt(36 + dataSize)
        byteBuffer.put("WAVE".toByteArray())

        // 2. fmt chunk
        byteBuffer.put("fmt ".toByteArray())
        byteBuffer.putInt(16) // Subchunk1Size (16 for PCM)
        byteBuffer.putShort(1.toShort()) // AudioFormat (1 for PCM)
        byteBuffer.putShort(numChannels.toShort())
        byteBuffer.putInt(sampleRate)
        byteBuffer.putInt(sampleRate * numChannels * (bitsPerSample / 8)) // ByteRate
        byteBuffer.putShort((numChannels * (bitsPerSample / 8)).toShort()) // BlockAlign
        byteBuffer.putShort(bitsPerSample.toShort())

        // 3. data chunk
        byteBuffer.put("data".toByteArray())
        byteBuffer.putInt(dataSize)

        // 4. Synthesize harmonious ambient chord pads with smooth envelopes
        val beatLength = (60.0 / tempoBpm) * sampleRate

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val beatPhase = (i % beatLength.toInt()).toDouble() / beatLength
            // Smooth attack and decay envelope
            val envelope = (sin(beatPhase * PI)).coerceIn(0.1, 1.0)
            val masterFade = when {
                i < sampleRate -> i.toDouble() / sampleRate
                i > totalSamples - sampleRate -> (totalSamples - i).toDouble() / sampleRate
                else -> 1.0
            }

            var leftSample = 0.0
            var rightSample = 0.0

            for ((idx, freq) in baseFrequencies.withIndex()) {
                val detune = 1.0 + (idx * 0.003)
                val waveL = sin(2.0 * PI * freq * detune * t)
                val waveR = sin(2.0 * PI * freq * (2.0 - detune) * t)
                // Add warm harmonic overtone
                val overtone = 0.3 * sin(4.0 * PI * freq * t)
                leftSample += (waveL + overtone) * (0.8 / baseFrequencies.size)
                rightSample += (waveR + overtone) * (0.8 / baseFrequencies.size)
            }

            val finalL = (leftSample * envelope * masterFade * 0.7 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            val finalR = (rightSample * envelope * masterFade * 0.7 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

            byteBuffer.putShort(finalL)
            byteBuffer.putShort(finalR)
        }

        FileOutputStream(file).use { fos ->
            fos.write(byteBuffer.array())
            fos.flush()
        }

        return file
    }
}
