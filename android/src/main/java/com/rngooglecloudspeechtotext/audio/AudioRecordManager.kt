package com.rngooglecloudspeechtotext.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class AudioRecordManager {
    companion object {
        private const val TAG = "AudioRecordManager"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
    }

    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private var recordingThread: Thread? = null
    private var audioDataListener: ((ByteArray, Int) -> Unit)? = null
    private var audioLevelListener: ((Int) -> Unit)? = null

    private val bufferSize: Int by lazy {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        minBufferSize * BUFFER_SIZE_FACTOR
    }

    fun setAudioDataListener(listener: (ByteArray, Int) -> Unit) {
        audioDataListener = listener
    }

    fun setAudioLevelListener(listener: (Int) -> Unit) {
        audioLevelListener = listener
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(): Boolean {
        try {
            if (isRecording.get()) {
                Log.w(TAG, "Already recording")
                return false
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return false
            }

            audioRecord?.startRecording()
            isRecording.set(true)

            recordingThread = thread(start = true) {
                recordAudio()
            }

            Log.i(TAG, "Audio recording started")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio recording", e)
            return false
        }
    }

    fun stopRecording() {
        isRecording.set(false)
        recordingThread?.join(1000)

        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                stop()
            }
            release()
        }
        audioRecord = null

        Log.i(TAG, "Audio recording stopped")
    }

    private fun recordAudio() {
        val buffer = ByteArray(bufferSize)

        while (isRecording.get()) {
            val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0

            if (bytesRead > 0) {
                // Calculate audio level for voice detection
                val audioLevel = calculateAudioLevel(buffer, bytesRead)
                audioLevelListener?.invoke(audioLevel)

                // Send audio data to listener
                audioDataListener?.invoke(buffer, bytesRead)
            }
        }
    }

    private fun calculateAudioLevel(buffer: ByteArray, length: Int): Int {
        var sum = 0.0
        for (i in 0 until length step 2) {
            if (i + 1 < length) {
                val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                val normalizedSample = if (sample > 32767) sample - 65536 else sample
                sum += normalizedSample * normalizedSample
            }
        }
        val rms = kotlin.math.sqrt(sum / (length / 2))
        return (rms * 100).toInt().coerceAtMost(1000)
    }

    fun getSampleRate(): Int = SAMPLE_RATE

    fun isCurrentlyRecording(): Boolean = isRecording.get()
}
