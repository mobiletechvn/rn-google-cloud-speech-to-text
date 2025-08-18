package com.rngooglecloudspeechtotext.speech

import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import java.util.Base64

class SpeechRecognitionManager {
    companion object {
        private const val TAG = "SpeechRecognitionManager"
        private const val SAMPLE_RATE = 16000
        private const val SPEECH_API_URL = "https://speech.googleapis.com/v1/speech:recognize"
        private const val MAX_AUDIO_BUFFER_SIZE = 60 * 1024 // 60KB buffer for audio chunks
    }

    private var apiKey: String? = null
    private var currentLanguageCode = "en-US"
    private val isRecognizing = AtomicBoolean(false)
    private val audioBuffer = ByteArrayOutputStream()
    private var bufferSize = 0

    private var onPartialResult: ((String, Boolean) -> Unit)? = null
    private var onError: ((String, String?) -> Unit)? = null

    fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
        Log.i(TAG, "API key set for speech recognition")
    }

    fun setResultListener(onResult: (String, Boolean) -> Unit) {
        onPartialResult = onResult
    }

    fun setErrorListener(onError: (String, String?) -> Unit) {
        this.onError = onError
    }

    fun startStreaming(languageCode: String = "en-US"): Boolean {
        try {
            if (apiKey.isNullOrEmpty()) {
                Log.e(TAG, "API key is not set")
                onError?.invoke("API_KEY_NOT_SET", "API key is required for speech recognition")
                return false
            }

            if (isRecognizing.get()) {
                Log.w(TAG, "Already recognizing")
                return true
            }

            // Store the language code for use in recognition calls
            currentLanguageCode = languageCode

            // Reset audio buffer
            audioBuffer.reset()
            bufferSize = 0
            isRecognizing.set(true)

            Log.i(TAG, "Speech recognition started with language: $languageCode")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            onError?.invoke("START_ERROR", e.message)
            return false
        }
    }

    fun sendAudioData(audioData: ByteArray, length: Int) {
        if (!isRecognizing.get()) {
            return
        }

        try {
            synchronized(audioBuffer) {
                audioBuffer.write(audioData, 0, length)
                bufferSize += length

                // Process audio in chunks when buffer reaches a certain size
                if (bufferSize >= MAX_AUDIO_BUFFER_SIZE) {
                    processAudioBuffer()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error buffering audio data", e)
            onError?.invoke("AUDIO_BUFFER_ERROR", e.message)
        }
    }

    fun stopStreaming() {
        if (!isRecognizing.get()) {
            return
        }

        try {
            // Process any remaining audio in buffer
            if (bufferSize > 0) {
                processAudioBuffer()
            }

            isRecognizing.set(false)
            Log.i(TAG, "Speech recognition stopped")

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }

    private fun processAudioBuffer() {
        if (bufferSize == 0) return

        thread {
            try {
                synchronized(audioBuffer) {
                    val audioBytes = audioBuffer.toByteArray()
                    audioBuffer.reset()
                    bufferSize = 0

                    // Send to Google Cloud Speech API
                    recognizeAudio(audioBytes, currentLanguageCode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio buffer", e)
                onError?.invoke("PROCESSING_ERROR", e.message)
            }
        }
    }

    private fun recognizeAudio(audioData: ByteArray, languageCode: String = "en-US") {
        try {
            val url = URL("$SPEECH_API_URL?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection

            // Set up HTTP connection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 30000

            // Create request JSON
            val audioBase64 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              Base64.getEncoder().encodeToString(audioData)
            } else {
              TODO("VERSION.SDK_INT < O")
            }
          val requestJson = JSONObject().apply {
                put("config", JSONObject().apply {
                    put("encoding", "LINEAR16")
                    put("sampleRateHertz", SAMPLE_RATE)
                    put("languageCode", languageCode)
                    put("enableAutomaticPunctuation", true)
                    put("model", "latest_long")
                    put("useEnhanced", true)
                })
                put("audio", JSONObject().apply {
                    put("content", audioBase64)
                })
            }

            // Send request
            val outputStream = connection.outputStream
            outputStream.write(requestJson.toString().toByteArray())
            outputStream.flush()
            outputStream.close()

            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                handleResponse(response)
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                Log.e(TAG, "API Error: $responseCode - $errorResponse")
                onError?.invoke("API_ERROR", "HTTP $responseCode: $errorResponse")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error calling speech recognition API", e)
            onError?.invoke("NETWORK_ERROR", e.message)
        }
    }

    private fun handleResponse(jsonResponse: String) {
        try {
            val response = JSONObject(jsonResponse)

            if (response.has("results")) {
                val results = response.getJSONArray("results")

                if (results.length() > 0) {
                    val result = results.getJSONObject(0)
                    val alternatives = result.getJSONArray("alternatives")

                    if (alternatives.length() > 0) {
                        val alternative = alternatives.getJSONObject(0)
                        val transcript = alternative.getString("transcript")
                        val confidence = alternative.optDouble("confidence", 0.0)

                        Log.d(TAG, "Recognition result: '$transcript' (confidence: $confidence)")

                        // For API-based recognition, we consider all results as final
                        // To simulate interim results, we could split this into partial calls
                        onPartialResult?.invoke(transcript, true)
                    }
                } else {
                    Log.d(TAG, "No recognition results in response")
                    // This could be an interim result with no text
                    onPartialResult?.invoke("", false)
                }
            } else if (response.has("error")) {
                val error = response.getJSONObject("error")
                val message = error.optString("message", "Unknown API error")
                val code = error.optString("code", "UNKNOWN")
                Log.e(TAG, "API returned error: $code - $message")
                onError?.invoke("API_ERROR", "$code: $message")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing API response", e)
            onError?.invoke("RESPONSE_PARSE_ERROR", e.message)
        }
    }

    fun destroy() {
        stopStreaming()
        synchronized(audioBuffer) {
            audioBuffer.reset()
            bufferSize = 0
        }
        Log.i(TAG, "Speech recognition manager destroyed")
    }

    fun isCurrentlyStreaming(): Boolean = isRecognizing.get()
}
