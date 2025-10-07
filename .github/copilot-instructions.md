# CogniNote - AI Coding Agent Instructions

## Project Overview

CogniNote is a **simplified, focused** Android note-taking app built with **Jetpack Compose**, **Room database**, **MVVM architecture**, and **Dagger Hilt** dependency injection. The app prioritizes **simplicity and daily usability** over feature complexity, following the philosophy of "Write → Find → Share".

## Architecture Patterns

### Simplified Architecture
- **3-Screen Navigation**: Notes List → Write/Edit Note → Search (no complex drawer/menu navigation)
- **Single Entity**: Only `Note` entity - removed Folder, Tag, NoteTemplate, Task entities for simplicity
- **Auto-Magic Features**: Auto-save, auto-tag extraction from #hashtags, auto-title generation
- **Repository Pattern**: `SimplifiedNoteRepository` handles all data operations

### Data Layer Philosophy
- **Room Database**: `CogniNoteDatabase.kt` with single `Note` entity
- **No Encryption**: Removed SQLCipher complexity for daily usability
- **Simple Folders**: String-based folder names instead of complex ID relationships
- **Auto-Tags**: Extract tags from content using #hashtag syntax automatically
- **No Soft Delete**: Hard delete only - no archive/trash complexity

### Core Conventions
- **Simple IDs**: Time-based note IDs (`note_${timestamp}_${random}`)
- **Auto-Processing**: Content → plainTextContent, #hashtags → tags automatically
- **Instant Search**: Search across title, content, and tags simultaneously
- **Material 3**: Clean, minimal UI with focus on content creation

## Development Workflow

### Build Commands
```bash
./gradlew build                    # Full project build
./gradlew clean build              # Clean build (fixes most issues)
./gradlew app:assembleDebug        # Debug APK only
```

### Key Development Features
- **Simplified UI**: Use `SimplifiedCogniNoteApp` as main entry point, not complex `CogniNoteApp`
- **Hot Reload**: Jetpack Compose preview support - focus on simplified screens in `screens/simplified/`
- **Database Inspection**: Single `notes` table - much simpler to debug
- **Auto-Magic**: Most features work automatically (save, tags, titles) - minimal user intervention needed

### Testing Strategy
- **Unit Tests**: ViewModels and repositories with coroutine testing
- **UI Tests**: Compose UI testing with `ui-test-junit4`
- **Database Tests**: Room testing with in-memory database

## Critical Integration Points

### Navigation Structure
- **Simple NavHost**: Only 3 routes in `SimplifiedCogniNoteApp.kt` - "notes", "write/{noteId}", "search"
- **No Drawer**: Removed complex navigation drawer - simple top bar with back button
- **FAB-Driven**: Single floating "Write" button creates new notes instantly

### State Management  
- **Auto-Save**: Notes save automatically after 1-second debounce - no manual save needed
- **Simple State**: ViewModels use basic `StateFlow` - no complex state management
- **Focus Management**: Auto-focus on content input for immediate typing experience

### Sharing System
- **Text Only**: Simple text sharing via Android share intents
- **No Complex Export**: Removed PDF, HTML, JSON complexity - copy/paste preferred
- **Instant Share**: Share button provides plain text immediately

## Project-Specific Gotchas

### Removed Complexity
- **No Encryption**: Removed SQLCipher, biometric auth, security complexity entirely
- **No Rich Text**: Simple `BasicTextField` with plain text - no formatting complexity  
- **No AI Features**: Disabled TensorFlow, ML Kit, OCR - focus on core note-taking
- **No Background Tasks**: Removed WorkManager, sync, backup complexity

### Performance Philosophy
- **Simple Queries**: Basic SQL queries only - no complex joins or FTS
- **Minimal Indexes**: Only essential indexes for sorting and basic search
- **Auto-Processing**: Tags and metadata extracted from content automatically

## Common Development Tasks

### Adding New Feature
1. Keep it simple - ask "do users need this daily?"
2. Add to `SimplifiedNoteRepository` if data-related
3. Update single `Note` entity if needed - avoid new entities
4. Use auto-magic processing over manual user configuration

### Database Changes
1. Modify `Note` entity in `data/entities/Note.kt`
2. Update `NoteDao` with simple queries only
3. Increment database version - destructive migration OK for simplicity
4. Update `SimplifiedNoteRepository` accordingly

### UI Improvements
1. Focus on the 3 core screens in `screens/simplified/`
2. Remove options rather than add them - simplify user choices
3. Make features invisible/automatic rather than configurable

## File Priorities for Context

**Core Architecture**: `CogniNoteApplication.kt`, `DatabaseModule.kt`, `SimplifiedCogniNoteApp.kt`, `CogniNoteDatabase.kt`

**Data Models**: `data/entities/Note.kt`, `data/entities/SimpleNoteTemplate.kt`

**Key Screens**: `screens/simplified/SimpleNotesListScreen.kt`, `screens/simplified/SimpleNoteEditScreen.kt`

**Build Configuration**: `app/build.gradle.kts` (contains feature flags and dependency status)