package com.rngooglecloudspeechtotext

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.module.annotations.ReactModule
import com.rngooglecloudspeechtotext.audio.AudioRecordManager
import com.rngooglecloudspeechtotext.speech.SpeechRecognitionManager
import com.rngooglecloudspeechtotext.file.AudioFileManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@ReactModule(name = RnGoogleCloudSpeechToTextModule.NAME)
class RnGoogleCloudSpeechToTextModule(reactContext: ReactApplicationContext) :
  NativeRnGoogleCloudSpeechToTextSpec(reactContext) {

  companion object {
    const val NAME = "RnGoogleCloudSpeechToText"
    private const val TAG = "RnGoogleCloudSpeechToText"
  }

  private var apiKey: String? = null
  private var isRecording = false
  private var currentFileId: String? = null
  private var currentFile: File? = null
  private var currentFileStream: FileOutputStream? = null
  private var speechToFile = false
  private var currentLanguageCode = "en-US"

  private val audioManager = AudioRecordManager()
  private val speechManager = SpeechRecognitionManager()
  private val fileManager = AudioFileManager(reactContext)

  init {
    setupAudioManager()
    setupSpeechManager()
  }

  override fun getName(): String {
    return NAME
  }

  // Legacy method for testing
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  override fun start(options: ReadableMap, promise: Promise) {
    try {
      // Check permissions
      if (!hasAudioPermission()) {
        promise.reject("PERMISSION_DENIED", "Microphone permission not granted")
        return
      }

      if (apiKey.isNullOrEmpty()) {
        promise.reject("NO_API_KEY", "Google Cloud API key not set")
        return
      }

      if (isRecording) {
        promise.reject("ALREADY_RECORDING", "Recording is already in progress")
        return
      }

      speechToFile = options.getBoolean("speechToFile")
      val languageCode = options.getString("languageCode") ?: "en-US"

      // Generate file ID and setup file recording if needed
      currentFileId = fileManager.generateFileId()

      if (speechToFile) {
        setupFileRecording()
      }

      // Start speech recognition streaming
      if (!speechManager.startStreaming(languageCode)) {
        promise.reject("SPEECH_START_ERROR", "Failed to start speech recognition")
        return
      }

      // Start audio recording
      if (!audioManager.startRecording()) {
        speechManager.stopStreaming()
        promise.reject("AUDIO_START_ERROR", "Failed to start audio recording")
        return
      }

      isRecording = true

      // Emit voice start event
      val voiceStartEvent = WritableNativeMap().apply {
        putInt("sampleRate", audioManager.getSampleRate())
        putInt("voiceRecorderState", 1) // Recording state
      }
      sendEvent("onVoiceStart", voiceStartEvent)

      // Return file info
      val result = WritableNativeMap().apply {
        putString("fileId", currentFileId)
        putString("tmpPath", currentFile?.absolutePath ?: "")
      }

      promise.resolve(result)

    } catch (e: Exception) {
      Log.e(TAG, "Error starting recording", e)
      promise.reject("START_ERROR", e.message, e)
    }
  }

  override fun stop(promise: Promise) {
    try {
      if (!isRecording) {
        promise.reject("NOT_RECORDING", "No recording in progress")
        return
      }

      // Stop audio recording
      audioManager.stopRecording()

      // Stop speech recognition
      speechManager.stopStreaming()

      // Finalize file if recording to file
      finalizeFileRecording()

      isRecording = false

      // Emit voice end event
      sendEvent("onVoiceEnd", null)

      promise.resolve(null)

    } catch (e: Exception) {
      Log.e(TAG, "Error stopping recording", e)
      promise.reject("STOP_ERROR", e.message, e)
    }
  }

  override fun setApiKey(apiKey: String) {
    this.apiKey = apiKey
    speechManager.setApiKey(apiKey)
    Log.i(TAG, "API key set")
  }

  override fun getAudioFile(fileId: String, options: ReadableMap, promise: Promise) {
    try {
      val file = File(fileManager.createTempAudioFile(fileId).absolutePath)

      if (!file.exists()) {
        promise.reject("FILE_NOT_FOUND", "Audio file not found for fileId: $fileId")
        return
      }

      val result = WritableNativeMap().apply {
        putDouble("size", fileManager.getFileSize(file).toDouble())
        putString("path", file.absolutePath)
      }

      promise.resolve(result)

    } catch (e: Exception) {
      Log.e(TAG, "Error getting audio file", e)
      promise.reject("GET_AUDIO_FILE_ERROR", e.message, e)
    }
  }

  override fun destroy(promise: Promise) {
    try {
      // Stop any ongoing recording
      if (isRecording) {
        audioManager.stopRecording()
        speechManager.stopStreaming()
        finalizeFileRecording()
        isRecording = false
      }

      // Cleanup resources
      speechManager.destroy()
      fileManager.cleanupTempFiles()

      // Reset state
      apiKey = null
      currentFileId = null
      currentFile = null

      promise.resolve(null)

    } catch (e: Exception) {
      Log.e(TAG, "Error destroying module", e)
      promise.reject("DESTROY_ERROR", e.message, e)
    }
  }

  override fun addListener(eventName: String) {
    // Event listeners are handled by React Native's event system
  }

  override fun removeListeners(count: Double) {
    // Event listeners are handled by React Native's event system
  }

  private fun setupAudioManager() {
    audioManager.setAudioDataListener { audioData, length ->
      // Send audio data to speech recognition
      speechManager.sendAudioData(audioData, length)

      // Write to file if recording to file
      if (speechToFile && currentFileStream != null) {
        try {
          currentFileStream?.write(audioData, 0, length)
        } catch (e: IOException) {
          Log.e(TAG, "Error writing audio data to file", e)
        }
      }
    }

    audioManager.setAudioLevelListener { audioLevel ->
      // Emit voice event with audio level
      val voiceEvent = WritableNativeMap().apply {
        putInt("size", audioLevel)
      }
      sendEvent("onVoice", voiceEvent)
    }
  }

  private fun setupSpeechManager() {
    speechManager.setResultListener { transcript, isFinal ->
      val recognizeEvent = WritableNativeMap().apply {
        putString("transcript", transcript)
        putBoolean("isFinal", isFinal)
      }

      if (isFinal) {
        sendEvent("onSpeechRecognized", recognizeEvent)
      } else {
        sendEvent("onSpeechRecognizing", recognizeEvent)
      }
    }

    speechManager.setErrorListener { errorCode, errorMessage ->
      val errorEvent = WritableNativeMap().apply {
        putMap("error", WritableNativeMap().apply {
          putString("code", errorCode)
          putString("message", errorMessage ?: "Unknown error")
        })
      }
      sendEvent("onSpeechError", errorEvent)
    }
  }

  private fun setupFileRecording() {
    try {
      currentFile = fileManager.createTempAudioFile(currentFileId!!)
      currentFileStream = FileOutputStream(currentFile)

      // Write WAV header
      fileManager.writeWavHeader(
        currentFileStream!!,
        audioManager.getSampleRate(),
        1, // mono
        16 // 16-bit
      )

    } catch (e: IOException) {
      Log.e(TAG, "Error setting up file recording", e)
      currentFileStream = null
    }
  }

  private fun finalizeFileRecording() {
    try {
      currentFileStream?.close()
      currentFileStream = null

      if (currentFile != null) {
        // Update WAV header with correct file size
        fileManager.updateWavHeader(currentFile!!)
      }

    } catch (e: IOException) {
      Log.e(TAG, "Error finalizing file recording", e)
    }
  }

  private fun hasAudioPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
      reactApplicationContext,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
  }

  private fun sendEvent(eventName: String, params: WritableMap?) {
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }
}
