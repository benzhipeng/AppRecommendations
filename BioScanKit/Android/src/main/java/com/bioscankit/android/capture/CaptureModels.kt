package com.bioscankit.android.capture

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bioscankit.android.design.BioScanTheme

enum class CameraFinderStyle { CornerMarkers, Scanner }
enum class CameraPageLayoutStyle { INatureLegacy, Adaptive }
enum class CropBorderAnimationStyle { None, Pulse }
enum class CropEditorLayoutStyle { INatureLegacy, Adaptive }
enum class ProcessingScreenLayoutStyle { INatureLegacy, Adaptive }

@Immutable
data class CameraScreenConfiguration(
    val theme: BioScanTheme = BioScanTheme.INature,
    val title: String = "Identify",
    val instruction: String = "Center your subject inside the frame",
    val permissionTitle: String = "Camera Access",
    val permissionMessage: String = "Allow camera access to photograph and identify your subject.",
    val deniedMessage: String = "Camera access is disabled. You can enable it in Settings.",
    val supportsPhotoLibrary: Boolean = true,
    val supportsFlash: Boolean = true,
    val supportsTapToFocus: Boolean = true,
    val supportsPinchToZoom: Boolean = true,
    val statusText: String? = "On-device recognition",
    val processingInstruction: String = "Analyzing subject…",
    val finderCornerRadius: Dp = 24.dp,
    val finderMaximumSize: Dp = 300.dp,
    val finderStyle: CameraFinderStyle = CameraFinderStyle.CornerMarkers,
    val finderColor: Color = theme.accent,
    val layoutStyle: CameraPageLayoutStyle = CameraPageLayoutStyle.INatureLegacy,
    val maximumZoomFactor: Float = 6f,
) {
    companion object {
        val INature = CameraScreenConfiguration()
    }
}

@Immutable
data class CropEditorConfiguration(
    val theme: BioScanTheme = BioScanTheme.INature,
    val aspectRatio: Float = 1f,
    val cropScale: Float = 0.78f,
    val cropMaximumSize: Dp? = null,
    val cornerRadius: Dp = 14.dp,
    val showsGrid: Boolean = true,
    val showsPreview: Boolean = true,
    val allowsReset: Boolean = true,
    val guidanceText: String = "Center your subject inside the frame",
    val confirmTitle: String = "Identify",
    val animationStyle: CropBorderAnimationStyle = CropBorderAnimationStyle.Pulse,
    val layoutStyle: CropEditorLayoutStyle = CropEditorLayoutStyle.INatureLegacy,
) {
    companion object {
        val INature = CropEditorConfiguration()
    }
}

@Immutable
data class ProcessingScreenConfiguration(
    val theme: BioScanTheme = BioScanTheme.INature,
    val eyebrow: String = "AI SCAN",
    val statuses: List<String> = listOf(
        "Preparing scan...",
        "Detecting...",
        "Extracting Features...",
        "Comparing Species...",
        "Waiting for result...",
    ),
    val waitingText: String = "Processing",
    val progressTargets: List<Float> = listOf(0.18f, 0.42f, 0.67f, 0.86f, 0.96f),
    val layoutStyle: ProcessingScreenLayoutStyle = ProcessingScreenLayoutStyle.INatureLegacy,
) {
    init {
        require(statuses.isNotEmpty()) { "Processing statuses cannot be empty." }
        require(progressTargets.size == statuses.size) {
            "Each processing status must have one progress target."
        }
        require(progressTargets.all { it in 0f..0.99f }) {
            "Progress targets must stay below 1.0 until recognition actually completes."
        }
    }

    companion object {
        val INature = ProcessingScreenConfiguration()
    }
}

@Immutable
data class CapturedPhoto(
    val uri: String,
    val source: PhotoSource,
)

enum class PhotoSource { Camera, PhotoLibrary, Demo }

@Immutable
data class RecognitionContext(
    val source: PhotoSource,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

sealed interface RecognitionAccess {
    data object Allowed : RecognitionAccess
    data object RequiresPurchase : RecognitionAccess
    data class Denied(val reason: String) : RecognitionAccess
}

sealed interface PhotoRecognitionPhase<out Result> {
    data object Idle : PhotoRecognitionPhase<Nothing>
    data class Cropping(val photo: CapturedPhoto) : PhotoRecognitionPhase<Nothing>
    data class Processing(val photo: CapturedPhoto) : PhotoRecognitionPhase<Nothing>
    data class Completed<Result>(val result: Result) : PhotoRecognitionPhase<Result>
    data class Failed(val message: String) : PhotoRecognitionPhase<Nothing>
}
