package com.rngooglecloudspeechtotext.file

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.*
import java.util.*

class AudioFileManager(private val context: Context) {
    companion object {
        private const val TAG = "AudioFileManager"
        private const val TEMP_DIR = "speech_audio"
    }

    private val tempDir: File by lazy {
        File(context.cacheDir, TEMP_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun generateFileId(): String {
        return "speech_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }

    fun createTempAudioFile(fileId: String): File {
        return File(tempDir, "${fileId}.wav")
    }

    fun writeWavHeader(outputStream: FileOutputStream, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val header = ByteArray(44)

        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // File size (will be updated later)
        writeInt(header, 4, 0)

        // WAVE header
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        // Subchunk1Size (16 for PCM)
        writeInt(header, 16, 16)

        // AudioFormat (1 for PCM)
        writeShort(header, 20, 1)

        // NumChannels
        writeShort(header, 22, channels.toShort())

        // SampleRate
        writeInt(header, 24, sampleRate)

        // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
        writeInt(header, 28, sampleRate * channels * bitsPerSample / 8)

        // BlockAlign (NumChannels * BitsPerSample/8)
        writeShort(header, 32, (channels * bitsPerSample / 8).toShort())

        // BitsPerSample
        writeShort(header, 34, bitsPerSample.toShort())

        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        // Subchunk2Size (will be updated later)
        writeInt(header, 40, 0)

        outputStream.write(header)
    }

    fun updateWavHeader(file: File) {
        try {
            val fileSize = file.length().toInt()
            val dataSize = fileSize - 44

            RandomAccessFile(file, "rw").use { raf ->
                // Update file size
                raf.seek(4)
                raf.write(intToByteArray(fileSize - 8))

                // Update data size
                raf.seek(40)
                raf.write(intToByteArray(dataSize))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error updating WAV header", e)
        }
    }

    fun getFileSize(file: File): Long {
        return if (file.exists()) file.length() else 0
    }

    fun getAudioDuration(file: File): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting audio duration", e)
            0
        }
    }

    fun deleteFile(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            false
        }
    }

    fun cleanupTempFiles() {
        try {
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("speech_")) {
                    val lastModified = file.lastModified()
                    val now = System.currentTimeMillis()
                    // Delete files older than 24 hours
                    if (now - lastModified > 24 * 60 * 60 * 1000) {
                        file.delete()
                        Log.d(TAG, "Deleted old temp file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up temp files", e)
        }
    }

    private fun writeShort(buffer: ByteArray, offset: Int, value: Short) {
        buffer[offset] = (value.toInt() and 0xFF).toByte()
        buffer[offset + 1] = ((value.toInt() shr 8) and 0xFF).toByte()
    }

    private fun writeInt(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buffer[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buffer[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }
}
