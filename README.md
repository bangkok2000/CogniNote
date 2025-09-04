# CogniNote - Advanced Notes App

A modern, feature-rich Android notes application built with Jetpack Compose, Room database, and Material Design 3.

## 🚀 Features

### Core Functionality
- **Rich Text Editor** - Full-screen text editing with automatic scrolling
- **Folder Management** - Create, organize, and manage notes in folders
- **Search & Filter** - Advanced search with tag filtering
- **Export Functionality** - Export notes as PDF, Text, Markdown, HTML, JSON, and Evernote formats
- **Templates System** - Pre-built note templates for quick creation
- **Task Management** - Built-in task and reminder system
- **AI Content Assistant** - Smart content suggestions and summarization

### Advanced Features
- **Biometric Security** - Fingerprint and face authentication
- **Cross-Platform Sync** - Cloud synchronization support
- **Advanced Search** - Full-text search with filters
- **Export/Import** - Multiple format support
- **Nested Folders** - Hierarchical organization
- **Tag System** - Flexible tagging and categorization
- **Pin & Archive** - Note organization tools

## 🛠️ Technology Stack

- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Database**: Room with SQLite
- **Dependency Injection**: Dagger Hilt
- **Navigation**: Navigation Compose
- **Async Operations**: Kotlin Coroutines & Flow
- **Material Design**: Material 3
- **Security**: Biometric authentication
- **Export**: Multiple format support (PDF, Text, Markdown, HTML, JSON)

## 📱 Screenshots

*Screenshots will be added here*

## 🏗️ Project Structure

```
app/
├── src/main/java/com/cogninote/app/
│   ├── data/
│   │   ├── dao/           # Room DAOs
│   │   ├── database/      # Database configuration
│   │   ├── entities/      # Data models
│   │   ├── repository/    # Repository layer
│   │   └── security/      # Security utilities
│   ├── di/                # Dependency injection
│   ├── presentation/
│   │   ├── ui/
│   │   │   ├── components/    # Reusable UI components
│   │   │   ├── screens/       # App screens
│   │   │   └── theme/         # App theming
│   │   └── viewmodel/     # ViewModels
│   ├── services/          # Background services
│   ├── sharing/           # Export/import functionality
│   └── utils/             # Utility functions
└── src/main/res/          # Resources
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 8 or later
- Android SDK 26 or later

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/CogniNote.git
cd CogniNote
```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or device

## 📋 Features Implementation Status

### ✅ Completed Features
- [x] Rich text editor with full-screen editing
- [x] Folder management system
- [x] Search functionality with tag filtering
- [x] Export functionality (PDF, Text, Markdown, HTML, JSON)
- [x] Template system
- [x] Task management framework
- [x] AI content assistant
- [x] Biometric authentication
- [x] Navigation drawer and overflow menu
- [x] Modern UI with Material Design 3
- [x] Database with Room
- [x] MVVM architecture
- [x] Dependency injection with Hilt

### 🔄 In Progress
- [ ] Cross-platform synchronization
- [ ] Advanced PDF generation
- [ ] Cloud storage integration
- [ ] Widget support
- [ ] Advanced AI features

### 📋 Planned Features
- [ ] Voice notes
- [ ] Handwriting recognition
- [ ] Collaborative editing
- [ ] Advanced templates
- [ ] Plugin system
- [ ] Desktop companion app

## 🧪 Testing

Run tests using:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## 📦 Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Material Design 3 for the beautiful UI components
- Jetpack Compose team for the modern UI framework
- Android team for the excellent development tools
- Open source community for inspiration and libraries

## 📞 Support

If you have any questions or need help, please:
- Open an issue on GitHub
- Contact the development team
- Check the documentation

## 🔮 Roadmap

### Version 1.1
- Enhanced PDF export with proper formatting
- Batch operations
- Advanced search filters
- Custom themes

### Version 1.2
- Cloud synchronization
- Collaborative features
- Advanced AI integration
- Plugin system

### Version 2.0
- Cross-platform support
- Desktop companion
- Advanced security features
- Enterprise features

---

**CogniNote** - Your intelligent notes companion 🧠📝