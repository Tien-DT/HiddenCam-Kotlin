package com.example.hiddencam.domain.model

/**
 * Represents video recording settings
 */
data class VideoSettings(
    val cameraFacing: CameraFacing = CameraFacing.BACK,
    val resolution: VideoResolution = VideoResolution.HD_720P,
    val frameRate: Int = 30,
    val bitrate: VideoBitrate = VideoBitrate.MEDIUM,
    val audioSource: AudioSource = AudioSource.MICROPHONE,
    val volumeButtonEnabled: Boolean = true,
    val powerButtonEnabled: Boolean = true,
    val orientation: VideoOrientation = VideoOrientation.PORTRAIT,
    val flashEnabled: Boolean = false
)

enum class CameraFacing {
    FRONT,
    BACK
}

enum class VideoResolution(val width: Int, val height: Int, val displayName: String) {
    SD_480P(640, 480, "480p (SD)"),
    HD_720P(1280, 720, "720p (HD)"),
    FHD_1080P(1920, 1080, "1080p (Full HD)"),
    UHD_4K(3840, 2160, "4K (UHD)")
}

enum class VideoBitrate(val bitsPerSecond: Int, val displayName: String) {
    LOW(2_000_000, "Low (2 Mbps)"),
    MEDIUM(5_000_000, "Medium (5 Mbps)"),
    HIGH(10_000_000, "High (10 Mbps)"),
    ULTRA(20_000_000, "Ultra (20 Mbps)")
}

enum class AudioSource(val displayName: String) {
    NONE("No Audio"),
    MICROPHONE("Microphone"),
    CAMCORDER("Camcorder (optimized)")
}

enum class VideoOrientation(val displayName: String) {
    PORTRAIT("Portrait (Dọc)"),
    LANDSCAPE("Landscape (Ngang)")
}
