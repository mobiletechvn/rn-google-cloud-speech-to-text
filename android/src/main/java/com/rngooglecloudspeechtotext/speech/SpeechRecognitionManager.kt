package com.rngooglecloudspeechtotext.speech

import android.util.Log
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechContext
import com.google.cloud.speech.v1.SpeechGrpc
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import com.google.protobuf.ByteString
import io.grpc.*
import io.grpc.ForwardingClientCall
import io.grpc.stub.StreamObserver
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.TimeUnit

class SpeechRecognitionManager {
    companion object {
        private const val TAG = "SpeechRecognitionManager"
        private const val SAMPLE_RATE = 16000
        private const val SPEECH_API_HOST = "speech.googleapis.com"
        private const val SPEECH_API_PORT = 443
    }

    private var apiKey: String? = null
    private var currentLanguageCode = "en-US"
    private val isRecognizing = AtomicBoolean(false)

    // gRPC components
    private var channel: ManagedChannel? = null
    private var speechClient: SpeechGrpc.SpeechStub? = null
    private var requestObserver: StreamObserver<StreamingRecognizeRequest>? = null

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

            // Initialize gRPC channel with API key authentication
            if (!initializeGrpcChannel()) {
                onError?.invoke("GRPC_INIT_ERROR", "Failed to initialize gRPC channel")
                return false
            }

            // Start streaming recognition
            if (!startStreamingRecognition()) {
                onError?.invoke("STREAM_START_ERROR", "Failed to start streaming recognition")
                return false
            }

            isRecognizing.set(true)
            Log.i(TAG, "gRPC streaming recognition started with language: $languageCode")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            onError?.invoke("START_ERROR", e.message)
            return false
        }
    }

    fun sendAudioData(audioData: ByteArray, length: Int) {
        if (!isRecognizing.get() || requestObserver == null) {
            return
        }

        try {
            // Send audio data directly to gRPC stream
            val audioRequest = StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(audioData, 0, length))
                .build()

            requestObserver?.onNext(audioRequest)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending audio data to gRPC stream", e)
            onError?.invoke("AUDIO_SEND_ERROR", e.message)
        }
    }

    fun stopStreaming() {
        if (!isRecognizing.get()) {
            return
        }

        try {
            // Complete the gRPC stream
            requestObserver?.onCompleted()

            // Shutdown gRPC resources
            cleanupGrpcResources()

            isRecognizing.set(false)
            Log.i(TAG, "gRPC streaming recognition stopped")

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
            onError?.invoke("STOP_ERROR", e.message)
        }
    }

    private fun initializeGrpcChannel(): Boolean {
        try {
            // Create gRPC channel with TLS
            channel = ManagedChannelBuilder.forAddress(SPEECH_API_HOST, SPEECH_API_PORT)
                .useTransportSecurity()
                .build()

            // Create API key interceptor
            val apiKeyInterceptor = ApiKeyInterceptor(apiKey!!)

            // Create speech client with API key authentication
            speechClient = SpeechGrpc.newStub(channel)
                .withInterceptors(apiKeyInterceptor)
                .withDeadlineAfter(300, TimeUnit.SECONDS)

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize gRPC channel", e)
            return false
        }
    }

    private fun startStreamingRecognition(): Boolean {
        try {
            // Create streaming recognition config
            val recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(SAMPLE_RATE)
                .setLanguageCode(currentLanguageCode)
                .setModel("command_and_search")
                .setEnableAutomaticPunctuation(false)
                .setMaxAlternatives(1)
                .addSpeechContexts(SpeechContext.newBuilder().addAllPhrases(listOf("một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "không")))
                .setUseEnhanced(true)
                .build()

            val streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(recognitionConfig)
                .setInterimResults(true)
                .setSingleUtterance(false)
                .build()

            // Create response observer
            val responseObserver = createResponseObserver()

            // Create request observer
            requestObserver = speechClient?.streamingRecognize(responseObserver)

            // Send initial config request
            val initialRequest = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingConfig)
                .build()

            requestObserver?.onNext(initialRequest)

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start streaming recognition", e)
            return false
        }
    }

    private fun createResponseObserver(): StreamObserver<StreamingRecognizeResponse> {
        return object : StreamObserver<StreamingRecognizeResponse> {
            override fun onNext(response: StreamingRecognizeResponse) {
                if (response.resultsCount > 0) {
                    val result = response.getResults(0)
                    if (result.alternativesCount > 0) {
                        val alternative = result.getAlternatives(0)
                        val transcript = alternative.transcript
                        val isFinal = result.isFinal

                        Log.d(TAG, "Recognition result: '$transcript' (isFinal: $isFinal)")
                        onPartialResult?.invoke(transcript, isFinal)
                    }
                }
            }

            override fun onError(error: Throwable) {
                Log.e(TAG, "gRPC stream error", error)
                onError?.invoke("GRPC_ERROR", error.message)
                cleanupGrpcResources()
                isRecognizing.set(false)
            }

            override fun onCompleted() {
                Log.d(TAG, "gRPC stream completed")
                cleanupGrpcResources()
                isRecognizing.set(false)
            }
        }
    }

    private fun cleanupGrpcResources() {
        try {
            requestObserver = null
            speechClient = null

            channel?.let {
                if (!it.isShutdown) {
                    it.shutdown()
                    try {
                        if (!it.awaitTermination(5, TimeUnit.SECONDS)) {
                            it.shutdownNow()
                        }
                    } catch (e: InterruptedException) {
                        Log.w(TAG, "Interrupted while waiting for channel termination", e)
                        it.shutdownNow()
                    }
                }
            }
            channel = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during gRPC cleanup", e)
        }
    }

    /**
     * Custom interceptor for API key authentication
     */
    private class ApiKeyInterceptor(private val apiKey: String) : ClientInterceptor {
        override fun <ReqT, RespT> interceptCall(
            method: MethodDescriptor<ReqT, RespT>,
            callOptions: CallOptions,
            next: Channel
        ): ClientCall<ReqT, RespT> {
            return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)
            ) {
                override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                    val newHeaders = Metadata()
                    val apiKeyMetadataKey = Metadata.Key.of("X-Goog-Api-Key", Metadata.ASCII_STRING_MARSHALLER)
                    newHeaders.put(apiKeyMetadataKey, apiKey)
                    newHeaders.merge(headers)
                    super.start(responseListener, newHeaders)
                }
            }
        }
    }

    fun destroy() {
        stopStreaming()
        cleanupGrpcResources()
        Log.i(TAG, "Speech recognition manager destroyed")
    }

    fun isCurrentlyStreaming(): Boolean = isRecognizing.get()
}
