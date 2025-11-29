# Hidden Camera App - Development Progress

## Project Overview
App Android quay video ẩn từ camera trước/sau, hỗ trợ chạy nền và khi khóa màn hình.

## Completed Tasks

### 1. Project Setup & Dependencies ✅
- Updated build.gradle.kts with necessary dependencies:
  - CameraX (camera-core, camera2, lifecycle, video) 1.4.0
  - DataStore Preferences 1.1.1
  - Navigation Compose 2.8.4
  - Hilt (Dependency Injection) 2.51.1
  - Lifecycle Service
  - Material Icons Extended
- Updated libs.versions.toml with all version catalogs
- Configured Kotlin 2.0.21 with Java 17
- compileSdk = 36

### 2. Permissions Configuration ✅
- AndroidManifest.xml updated with:
  - CAMERA, RECORD_AUDIO
  - WRITE_EXTERNAL_STORAGE, READ_MEDIA_VIDEO
  - FOREGROUND_SERVICE, FOREGROUND_SERVICE_CAMERA, FOREGROUND_SERVICE_MICROPHONE
  - POST_NOTIFICATIONS
  - WAKE_LOCK (for recording when screen off)
  - RECEIVE_BOOT_COMPLETED
  - **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS** *(NEW - xin quyền tắt battery optimization)*

### 3. Clean Architecture Structure ✅

#### Domain Layer
- **Models:**
  - VideoSettings.kt - Settings model (camera facing, resolution, fps, bitrate, audio source, **orientation, flashEnabled**)
  - RecordingState.kt - Recording state sealed class (Idle, Starting, Recording, Paused, Stopping, Error)
  - **VideoOrientation** enum - PORTRAIT ("Portrait (Dọc)"), LANDSCAPE ("Landscape (Ngang)")

- **Repository Interfaces:**
  - SettingsRepository.kt - Settings CRUD operations
  - VideoRecordingRepository.kt - Recording control operations

- **Use Cases:**
  - StartRecordingUseCase.kt
  - StopRecordingUseCase.kt
  - PauseRecordingUseCase.kt
  - ResumeRecordingUseCase.kt
  - GetRecordingStateUseCase.kt
  - GetSettingsUseCase.kt
  - UpdateSettingsUseCase.kt (added **setOrientation, setFlashEnabled**)

#### Data Layer
- **Local Storage:**
  - SettingsDataStore.kt - DataStore Preferences implementation
    - Added: ORIENTATION, FLASH_ENABLED keys

- **Repositories:**
  - SettingsRepositoryImpl.kt - Settings repository implementation
  - VideoRecordingRepositoryImpl.kt - Recording repository implementation

- **Services:**
  - VideoRecordingService.kt - Foreground service for background recording
    - ✅ **Hỗ trợ Flash/Torch** (back camera only)
    - ✅ **Hỗ trợ Video Orientation** (Portrait/Landscape) via targetRotation
    - ✅ **FallbackStrategy** cho QualitySelector (fix lỗi camera không hỗ trợ quality)
    - ✅ **Improved Notification** với thời gian quay, camera info
    - ✅ **Widget integration** - cập nhật widget khi recording state thay đổi

#### Presentation Layer
- **DI:**
  - RepositoryModule.kt - Hilt module for dependency injection

- **Navigation:**
  - AppNavigation.kt - Navigation component setup

- **Screens:**
  - **Home:**
    - HomeScreen.kt - Main recording screen UI
    - HomeViewModel.kt - Home screen ViewModel
    - **BatteryOptimizationCard** *(NEW)* - Warning card để xin quyền battery optimization
  - **Settings:**
    - SettingsScreen.kt - Settings screen UI
    - SettingsViewModel.kt - Settings screen ViewModel

- **Widget (NEW):**
  - RecordingWidgetReceiver.kt - Widget để quay video nhanh từ home screen
  - widget_recording.xml - Layout cho widget
  - recording_widget_info.xml - Widget metadata
  - widget_background.xml, widget_background_recording.xml - Widget backgrounds
  - ic_videocam.xml, ic_stop_recording.xml - Widget icons

- **Utilities (NEW):**
  - BatteryOptimizationHelper.kt - Helper để quản lý battery optimization

- **Application:**
  - HiddenCamApplication.kt - Application class with Hilt and notification channel
  - MainActivity.kt - Main activity with permission handling

### 4. Features Implemented

#### Video Recording
- [x] Record from front or back camera
- [x] Background recording (foreground service)
- [x] Recording continues when screen is locked (WakeLock)
- [x] Pause/Resume recording
- [x] Stop recording
- [x] Save videos to Movies/HiddenCam folder
- [x] **Flash/Torch support** *(NEW - back camera only)*
- [x] **Video orientation - Portrait/Landscape** *(NEW)*
- [x] **FallbackStrategy for unsupported video qualities** *(NEW - fix crash on some devices)*

#### Settings
- [x] Camera selection (Front/Back)
- [x] Resolution (480p, 720p, 1080p, 4K)
- [x] Frame rate (24, 30, 60 fps)
- [x] Bitrate quality (Low, Medium, High, Ultra)
- [x] Audio source (None, Microphone, Camcorder)
- [x] Volume button control toggle
- [x] Power button control toggle
- [x] **Video Orientation (Portrait/Landscape)** *(NEW)*
- [x] **Flash Light toggle** *(NEW)*
- [x] **App Icon Disguise** - 3 icons: X (default), Molecule (calculator), Gear (settings) *(NEW)*
- [x] **App Name Disguise** - 4 names: HiddenCam, Calculator, Notes, Settings *(NEW)*
- [x] **ISO Control** - Auto, 100, 200, 400, 800, 1600, 3200, 6400, Max *(NEW)*
- [x] **Exposure Compensation** - -4 EV to +4 EV với visual slider *(NEW)*
- [x] **Shutter Speed** - Auto/Custom với validation theo frame rate *(NEW)*
- [x] **Focus Mode** - 6 chế độ: Continuous Video, Continuous Picture, Auto, Macro, Infinity, Face Detection *(NEW)*

#### Camera Preview (NEW)
- [x] **Camera Preview Screen** - Xem trước camera trước khi quay
- [x] **Camera Switch** - Chuyển đổi front/back camera
- [x] **Flash Toggle** - Bật/tắt flash trong preview (back camera only)
- [x] **Settings Display** - Hiển thị resolution, fps, ISO, focus mode

#### UI Features
- [x] Material 3 design
- [x] Recording status display
- [x] Duration timer with pulse animation
- [x] Quick tips card
- [x] Permission request handling
- [x] **Improved notification với duration, camera info, stop button** *(NEW)*
- [x] **Battery optimization warning card** *(NEW)*

#### Background Optimization (NEW)
- [x] **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS permission**
- [x] **BatteryOptimizationHelper utility class**
- [x] **UI card để request battery optimization exemption**
- [x] **Link đến Battery settings để người dùng tự cấu hình**

#### Widget (NEW)
- [x] **Recording Widget** - tap để start/stop recording từ home screen
- [x] **Widget cập nhật với recording state và duration**
- [x] **Icon thay đổi khi đang quay (videocam ↔ stop)**
- [x] **Compact 1x1 Widget** - chỉ icon, không text *(UPDATED)*
- [x] **SharedPreferences state** - fix lỗi mất trạng thái khi process bị kill *(FIXED)*
- [x] **Circular widget design** - background oval thay vì rectangle *(UPDATED)*

### 5. In Progress / TODO

#### Hardware Button Controls
- [ ] Long press volume down to start recording (partially implemented)
- [ ] Double tap power button to stop recording (needs AccessibilityService)

#### Improvements Needed
- [ ] Test on physical device
- [x] Add video gallery screen ✅
- [ ] Add video playback
- [ ] Improve hardware button detection

### 6. New Features - Session 5: Security & Video Gallery ✅

#### App Lock Feature
- [x] **PIN Lock** - 4-digit PIN protection
- [x] **PIN Setup Mode** - Create and confirm PIN
- [x] **PIN Verify Mode** - Unlock app with PIN
- [x] **SHA-256 PIN Hashing** - Secure PIN storage
- [x] **Biometric Option** - Fingerprint/Face unlock
- [x] **BiometricPrompt Integration** - Native Android biometric API

#### Video Gallery Feature
- [x] **Video Gallery Screen** - View all recorded videos
- [x] **Video Thumbnails** - Coil image loading
- [x] **Video Info Display** - Duration, size, date
- [x] **Video Playback** - Open with external player
- [x] **Video Sharing** - Share videos via intent
- [x] **Video Deletion** - Delete with confirmation dialog
- [x] **Empty State UI** - Message when no videos

#### Bluetooth Remote Feature
- [x] **Bluetooth Remote Setting** - Enable/disable in settings
- [x] **Device Pairing UI** - Select from bonded devices
- [x] **BluetoothRemoteReceiver** - Media button receiver
- [x] **Toggle Recording** - PLAY/PAUSE button to toggle
- [x] **Stop Recording** - STOP button support

#### Files Created
| File | Purpose |
|------|---------|
| AppLockSettings.kt | Model for app lock config |
| BluetoothRemoteSettings.kt | Model for Bluetooth remote config |
| VideoItem.kt | Model for video items with formatted properties |
| SecurityDataStore.kt | DataStore for security settings (PIN, biometric, Bluetooth) |
| VideoGalleryRepository.kt | Repository for MediaStore video queries |
| PinLockScreen.kt | PIN lock UI with numpad and biometric |
| VideoGalleryScreen.kt | Video gallery grid with thumbnails |
| VideoGalleryViewModel.kt | ViewModel for gallery screen |
| SecurityModule.kt | Hilt module for SecurityDataStore and VideoGalleryRepository |
| BluetoothRemoteReceiver.kt | BroadcastReceiver for media button events |

#### Files Modified
| File | Changes |
|------|---------|
| SettingsScreen.kt | + Security section (App Lock), + Bluetooth Remote section |
| HomeScreen.kt | + Video gallery button (VideoLibrary icon) |
| AppNavigation.kt | + Routes: PinLock, PinSetup, VideoGallery |
| MainActivity.kt | + SecurityDataStore injection, + isAppUnlocked state |
| VideoRecordingService.kt | + ACTION_TOGGLE_RECORDING for Bluetooth remote |
| build.gradle.kts | + biometric 1.1.0, + coil-compose 2.5.0 |
| AndroidManifest.xml | + Bluetooth permissions, + USE_BIOMETRIC, + BluetoothRemoteReceiver |

## File Structure
```
app/src/main/java/com/example/hiddencam/
├── HiddenCamApplication.kt
├── data/
│   ├── local/
│   │   └── SettingsDataStore.kt
│   ├── repository/
│   │   ├── SettingsRepositoryImpl.kt
│   │   └── VideoRecordingRepositoryImpl.kt
│   └── service/
│       └── VideoRecordingService.kt
├── di/
│   └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   ├── RecordingState.kt
│   │   └── VideoSettings.kt (+ VideoOrientation enum)
│   ├── repository/
│   │   ├── SettingsRepository.kt
│   │   └── VideoRecordingRepository.kt
│   └── usecase/
│       ├── GetRecordingStateUseCase.kt
│       ├── GetSettingsUseCase.kt
│       ├── PauseRecordingUseCase.kt
│       ├── ResumeRecordingUseCase.kt
│       ├── StartRecordingUseCase.kt
│       ├── StopRecordingUseCase.kt
│       └── UpdateSettingsUseCase.kt
├── presentation/
│   ├── MainActivity.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt (+ BatteryOptimizationCard, Preview button)
│   │   │   └── HomeViewModel.kt
│   │   ├── preview/
│   │   │   └── CameraPreviewScreen.kt (NEW - camera preview)
│   │   └── settings/
│   │       ├── SettingsScreen.kt (+ Orientation, Flash, Advanced Camera Settings UI)
│   │       └── SettingsViewModel.kt
│   ├── util/
│   │   └── BatteryOptimizationHelper.kt (NEW)
│   └── widget/
│       └── RecordingWidgetReceiver.kt (NEW)
└── ui/
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt

app/src/main/res/
├── drawable/
│   ├── ic_videocam.xml (NEW)
│   ├── ic_stop_recording.xml (NEW)
│   ├── widget_background.xml (NEW)
│   └── widget_background_recording.xml (NEW)
├── layout/
│   └── widget_recording.xml (NEW)
├── values/
│   └── strings.xml (UPDATED with widget strings)
└── xml/
    └── recording_widget_info.xml (NEW)
```

## Recent Changes Log

### 2025-01-XX - Session 4: Advanced Camera Settings & Camera Preview

#### New Features

1. **Advanced Camera Settings**
   - **ISO Control**: Auto, 100, 200, 400, 800, 1600, 3200, 6400, Max ISO
   - **Exposure Compensation**: -4 EV to +4 EV với visual slider
   - **Shutter Speed**: Auto hoặc Custom (1/30s đến 1/8000s)
   - **Shutter Speed Validation**: Tự động lọc tốc độ phù hợp với frame rate (VD: 30fps → min 1/30s)
   - **Focus Mode**: 6 chế độ
     - Continuous Video (Liên tục)
     - Continuous Picture
     - Auto (Tự động)
     - Macro (Cận cảnh)
     - Infinity (Vô cực)
     - Face Detection (Nhận diện khuôn mặt)

2. **Camera Preview Screen**
   - Xem trước camera trước khi quay
   - Hiển thị thông tin settings hiện tại (resolution, fps, ISO, focus mode)
   - Nút switch camera (front/back)
   - Nút bật/tắt flash (back camera only)
   - Camera facing indicator

3. **Home Screen Update**
   - Thêm nút Preview (👁️) trên app bar
   - Navigate đến Camera Preview screen

#### Technical Implementation

1. **Domain Layer - VideoSettings.kt**
   ```kotlin
   // New enums
   enum class IsoMode(displayName: String, isoValue: Int?)
   enum class ShutterSpeedMode(displayName: String)
   enum class FocusMode(displayName: String)
   object ShutterSpeedValues {
       // Constants: SPEED_1_30, SPEED_1_60, ... SPEED_1_8000
       fun getMinShutterSpeed(frameRate: Int): Long
       fun isValidShutterSpeed(shutterSpeedNs: Long, frameRate: Int): Boolean
       fun getDisplayName(shutterSpeedNs: Long): String
       fun getAvailableSpeeds(frameRate: Int): List<Pair<Long, String>>
       fun fromDisplayName(displayName: String): Long?
   }
   
   // New fields in VideoSettings
   val isoMode: IsoMode = IsoMode.AUTO
   val exposureCompensation: Int = 0
   val shutterSpeedMode: ShutterSpeedMode = ShutterSpeedMode.AUTO
   val customShutterSpeed: Long = 0L
   val focusMode: FocusMode = FocusMode.CONTINUOUS_VIDEO
   ```

2. **Data Layer - SettingsDataStore.kt**
   ```kotlin
   // New keys
   ISO_MODE, EXPOSURE_COMPENSATION, SHUTTER_SPEED_MODE, CUSTOM_SHUTTER_SPEED, FOCUS_MODE
   ```

3. **Service Layer - VideoRecordingService.kt**
   - Camera2 Interop để apply advanced settings
   - `@OptIn(ExperimentalCamera2Interop::class)`
   - `Camera2CameraControl.setCaptureRequestOptions()` với:
     - `CaptureRequest.SENSOR_SENSITIVITY` (ISO)
     - `CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION` (EV)
     - `CaptureRequest.SENSOR_EXPOSURE_TIME` (Shutter speed)
     - `CaptureRequest.CONTROL_AF_MODE` (Focus mode)
     - `CaptureRequest.STATISTICS_FACE_DETECT_MODE` (Face detection)
     - `CaptureRequest.LENS_FOCUS_DISTANCE` (For infinity focus)

4. **Presentation Layer**
   - `SettingsScreen.kt`: New "Advanced Camera Settings" section
   - `CameraPreviewScreen.kt`: New camera preview screen
   - `AppNavigation.kt`: Added CameraPreview route

#### Files Created
| File | Purpose |
|------|---------|
| CameraPreviewScreen.kt | Camera preview UI với live camera feed |

#### Files Modified
| File | Changes |
|------|---------|
| VideoSettings.kt | + IsoMode, ShutterSpeedMode, FocusMode enums, ShutterSpeedValues object, new fields |
| SettingsDataStore.kt | + Keys for new settings, getter/setter methods |
| SettingsRepository.kt | + 5 new interface methods |
| SettingsRepositoryImpl.kt | + 5 new implementations |
| UpdateSettingsUseCase.kt | + 5 new setter methods |
| SettingsViewModel.kt | + Functions với validation, getAvailableShutterSpeeds() |
| SettingsScreen.kt | + Advanced Camera Settings section, ExposureCompensationItem composable |
| VideoRecordingService.kt | + applyAdvancedCameraSettings() with Camera2 interop |
| HomeScreen.kt | + Preview button, onNavigateToPreview param |
| AppNavigation.kt | + CameraPreview route |
| libs.versions.toml | + accompanist version, camera-view |
| build.gradle.kts | + accompanist-permissions, camera-view dependencies |

#### Dependencies Added
```toml
# libs.versions.toml
accompanist = "0.34.0"
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "cameraX" }
accompanist-permissions = { group = "com.google.accompanist", name = "accompanist-permissions", version.ref = "accompanist" }
```

---

### 2025-11-29 - Session 3: App Disguise & Widget Fix

#### New Features
1. **App Icon Disguise**
   - 3 icon options: X Logo (default), Molecule (calculator style), Gear (settings style)
   - Created vector drawables cho từng icon
   - Dùng `activity-alias` pattern trong AndroidManifest
   - `PackageManager.setComponentEnabledSetting()` để switch icon
   - Icons được tạo dạng adaptive icon (foreground + background)

2. **App Name Disguise**
   - 4 tên: HiddenCam, Calculator, Notes, Settings
   - Mỗi activity-alias có label riêng
   - Người dùng chọn trong Settings > App Disguise section

3. **Widget Fix & Compact Design**
   - **Fix bug**: Widget không record khi tap
   - **Root cause**: Static variable `isRecording` bị reset khi process killed
   - **Solution**: Dùng SharedPreferences ("widget_prefs") để lưu trạng thái
   - **Compact size**: 1x1 cell (56dp), chỉ icon không text
   - **Circular design**: Background oval thay vì rectangle
   - **Better logging**: Thêm Log.d statements để debug

#### Files Created
| File | Purpose |
|------|---------|
| ic_launcher_default.xml | X logo icon (drawable) |
| ic_launcher_molecule.xml | Molecule/calculator icon (drawable) |
| ic_launcher_gear.xml | Gear/settings icon (drawable) |
| mipmap-anydpi/ic_launcher_default.xml | Adaptive icon wrapper |
| mipmap-anydpi/ic_launcher_molecule.xml | Adaptive icon wrapper |
| mipmap-anydpi/ic_launcher_gear.xml | Adaptive icon wrapper |
| widget_icon_bg.xml | Circular background for widget icon |

#### Files Modified
| File | Changes |
|------|---------|
| VideoSettings.kt | + `AppIcon` enum, `AppName` enum, `appIcon`, `appName` fields |
| SettingsDataStore.kt | + APP_ICON, APP_NAME keys |
| SettingsRepository.kt | + `setAppIcon()`, `setAppName()` |
| SettingsRepositoryImpl.kt | + Implementation |
| UpdateSettingsUseCase.kt | + `setAppIcon()`, `setAppName()` |
| SettingsViewModel.kt | + Icon switching with PackageManager |
| SettingsScreen.kt | + "App Disguise" section với dropdowns |
| AndroidManifest.xml | + 3 activity-alias (MainActivityDefault, MainActivityMolecule, MainActivityGear) |
| RecordingWidgetReceiver.kt | **Rewrite**: SharedPreferences instead of static, better logging |
| widget_recording.xml | Simplified to FrameLayout + ImageView only |
| recording_widget_info.xml | 1x1 cell (56dp), resizeMode="none" |
| widget_background.xml | Changed to oval shape |
| widget_background_recording.xml | Changed to oval shape |
| VideoRecordingRepositoryImpl.kt | + SettingsDataStore injection, `getSettings()` |
| VideoRecordingService.kt | + Load settings in onStartCommand for widget starts |
| colors.xml | + launcher_background colors |
| strings.xml | + app_name_calculator, app_name_notes, app_name_settings |

### 2025-11-28 - Session 2: Feature Enhancements

#### Bug Fixes
1. **Fix Camera QualitySelector Error**
   - Vấn đề: Crash "Unable to find supported quality by QualitySelector" trên một số thiết bị
   - Giải pháp: Thêm `FallbackStrategy.higherQualityOrLowerThan(Quality.SD)` và kiểm tra supportedQualities

2. **Fix Notification Display**
   - Vấn đề: Notification không hiển thị đúng thông tin
   - Giải pháp: Cải thiện notification với duration, camera info, chronometer

#### New Features
1. **Video Orientation Setting**
   - Thêm enum `VideoOrientation` (PORTRAIT, LANDSCAPE)
   - UI dropdown trong Settings
   - Service áp dụng `targetRotation` khi quay

2. **Flash Light Setting**
   - Thêm toggle `flashEnabled` trong Settings
   - Service gọi `camera.cameraControl.enableTorch()` (chỉ back camera)

3. **Battery Optimization Request**
   - Permission: `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
   - Helper class: `BatteryOptimizationHelper.kt`
   - UI: `BatteryOptimizationCard` trong HomeScreen

4. **Recording Widget**
   - `RecordingWidgetReceiver.kt` extends `AppWidgetProvider`
   - Layout: `widget_recording.xml`
   - Config: `recording_widget_info.xml`
   - Backgrounds: `widget_background.xml`, `widget_background_recording.xml`
   - Icons: `ic_videocam.xml`, `ic_stop_recording.xml`

5. **Improved Notifications**
   - Hiển thị: "🔴 Recording Video"
   - Duration với chronometer
   - Camera info: "BACK • 720p (HD)"
   - Stop button action

#### Files Modified
| File | Changes |
|------|---------|
| VideoSettings.kt | + `orientation: VideoOrientation`, `flashEnabled: Boolean` |
| SettingsDataStore.kt | + ORIENTATION, FLASH_ENABLED keys |
| SettingsRepository.kt | + `setOrientation()`, `setFlashEnabled()` |
| SettingsRepositoryImpl.kt | + Implementation |
| UpdateSettingsUseCase.kt | + `setOrientation()`, `setFlashEnabled()` |
| SettingsViewModel.kt | + UI binding methods |
| SettingsScreen.kt | + Orientation dropdown, Flash toggle |
| VideoRecordingService.kt | + Flash control, orientation, notification, widget |
| HomeScreen.kt | + BatteryOptimizationCard |
| AndroidManifest.xml | + Battery permission, widget receiver |

#### Files Created
| File | Purpose |
|------|---------|
| BatteryOptimizationHelper.kt | Battery optimization utilities |
| RecordingWidgetReceiver.kt | Widget AppWidgetProvider |
| recording_widget_info.xml | Widget metadata |
| widget_recording.xml | Widget layout |
| widget_background.xml | Widget normal background |
| widget_background_recording.xml | Widget recording background |
| ic_videocam.xml | Widget start icon |
| ic_stop_recording.xml | Widget stop icon |

### 2025-11-28 - Session 1: Initial Development
- Tạo project structure với Clean Architecture
- Implement tất cả layers: Domain, Data, Presentation
- Setup CameraX, Hilt, DataStore, Navigation
- Tạo UI với Material 3

## Build & Run
1. Open project in Android Studio
2. Sync Gradle files
3. Build and run on device (physical device recommended for camera testing)
4. Grant all permissions when prompted
5. **Khuyến nghị: Cho phép battery optimization exemption để chạy nền tốt hơn**

## Known Issues
- Hardware button detection cần AccessibilityService cho power button
- gradle-wrapper.jar có thể bị thiếu, cần chạy `gradle wrapper` để tạo lại

## Last Updated
- **Date:** 2025-01-XX
- **Status:** Core functionality complete + App Disguise + Advanced Camera Settings + Camera Preview + v1.2 Features
- **New Features:** Video encryption, USB Camera support, Loop recording, Bluetooth audio sources
- **Next:** Test trên physical device, fix remaining issues

---

## Session 6: Advanced Recording Features (v1.2)

### New Features

#### 1. Video Encryption 🔐
- **AES-GCM Encryption** - Videos encrypted with 256-bit AES
- **PIN-Based Key Derivation** - Uses app lock PIN via SHA-256 to derive encryption key
- **Automatic Encryption** - When enabled, videos are encrypted after recording
- **Encrypted File Storage** - Saved to Movies/HiddenCam/Encrypted with .enc extension
- **Gallery Decryption** - Ability to decrypt and play encrypted videos

#### 2. USB Camera (OTG) Support 📹
- **External Camera Detection** - Detects USB cameras connected via OTG
- **Camera Selection** - Added USB option to camera selector (Front/Back/USB)
- **Fallback Handling** - Shows error message if no USB camera found

#### 3. Bluetooth Audio Sources 🎤
- **Phone Microphone** - Default audio source
- **Bluetooth Headset** - Record audio from connected Bluetooth headset
- **Mixed Mode** - Record from both phone mic and Bluetooth simultaneously

#### 4. Recording Modes 🔄
- **Manual Mode** - Recording stops only when user stops it
- **Until Storage Full** - Recording continues until storage is nearly full (~50MB remaining)
- **Loop Recording** - Automatically deletes oldest recordings when storage is low
  - Configurable minimum free GB (1, 2, 5, 10 GB options)
  - Automatic cleanup during recording

### Technical Implementation

#### New Files Created
| File | Purpose |
|------|---------|
| `util/VideoEncryptionUtil.kt` | AES-GCM encryption/decryption for videos |
| `util/StorageUtil.kt` | Storage monitoring and management for loop recording |

#### Files Modified
| File | Changes |
|------|---------|
| `domain/model/VideoSettings.kt` | + RecordingMode enum, USB in CameraFacing, BLUETOOTH/MIXED in AudioSource |
| `data/datastore/SecurityDataStore.kt` | + encryptVideo Flow, setEncryptVideo(), getEncryptionPin(), APP_LOCK_PIN key |
| `data/local/SettingsDataStore.kt` | + RECORDING_MODE, LOOP_RECORDING_MIN_FREE_GB keys |
| `domain/repository/SettingsRepository.kt` | + setRecordingMode(), setLoopRecordingMinFreeGB() |
| `data/repository/SettingsRepositoryImpl.kt` | + Recording mode implementation |
| `domain/usecase/UpdateSettingsUseCase.kt` | + Recording mode update methods |
| `presentation/screens/settings/SettingsViewModel.kt` | + RecordingMode, loopRecordingMinFreeGB |
| `presentation/screens/settings/SettingsScreen.kt` | + Recording Mode section, Video Encryption toggle, USB camera selector |
| `data/service/VideoRecordingService.kt` | + USB camera binding, storage monitoring, encryption |

#### VideoEncryptionUtil Features
```kotlin
object VideoEncryptionUtil {
    // AES-256-GCM encryption
    fun encryptFile(inputFile: File, outputFile: File, pin: String): Boolean
    fun decryptFile(encryptedFile: File, outputFile: File, pin: String): Boolean
    fun encryptToMediaStore(contentResolver, sourceFile, pin, displayName): Uri?
    fun decryptToTemp(encryptedFile: File, pin: String, cacheDir: File): File?
    fun verifyPin(encryptedFile: File, pin: String): Boolean
    fun isEncrypted(file: File): Boolean
}
```

#### StorageUtil Features
```kotlin
object StorageUtil {
    fun getAvailableStorageGB(): Double
    fun isStorageLow(minFreeGB: Int): Boolean
    fun hasEnoughStorageForRecording(requiredMB: Long): Boolean
    fun getHiddenCamRecordings(contentResolver): List<VideoFileInfo>
    fun deleteOldestRecording(contentResolver): Long
    fun freeUpStorage(contentResolver, minFreeGB): Long
    fun estimateRecordingTimeSeconds(bitrateKbps: Int): Long
}
```

#### VideoRecordingService Updates
- **USB Camera Detection** - Uses Camera2CameraInfo to find LENS_FACING_EXTERNAL cameras
- **Storage Monitoring** - Background job checks storage every 30 seconds
- **Encryption Pipeline** - Encrypts video after recording when enabled
- **Loop Recording Logic** - Deletes oldest files when storage threshold reached

### Settings UI Changes

#### Security Section (when App Lock enabled)
- App Lock toggle
- Change PIN
- Biometric Unlock toggle  
- Lock Timeout dropdown
- **Video Encryption toggle** (NEW)

#### Recording Mode Section (NEW)
- Recording Mode dropdown (Manual/Until Full/Loop)
- Minimum Free Storage dropdown (only shown for Loop mode)
- Mode description text

#### Camera Selection (UPDATED)
- Front / Back / USB selector buttons
- USB icon for external cameras

#### Audio Settings (UPDATED)
- Audio Source dropdown now includes:
  - No Audio
  - Phone Microphone
  - Camcorder (optimized)
  - Bluetooth Headset (NEW)
  - Phone + Bluetooth Mixed (NEW)

### Permissions (No changes needed)
- USB cameras use existing CAMERA permission
- Bluetooth audio uses existing RECORD_AUDIO permission

### Version
- **versionCode:** 3
- **versionName:** 1.2

---
