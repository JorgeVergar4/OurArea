# OurArea 🌍

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Min SDK](https://img.shields.io/badge/minSdk-25-orange.svg)
![Target SDK](https://img.shields.io/badge/targetSdk-36-orange.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

### 📍 Discover and create geolocated events near you

**Offline-First Architecture** • **Real-Time Sync** • **Google Maps Integration**

[Features](#-features) • [Tech Stack](#-tech-stack) • [Installation](#-installation) • [API Integration](#-api-integration-xano)

</div>

---

## 📋 Overview

**OurArea** is a native Android application that allows users to discover, create, and share geolocated events in real-time. Built with **Jetpack Compose** and **Kotlin**, the app combines interactive maps with an offline-first architecture for a seamless user experience.

### ✨ Key Highlights

- 🔄 **Offline-First**: Works without internet, syncs when available
- 🗺️ **Interactive Maps**: Google Maps integration with real-time geolocation
- 🔐 **Secure Authentication**: PBKDF2 password encryption + JWT tokens
- 🎭 **Role-Based Access**: Admin, Moderator, Organizer, and User roles
- 🌐 **Xano Backend**: Full REST API integration with cloud sync
- 📱 **Responsive Design**: Optimized for all screen sizes and orientations

---

## 🚀 Features

### 🔐 User Authentication & Authorization
- **Secure Login/Register** with email validation
- **PBKDF2 password encryption** (10,000 iterations, 256-bit keys)
- **Automatic session persistence** using DataStore Preferences
- **Role-based permissions** (Admin, Moderator, Organizer, User)
- **JWT token authentication** with Xano backend

### 🗺️ Interactive Maps
- **Google Maps integration** for event visualization
- **Real-time geolocation** with distance calculation
- **Draggable markers** and custom location picker
- **Center on user location** button
- **Marker clustering** for better performance

### 📅 Event Management
- **Create events** with title, description, location, and categories
- **Image support** (local storage only - see note below)
- **Category filtering**: Music, Food, Art, Sports, Family, Free
- **Smart search** functionality
- **Event details** with Google Maps navigation
- **Edit/Delete** events (permission-based)

### 🔄 Offline-First Architecture
- **Local SQLite database** (Room) for data persistence
- **Works without internet** - events saved locally first
- **Automatic background sync** when connection is available
- **Real-time updates** from Xano API
- **Conflict resolution** with server-wins strategy

### 🎨 Modern UI/UX
- **Material Design 3** implementation
- **Fully responsive** (portrait & landscape)
- **Smooth animations** and transitions
- **Custom permission dialogs**
- **Dark mode ready** (theme system in place)

---

## ⚠️ Important Note: Image Storage Limitation

**The Xano API does NOT support image uploads in the current plan.**

- ✅ Event images are stored **locally on the device**
- ✅ Events sync to Xano **without images**
- ❌ Images are **NOT uploaded** to Xano cloud storage
- ❌ Images will **NOT sync** across multiple devices

**Why?**  
Xano's free/basic tier does not include file storage capabilities. To enable cloud image storage, you would need:
1. A Xano paid plan with file storage
2. Or integrate with a third-party service (AWS S3, Cloudinary, etc.)

**Current behavior:**
- Images are saved to device's internal storage: `app_dir/files/event_images/`
- Image paths are stored in the local Room database
- When syncing to Xano, the `image_url` field is empty or contains the local path (not accessible from other devices)

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.0.21 | Programming language |
| **Jetpack Compose** | 2024.10.00 | Modern declarative UI |
| **Room Database** | 2.6.1 | Local SQLite persistence |
| **Coroutines & Flow** | 1.9.0 | Async programming |
| **ViewModel** | 2.8.7 | MVVM architecture |
| **Navigation Compose** | 2.8.3 | Screen navigation |
| **Retrofit** | 2.9.0 | REST API client |
| **Xano Backend** | - | Cloud database & API |
| **Google Maps Compose** | 6.2.1 | Interactive maps |
| **DataStore** | 1.1.1 | Encrypted preferences |
| **Coil** | 2.7.0 | Async image loading |
| **Material3** | Latest | Material Design 3 |

---

## 🏗️ Architecture

**MVVM (Model-View-ViewModel)** pattern with Clean Architecture principles:

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │
│   LoginScreen, HomeScreen, etc.     │
└──────────────┬──────────────────────┘
               │ observes State
               ▼
┌─────────────────────────────────────┐
│       ViewModel Layer               │
│   AuthViewModel, EventViewModel     │
│   (UI State + Business Logic)       │
└──────────────┬──────────────────────┘
               │ calls methods
               ▼
┌─────────────────────────────────────┐
│      Repository Layer               │
│  UserRepository, EventRepository    │
│   (Data Source Abstraction)         │
└──────────────┬──────────────────────┘
               │ manages
               ▼
┌──────────────────┬──────────────────┐
│   Room (Local)   │  Retrofit (API)  │
│   SQLite DB      │  Xano Backend    │
└──────────────────┴──────────────────┘
```

### Offline-First Strategy
1. **Read**: Always from local Room database
2. **Write**: Save to Room first → Sync to Xano in background
3. **Sync**: Automatic when internet available
4. **Conflict**: Server wins (last-write-wins)

---

## 📦 Installation

### Prerequisites
- **Android Studio** Ladybug 2024.2.1 or newer
- **JDK** 17 or higher
- **Android SDK** 36
- **Device/Emulator** with Android 7.0+ (API 25+)

### Setup Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/JorgeVergar4/OurArea.git
cd OurArea
```

#### 2. Configure API Keys
Create `apikeys.properties` in the project root:

```bash
cp apikeys.properties.example apikeys.properties
```

Edit and add your keys:
```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

**Get Google Maps API Key:**  
https://console.cloud.google.com/apis/credentials

⚠️ **Important:** `apikeys.properties` is in `.gitignore` - never commit it!

#### 3. Sync Gradle
Open the project in Android Studio and wait for automatic Gradle sync.

#### 4. Run the App
```bash
./gradlew installDebug
```

Or click ▶️ Run in Android Studio.

### Build APK for Production
```bash
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/release/app-release.apk`

---

## 🌐 API Integration (Xano)

**Status:** ✅ **FULLY INTEGRATED & PRODUCTION READY**

**Base URL:** `https://x8ki-letl-twmt.n7.xano.io/`

### API Endpoints

The app integrates with Xano through two API groups:

#### 🔐 Authentication API (`api:p4Kx6qbK`)
- `POST /auth/signup` - Register new user
- `POST /auth/login` - Login (returns JWT token)
- `GET /auth/me` - Get current user info

#### 📅 Events API (`api:8B8nOhtv`)
- `GET /event` - List all events (with filters)
- `GET /event/{id}` - Get event by ID
- `POST /event` - Create new event
- `PATCH /event/{id}` - Update event
- `DELETE /event/{id}` - Delete event
- `GET /event/user/{userId}` - Get user's events

### 📸 Image Storage - Important Limitation

⚠️ **The Xano free/basic plan does NOT support image uploads.**

**Current Implementation:**
- ✅ Images stored **locally** on device (`app/files/event_images/`)
- ✅ Events sync to Xano **without images**
- ❌ Images **NOT uploaded** to cloud
- ❌ Images **NOT synced** across devices

**To enable cloud image storage, you need:**
1. **Xano paid plan** with file storage, OR
2. **Third-party service** (AWS S3, Cloudinary, Firebase Storage)

### Xano Database Schema

**Table: `user`**
```sql
- id (int, auto-increment, PK)
- name (text)
- email (text, unique)
- password (text, hashed)
- role (text, default: "user")
- created_at (timestamp)
```

**Table: `event`**
```sql
- id (int, auto-increment, PK)
- title (text)
- description (text)
- latitude (float)
- longitude (float)
- time_info (text)
- is_free (boolean)
- is_family (boolean)
- is_music (boolean)
- is_food (boolean)
- is_art (boolean)
- is_sports (boolean)
- user_id (int, FK -> user.id)
- created_at (timestamp)
```

### Sync Strategy
1. **On App Start:** Fetch all events from Xano → Save to Room
2. **Create Event:** Save to Room → POST to Xano → Update Room with Xano ID
3. **Delete Event:** DELETE from Xano → DELETE from Room
4. **Update Event:** PATCH to Xano → Update Room

---

## 🔐 Security

### Password Encryption
- **Algorithm:** PBKDF2WithHmacSHA256
- **Iterations:** 10,000
- **Key Length:** 256 bits
- **Salt:** 16 random bytes per password
- **Format:** `salt:hash` (hexadecimal)

### Session Management
- **DataStore Preferences** with encryption
- **JWT tokens** from Xano
- **Automatic logout** on token expiry

### Best Practices
- ✅ Input validation & sanitization
- ✅ SQL injection prevention (Room with safe parameters)
- ✅ HTTPS-only API communication
- ✅ ProGuard/R8 in release builds
- ✅ API keys in `BuildConfig` (not hardcoded)
- ✅ Minimum permissions requested

---

## 📱 Permissions

| Permission | Usage | Required |
|-----------|-------|----------|
| `INTERNET` | API calls & map tiles | ✅ Yes |
| `ACCESS_FINE_LOCATION` | Precise user location | ✅ Yes |
| `ACCESS_COARSE_LOCATION` | Approximate location | ⚠️ Fallback |
| `CAMERA` | Take event photos | 🔵 Optional |
| `READ_MEDIA_IMAGES` (API 33+) | Gallery access | 🔵 Optional |
| `READ_EXTERNAL_STORAGE` (API <33) | Gallery access | 🔵 Optional |

---

## 📂 Project Structure

```
our/
├── app/
│   ├── src/main/java/cl/duoc/ourarea/
│   │   ├── MainActivity.kt
│   │   ├── api/
│   │   │   ├── RetrofitClient.kt
│   │   │   ├── XanoApiService.kt
│   │   │   ├── XanoAuthApiService.kt
│   │   │   └── models/
│   │   │       ├── ApiModels.kt
│   │   │       └── ModelMappers.kt
│   │   ├── data/
│   │   │   ├── UserPreferencesManager.kt
│   │   │   └── PasswordHasher.kt
│   │   ├── model/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── User.kt & UserDao.kt
│   │   │   └── Event.kt & EventDao.kt
│   │   ├── repository/
│   │   │   ├── UserRepository.kt
│   │   │   └── EventRepository.kt
│   │   ├── viewmodel/
│   │   │   ├── AuthViewModel.kt
│   │   │   └── EventViewModel.kt
│   │   └── ui/
│   │       ├── AppNavGraph.kt
│   │       ├── SplashScreen.kt
│   │       ├── LoginScreen.kt
│   │       ├── RegisterScreen.kt
│   │       ├── HomeScreen.kt
│   │       ├── AddEventScreen.kt
│   │       ├── EditEventScreen.kt
│   │       ├── EventDetailScreen.kt
│   │       └── theme/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── apikeys.properties.example
└── README.md
```

---

## 🎯 Roadmap

### Version 1.1.0 (Q1 2025)
- [ ] Dark mode implementation
- [ ] Push notifications (Firebase)
- [ ] Social media sharing
- [ ] Event QR codes

### Version 1.2.0 (Q2 2025)
- [ ] Cloud image storage (AWS S3 or Cloudinary)
- [ ] Event chat feature
- [ ] Calendar integration
- [ ] Export events to PDF

### Version 2.0.0 (Q3 2025)
- [ ] Real-time updates (WebSockets)
- [ ] Event ratings & reviews
- [ ] Premium events
- [ ] In-app payments

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'feat: add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Guidelines
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Jetpack Compose for UI
- Document public APIs
- Write unit tests for business logic
- Use [Conventional Commits](https://www.conventionalcommits.org/)

---

## 📄 License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

---

## 👥 Authors & Contact

**Developers:**  
- Jorge Vergara - [@JorgeVergar4](https://github.com/JorgeVergar4)  
- Fernando Villalón

**Project Link:** https://github.com/JorgeVergar4/OurArea  
**Email:** contact@ourarea.app

### Report Issues
Found a bug? [Create an issue](https://github.com/JorgeVergar4/OurArea/issues) with:
- Problem description
- Steps to reproduce
- Device & Android version
- Screenshots (if applicable)

---

<div align="center">

**Built with ❤️ using Jetpack Compose & Kotlin**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.10-blue?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Xano](https://img.shields.io/badge/Backend-Xano-orange)](https://xano.com)

[⬆ Back to top](#ourarea-)

</div>
