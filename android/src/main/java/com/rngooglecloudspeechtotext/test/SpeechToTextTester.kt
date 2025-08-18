package com.rngooglecloudspeechtotext.test

import android.content.Context
import android.util.Log
import com.rngooglecloudspeechtotext.audio.AudioRecordManager
import com.rngooglecloudspeechtotext.speech.SpeechRecognitionManager
import com.rngooglecloudspeechtotext.file.AudioFileManager

/**
 * Test utilities for the Google Cloud Speech-to-Text module
 * This class provides testing methods to verify the implementation
 */
class SpeechToTextTester(private val context: Context) {
    companion object {
        private const val TAG = "SpeechToTextTester"
    }

    fun testAudioRecordManager(): Boolean {
        return try {
            val audioManager = AudioRecordManager()

            // Test audio level listener
            audioManager.setAudioLevelListener { level ->
                Log.d(TAG, "Audio level: $level")
            }

            // Test audio data listener
            audioManager.setAudioDataListener { data, length ->
                Log.d(TAG, "Audio data received: $length bytes")
            }

            Log.i(TAG, "AudioRecordManager test passed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecordManager test failed", e)
            false
        }
    }

    fun testSpeechRecognitionManager(): Boolean {
        return try {
            val speechManager = SpeechRecognitionManager()

            // Test result listener
            speechManager.setResultListener { transcript, isFinal ->
                Log.d(TAG, "Speech result: $transcript, final: $isFinal")
            }

            // Test error listener
            speechManager.setErrorListener { code, message ->
                Log.d(TAG, "Speech error: $code - $message")
            }

            Log.i(TAG, "SpeechRecognitionManager test passed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "SpeechRecognitionManager test failed", e)
            false
        }
    }

    fun testAudioFileManager(): Boolean {
        return try {
            val fileManager = AudioFileManager(context)

            // Test file ID generation
            val fileId = fileManager.generateFileId()
            Log.d(TAG, "Generated file ID: $fileId")

            // Test temp file creation
            val tempFile = fileManager.createTempAudioFile(fileId)
            Log.d(TAG, "Created temp file: ${tempFile.absolutePath}")

            // Test cleanup
            fileManager.cleanupTempFiles()

            Log.i(TAG, "AudioFileManager test passed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "AudioFileManager test failed", e)
            false
        }
    }

    fun runAllTests(): Boolean {
        Log.i(TAG, "Starting Speech-to-Text implementation tests...")

        val audioTest = testAudioRecordManager()
        val speechTest = testSpeechRecognitionManager()
        val fileTest = testAudioFileManager()

        val allPassed = audioTest && speechTest && fileTest

        Log.i(TAG, "All tests ${if (allPassed) "PASSED" else "FAILED"}")
        return allPassed
    }

    fun testApiKeyValidation(apiKey: String): Boolean {
        return try {
            // Basic API key validation
            val isValid = apiKey.isNotEmpty() &&
                         apiKey.startsWith("AIza") &&
                         apiKey.length == 39

            Log.d(TAG, "API key validation: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "API key validation failed", e)
            false
        }
    }

    fun testPermissions(): Map<String, Boolean> {
        val permissions = mapOf(
            "RECORD_AUDIO" to android.Manifest.permission.RECORD_AUDIO,
            "INTERNET" to android.Manifest.permission.INTERNET,
            "ACCESS_NETWORK_STATE" to android.Manifest.permission.ACCESS_NETWORK_STATE,
            "WRITE_EXTERNAL_STORAGE" to android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            "MODIFY_AUDIO_SETTINGS" to android.Manifest.permission.MODIFY_AUDIO_SETTINGS
        )

        return permissions.mapValues { (name, permission) ->
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            Log.d(TAG, "Permission $name: ${if (granted) "GRANTED" else "DENIED"}")
            granted
        }
    }
}
