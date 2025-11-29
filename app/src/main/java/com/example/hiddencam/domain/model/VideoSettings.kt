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
    val vibrationFeedbackEnabled: Boolean = true, // Vibrate when recording starts/stops
    val orientation: VideoOrientation = VideoOrientation.PORTRAIT,
    val flashEnabled: Boolean = false,
    val appIcon: AppIcon = AppIcon.DEFAULT,
    val appName: AppName = AppName.HIDDEN_CAM,
    // Advanced camera settings
    val isoMode: IsoMode = IsoMode.AUTO,
    val exposureCompensation: Int = 0, // EV value from -4 to +4 (in steps)
    val shutterSpeedMode: ShutterSpeedMode = ShutterSpeedMode.AUTO,
    val customShutterSpeed: Long = 0L, // in nanoseconds, 0 means auto
    val focusMode: FocusMode = FocusMode.CONTINUOUS_VIDEO,
    // New features
    val encryptVideo: Boolean = false, // Encrypt video with app lock PIN
    val recordingMode: RecordingMode = RecordingMode.MANUAL, // Recording mode
    val loopRecordingMinFreeGB: Int = 2 // Min free storage before deleting old videos (GB)
)

enum class CameraFacing(val displayName: String) {
    FRONT("Front Camera"),
    BACK("Back Camera"),
    USB("USB Camera (OTG)")
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
    MICROPHONE("Phone Microphone"),
    CAMCORDER("Camcorder (optimized)"),
    BLUETOOTH("Bluetooth Headset"),
    MIXED("Phone + Bluetooth Mixed")
}

/**
 * Recording mode
 */
enum class RecordingMode(val displayName: String) {
    MANUAL("Manual (Stop manually)"),
    UNTIL_FULL("Until Storage Full"),
    LOOP("Loop Recording (Auto-delete old)")
}

enum class VideoOrientation(val displayName: String) {
    PORTRAIT("Portrait (Dọc)"),
    LANDSCAPE("Landscape (Ngang)")
}

enum class AppIcon(val displayName: String, val aliasName: String) {
    DEFAULT("Default (X Logo)", ".MainActivityDefault"),
    MOLECULE("Calculator", ".MainActivityMolecule"),
    GEAR("Settings", ".MainActivityGear")
}

enum class AppName(val displayName: String, val labelResName: String) {
    HIDDEN_CAM("HiddenCam", "app_name"),
    CALCULATOR("Calculator", "app_name_calculator"),
    NOTES("Notes", "app_name_notes"),
    SETTINGS("Settings", "app_name_settings")
}

/**
 * ISO sensitivity mode
 * AUTO: Camera automatically selects ISO
 * Manual values: Fixed ISO levels
 */
enum class IsoMode(val displayName: String, val isoValue: Int?) {
    AUTO("Auto", null),
    ISO_100("100", 100),
    ISO_200("200", 200),
    ISO_400("400", 400),
    ISO_800("800", 800),
    ISO_1600("1600", 1600),
    ISO_3200("3200", 3200),
    ISO_6400("6400", 6400),
    ISO_MAX("Max", Int.MAX_VALUE)
}

/**
 * Shutter speed mode
 * AUTO: Camera automatically selects shutter speed
 * CUSTOM: User-defined shutter speed
 */
enum class ShutterSpeedMode(val displayName: String) {
    AUTO("Auto"),
    CUSTOM("Custom")
}

/**
 * Common shutter speed values in nanoseconds
 * Note: Minimum shutter speed depends on frame rate (e.g., 30fps -> min 1/30s)
 */
object ShutterSpeedValues {
    // Shutter speed in nanoseconds (1 second = 1_000_000_000 nanoseconds)
    const val SPEED_1_30 = 33_333_333L    // 1/30 second
    const val SPEED_1_60 = 16_666_667L    // 1/60 second
    const val SPEED_1_125 = 8_000_000L    // 1/125 second
    const val SPEED_1_250 = 4_000_000L    // 1/250 second
    const val SPEED_1_500 = 2_000_000L    // 1/500 second
    const val SPEED_1_1000 = 1_000_000L   // 1/1000 second
    const val SPEED_1_2000 = 500_000L     // 1/2000 second
    const val SPEED_1_4000 = 250_000L     // 1/4000 second
    const val SPEED_1_8000 = 125_000L     // 1/8000 second
    
    /**
     * Get minimum allowed shutter speed based on frame rate
     * @param frameRate current video frame rate
     * @return minimum shutter speed in nanoseconds
     */
    fun getMinShutterSpeed(frameRate: Int): Long {
        // Frame duration in nanoseconds
        return (1_000_000_000L / frameRate)
    }
    
    /**
     * Validate if shutter speed is compatible with frame rate
     * Shutter speed must be faster than or equal to frame duration
     */
    fun isValidShutterSpeed(shutterSpeedNs: Long, frameRate: Int): Boolean {
        val minSpeed = getMinShutterSpeed(frameRate)
        return shutterSpeedNs <= minSpeed
    }
    
    /**
     * Get display name for shutter speed
     */
    fun getDisplayName(shutterSpeedNs: Long): String {
        return when {
            shutterSpeedNs <= 0 -> "Auto"
            shutterSpeedNs >= 1_000_000_000L -> "${shutterSpeedNs / 1_000_000_000}s"
            else -> {
                val fraction = 1_000_000_000L / shutterSpeedNs
                "1/${fraction}s"
            }
        }
    }
    
    /**
     * Get all available shutter speeds for a given frame rate
     */
    fun getAvailableSpeeds(frameRate: Int): List<Pair<Long, String>> {
        val minSpeed = getMinShutterSpeed(frameRate)
        val allSpeeds = listOf(
            SPEED_1_30 to "1/30s",
            SPEED_1_60 to "1/60s",
            SPEED_1_125 to "1/125s",
            SPEED_1_250 to "1/250s",
            SPEED_1_500 to "1/500s",
            SPEED_1_1000 to "1/1000s",
            SPEED_1_2000 to "1/2000s",
            SPEED_1_4000 to "1/4000s",
            SPEED_1_8000 to "1/8000s"
        )
        return allSpeeds.filter { it.first <= minSpeed }
    }
    
    /**
     * Get shutter speed value from display name
     */
    fun fromDisplayName(displayName: String): Long? {
        val allSpeeds = mapOf(
            "1/30s" to SPEED_1_30,
            "1/60s" to SPEED_1_60,
            "1/125s" to SPEED_1_125,
            "1/250s" to SPEED_1_250,
            "1/500s" to SPEED_1_500,
            "1/1000s" to SPEED_1_1000,
            "1/2000s" to SPEED_1_2000,
            "1/4000s" to SPEED_1_4000,
            "1/8000s" to SPEED_1_8000
        )
        return allSpeeds[displayName]
    }
}

/**
 * Focus mode for video recording
 */
enum class FocusMode(val displayName: String) {
    CONTINUOUS_VIDEO("Continuous Video (Liên tục)"),
    CONTINUOUS_PICTURE("Continuous Picture"),
    AUTO("Auto (Tự động)"),
    MACRO("Macro (Cận cảnh)"),
    INFINITY("Infinity (Vô cực)"),
    FACE_DETECTION("Face Detection (Nhận diện khuôn mặt)")
}
