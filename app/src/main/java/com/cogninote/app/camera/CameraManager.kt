package com.cogninote.app.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraManager @Inject constructor(
    private val context: Context
) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: androidx.camera.view.PreviewView,
        onTextDetected: (String) -> Unit
    ): CameraProvider = suspendCancellableCoroutine { continuation ->
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Preview use case
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                
                // Image analysis use case for OCR
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageForText(imageProxy, onTextDetected)
                }
                
                // Select back camera as default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()
                    
                    // Bind use cases to camera
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    
                    continuation.resume(CameraProvider(cameraProvider, camera))
                    
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
                
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    private fun processImageForText(
        imageProxy: ImageProxy,
        onTextDetected: (String) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    if (extractedText.isNotBlank()) {
                        onTextDetected(extractedText)
                    }
                }
                .addOnFailureListener { e ->
                    // Handle OCR failure
                    android.util.Log.e("CameraManager", "Text recognition failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
    
    suspend fun captureAndAnalyzeImage(imageProxy: ImageProxy): OcrResult = 
        suspendCancellableCoroutine { continuation ->
            
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val result = OcrResult.Success(
                            text = visionText.text,
                            textBlocks = visionText.textBlocks.map { block ->
                                TextBlock(
                                    text = block.text,
                                    boundingBox = block.boundingBox,
                                    confidence = 0f // block.confidence ?: 0f // Confidence might not be available in this ML Kit version
                                )
                            }
                        )
                        continuation.resume(result)
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(OcrResult.Error(e.message ?: "OCR failed"))
                    }
            } else {
                continuation.resume(OcrResult.Error("No image available"))
            }
        }
    
    fun shutdown() {
        cameraExecutor.shutdown()
        textRecognizer.close()
    }
}

data class CameraProvider(
    val provider: ProcessCameraProvider,
    val camera: Camera
)

sealed class OcrResult {
    data class Success(
        val text: String,
        val textBlocks: List<TextBlock>
    ) : OcrResult()
    
    data class Error(val message: String) : OcrResult()
}

data class TextBlock(
    val text: String,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float
)
