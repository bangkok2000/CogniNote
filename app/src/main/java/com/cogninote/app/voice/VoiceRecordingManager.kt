package com.cogninote.app.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecordingManager @Inject constructor(
    private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()
    
    fun startRecording(): RecordingResult {
        return try {
            val recordingFile = createRecordingFile()
            currentRecordingFile = recordingFile
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(recordingFile.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                
                try {
                    prepare()
                    start()
                    _recordingState.value = RecordingState.RECORDING
                } catch (e: IOException) {
                    _recordingState.value = RecordingState.ERROR
                    return RecordingResult.Error("Failed to start recording: ${e.message}")
                }
            }
            RecordingResult.Success
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            RecordingResult.Error("Failed to initialize recording: ${e.message}")
        }
    }
    
    fun stopRecording(): RecordingResult {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _recordingState.value = RecordingState.IDLE
            
            currentRecordingFile?.let { file ->
                RecordingResult.Completed(file)
            } ?: RecordingResult.Error("No recording file found")
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            RecordingResult.Error("Failed to stop recording: ${e.message}")
        }
    }
    
    fun pauseRecording(): RecordingResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                _recordingState.value = RecordingState.PAUSED
                RecordingResult.Success
            } else {
                RecordingResult.Error("Pause not supported on this Android version")
            }
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            RecordingResult.Error("Failed to pause recording: ${e.message}")
        }
    }
    
    fun resumeRecording(): RecordingResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                _recordingState.value = RecordingState.RECORDING
                RecordingResult.Success
            } else {
                RecordingResult.Error("Resume not supported on this Android version")
            }
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            RecordingResult.Error("Failed to resume recording: ${e.message}")
        }
    }
    
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            // Delete the recording file
            currentRecordingFile?.delete()
            currentRecordingFile = null
            
            _recordingState.value = RecordingState.IDLE
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
        }
    }
    
    private fun createRecordingFile(): File {
        val recordingsDir = File(context.filesDir, "recordings")
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }
        
        val timestamp = System.currentTimeMillis()
        return File(recordingsDir, "recording_$timestamp.m4a")
    }
    
    fun getRecordingAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }
}

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    ERROR
}

sealed class RecordingResult {
    object Success : RecordingResult()
    data class Completed(val file: File) : RecordingResult()
    data class Error(val message: String) : RecordingResult()
}
