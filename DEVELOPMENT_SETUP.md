# CogniNote Development Setup

## Prerequisites
- macOS with at least 8GB RAM
- 10GB free disk space
- Internet connection for dependencies

## Setup Steps

### 1. Install Android Studio
1. Download from: https://developer.android.com/studio
2. Install Android Studio to Applications folder
3. Launch and complete setup wizard

### 2. SDK Configuration
- Install Android SDK API Level 34 (compileSdk in build.gradle)
- Install Android SDK Build-Tools
- Install Android Emulator

### 3. Open CogniNote Project
1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to: `/Users/mohmadnoorariffin/CogniNote`
4. Select the root folder and click "OK"

### 4. Sync Project
- Android Studio will automatically sync Gradle
- Install any missing dependencies when prompted
- This may take 5-10 minutes on first run

### 5. Set up Emulator
1. Tools → AVD Manager
2. Create Virtual Device
3. Choose Pixel 7 or similar
4. Download system image (API 34)
5. Finish setup

### 6. Run the App
1. Select your emulator from the device dropdown
2. Click the green "Run" button (▶️)
3. Wait for build and deployment

## Project Structure
```
CogniNote/
├── app/                    # Main application module
│   ├── src/main/java/     # Kotlin source files
│   ├── src/main/res/      # Resources (layouts, strings, etc.)
│   └── build.gradle.kts   # App-level build configuration
├── build.gradle.kts       # Project-level build configuration
└── settings.gradle.kts    # Project settings
```

## Key Features Implemented
- ✅ Note creation and editing with rich text
- ✅ SQLCipher encrypted database
- ✅ Full-text search with FTS5
- ✅ Tag system with autocomplete
- ✅ Folder organization
- ✅ Biometric authentication
- ✅ Material 3 UI design
- ✅ Dark/Light theme support

## Development Commands

### Build the project:
```bash
./gradlew build
```

### Run tests:
```bash
./gradlew test
```

### Clean build:
```bash
./gradlew clean build
```

## Troubleshooting

### Common Issues:
1. **"SDK not found"** - Install Android SDK via SDK Manager
2. **"Build failed"** - Sync project and clean build
3. **"Emulator won't start"** - Enable virtualization in BIOS/macOS settings
4. **"Dependencies not found"** - Check internet connection and sync again

### Getting Help:
- Check Android Studio's "Build" tab for error details
- Review logcat for runtime issues
- Ensure all dependencies in build.gradle are available

## Next Development Steps
1. Test core functionality (create/edit/delete notes)
2. Implement remaining UI screens
3. Add advanced features (OCR, voice notes, etc.)
4. Test on physical device
5. Prepare for release builds
