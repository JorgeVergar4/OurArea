# OurArea

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Min SDK](https://img.shields.io/badge/minSdk-25-orange.svg)
![Target SDK](https://img.shields.io/badge/targetSdk-36-orange.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

### Discover events near you in real-time

[Features](#features) • [Tech Stack](#tech-stack) • [Installation](#installation) • [API Integration](#api-integration-with-xano) • [Architecture](#architecture)

</div>

---

## Overview

**OurArea** is a native Android mobile application that enables users to discover, create, and share geolocated events in real-time. The app combines interactive maps with a local database to deliver a smooth, offline-first experience for exploring nearby events.

The application is designed with **Xano API REST integration** in mind, allowing seamless backend connectivity for cloud synchronization, real-time updates, and multi-device support.

---

## Features

### Core Functionality

- **Secure Authentication**
  - Login and registration system with PBKDF2 password encryption
  - Automatic session persistence using DataStore Preferences
  - Email validation and duplicate prevention

- **Interactive Maps**
  - Google Maps integration for event visualization
  - Real-time geolocation with distance calculation
  - Draggable markers and location picker
  - Center on user location

- **Event Management**
  - Create events with images (camera or gallery)
  - Category-based filtering (Music, Food, Art, Sports, etc.)
  - Smart search functionality
  - Event details with navigation to Google Maps

- **Modern UI/UX**
  - Material Design 3 implementation
  - Responsive design (portrait and landscape)
  - Smooth animations and transitions
  - Custom permission dialogs
  - Enhanced logout confirmation modal

- **Offline-First Architecture**
  - Local SQLite database with Room
  - No internet required for basic functionality
  - Ready for cloud sync with Xano API

---

## Tech Stack

### Primary Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.0.21 | Programming language |
| **Jetpack Compose** | 2024.10.00 | Declarative UI framework |
| **Room Database** | 2.6.1 | Local data persistence |
| **Coroutines & Flow** | Latest | Asynchronous programming |
| **ViewModel** | 2.8.7 | MVVM architecture |
| **Navigation Compose** | 2.8.3 | Screen navigation |
| **Google Maps Compose** | 6.2.1 | Interactive maps |
| **DataStore Preferences** | 1.1.1 | Preference storage |
| **Coil** | 2.7.0 | Image loading |

### Additional Dependencies

- **Google Play Services** (Maps, Location): 19.0.0 / 21.3.0
- **Accompanist Permissions**: 0.34.0
- **Security Crypto**: 1.1.0-alpha06
- **Material Icons Extended**: Latest
- **Retrofit** (for Xano integration): Ready to implement

---

## Architecture

OurArea follows the **MVVM (Model-View-ViewModel)** architecture pattern recommended by Google:

```
┌─────────────────────────────────────┐
│            UI Layer                 │
│     (Composables Screens)           │
│  LoginScreen, HomeScreen, etc.      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│        ViewModel Layer              │
│  AuthViewModel, EventViewModel      │
│     (Business Logic & State)        │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│       Repository Layer              │
│  UserRepository, EventRepository    │
│     (Data Source Abstraction)       │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│         Data Layer                  │
│  Room DAO (Local) + Xano API (Cloud)│
│     (Data Persistence)              │
└─────────────────────────────────────┘
```

This architecture makes it easy to add Xano API integration without modifying existing code.

---

## Database Schema

### Local SQLite (Room)

**Database Name**: `ourarea_database`
**Version**: 3

#### Table: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique user ID |
| name | TEXT | NOT NULL | Full name |
| email | TEXT | NOT NULL, UNIQUE | Email (login credential) |
| password | TEXT | NOT NULL | Hashed password (PBKDF2) |

**Indexes**: UNIQUE index on `email`

#### Table: `events`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique event ID |
| title | TEXT | NOT NULL | Event title |
| description | TEXT | NOT NULL | Detailed description |
| latitude | REAL | NOT NULL | Latitude coordinate |
| longitude | REAL | NOT NULL | Longitude coordinate |
| image | TEXT | NOT NULL | Local image path |
| timeInfo | TEXT | NOT NULL | Time info (Today, This weekend) |
| isFree | INTEGER | NOT NULL | Is free event (0/1) |
| isFamily | INTEGER | NOT NULL | Family category (0/1) |
| isMusic | INTEGER | NOT NULL | Music category (0/1) |
| isFood | INTEGER | NOT NULL | Food category (0/1) |
| isArt | INTEGER | NOT NULL | Art category (0/1) |
| isSports | INTEGER | NOT NULL | Sports category (0/1) |
| distance | REAL | DEFAULT 0 | Calculated distance in meters |
| createdByUserId | INTEGER | DEFAULT 0 | Creator user ID |
| createdAt | INTEGER | DEFAULT current_timestamp | Creation timestamp |

---

## API Integration with Xano

### Why Xano?

[Xano](https://xano.com) is a powerful no-code backend platform that provides:
- RESTful API generation
- Database management
- Authentication & authorization
- File storage
- Real-time features
- Scalable infrastructure

### Xano Setup Guide

#### 1. Create Xano Database Tables

Create the following tables in your Xano workspace:

**Table: `users`**
```
- id (int, auto-increment, primary key)
- name (text)
- email (text, unique)
- password (text)  // Will store hashed passwords
- created_at (timestamp)
```

**Table: `events`**
```
- id (int, auto-increment, primary key)
- title (text)
- description (text)
- latitude (float)
- longitude (float)
- image_url (text)  // Xano file storage URL
- time_info (text)
- is_free (boolean)
- is_family (boolean)
- is_music (boolean)
- is_food (boolean)
- is_art (boolean)
- is_sports (boolean)
- created_by_user_id (int, foreign key -> users.id)
- created_at (timestamp)
```

#### 2. Create Xano API Endpoints

Create the following endpoints in Xano:

**Authentication:**
- `POST /auth/signup` - Register new user
- `POST /auth/login` - Login user (returns JWT token)
- `GET /auth/me` - Get current user info

**Events:**
- `GET /events` - Get all events (with pagination and filters)
- `GET /events/{id}` - Get event by ID
- `POST /events` - Create new event
- `PATCH /events/{id}` - Update event
- `DELETE /events/{id}` - Delete event

**File Upload:**
- `POST /upload` - Upload event image

#### 3. Configure Android App for Xano

Add Retrofit dependencies to `app/build.gradle.kts`:

```kotlin
dependencies {
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines adapter
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
}
```

#### 4. Create Xano API Service

Create `app/src/main/java/cl/duoc/ourarea/api/XanoApiService.kt`:

```kotlin
interface XanoApiService {
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<User>

    @GET("events")
    suspend fun getEvents(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("radius") radius: Int
    ): Response<List<Event>>

    @POST("events")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Body event: Event
    ): Response<Event>

    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>
}
```

#### 5. Update Repository Pattern

Modify repositories to support both local (Room) and remote (Xano) data sources:

```kotlin
class EventRepository(
    private val localDataSource: EventDao,
    private val remoteDataSource: XanoApiService
) {
    suspend fun syncEvents() {
        // Fetch from Xano
        val remoteEvents = remoteDataSource.getEvents(lat, lng, radius)

        // Update local database
        localDataSource.insertEvents(remoteEvents.body()!!)
    }

    suspend fun createEvent(event: Event) {
        // Save to local DB first (offline-first)
        localDataSource.insertEvent(event)

        // Try to sync with Xano
        try {
            remoteDataSource.createEvent(authToken, event)
        } catch (e: Exception) {
            // Mark for later sync
            // Implement sync queue
        }
    }
}
```

#### 6. Environment Configuration

Create `apikeys.properties` file:

```properties
MAPS_API_KEY=your_google_maps_api_key
XANO_BASE_URL=https://your-workspace.xano.io/api:1
XANO_API_KEY=your_xano_api_key  # If using API key auth
```

Update `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        val properties = Properties()
        properties.load(FileInputStream(rootProject.file("apikeys.properties")))

        buildConfigField("String", "XANO_BASE_URL",
            "\"${properties.getProperty("XANO_BASE_URL")}\"")
        buildConfigField("String", "XANO_API_KEY",
            "\"${properties.getProperty("XANO_API_KEY")}\"")
    }
}
```

### Sync Strategy

The app implements an **offline-first** strategy:

1. **Read**: Always read from local Room database
2. **Write**: Save to Room first, then sync to Xano in background
3. **Sync**: Periodic background sync when internet is available
4. **Conflict Resolution**: Server wins (last-write-wins strategy)

### Benefits of Xano Integration

- **Multi-device sync**: Access events from any device
- **Real-time updates**: See new events instantly
- **Cloud storage**: Images stored in Xano's file system
- **Scalability**: Handle thousands of users
- **Analytics**: Track app usage with Xano's built-in analytics
- **Admin panel**: Manage content through Xano's dashboard

---

## Installation

### Prerequisites

- Android Studio Ladybug | 2024.2.1 or newer
- JDK 17
- Android SDK 36
- Device/Emulator with Android 7.0 (API 25) or higher

### Setup Steps

1. **Clone the repository**

```bash
git clone https://github.com/JorgeVergar4/OurArea.git
cd OurArea
```

2. **Configure Google Maps API Key**

Create `apikeys.properties` file in the project root:

```bash
cp apikeys.properties.example apikeys.properties
```

Edit `apikeys.properties` and add your API keys:

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
XANO_BASE_URL=https://your-workspace.xano.io/api:1
```

Get Google Maps API Key: https://console.cloud.google.com/apis/credentials

**IMPORTANT**: The `apikeys.properties` file is NOT uploaded to GitHub (it's in .gitignore)

3. **Sync Gradle**

Open the project in Android Studio and wait for automatic sync.

4. **Run the application**

```bash
./gradlew installDebug
```

Or use the ▶️ Run button in Android Studio.

### Production Build

1. **Update signing config** in `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("path/to/keystore.jks")
        storePassword = "your_store_password"
        keyAlias = "your_key_alias"
        keyPassword = "your_key_password"
    }
}
```

2. **Build release APK**:

```bash
./gradlew assembleRelease
```

The APK will be generated in: `app/build/outputs/apk/release/`

---

## Security

### Password Encryption

OurArea implements robust password encryption:

- **Algorithm**: PBKDF2WithHmacSHA256
- **Iterations**: 10,000
- **Key Length**: 256 bits
- **Salt**: 16 random bytes per password
- **Storage Format**: `salt:hash` (hexadecimal)

### Session Persistence

- Encrypted **DataStore Preferences**
- Secure **session tokens**
- **Automatic cleanup** on logout

### Best Practices

- ✅ Real-time input validation
- ✅ SQL Injection prevention (Room with safe parameters)
- ✅ Minimum required permissions
- ✅ ProGuard/R8 enabled in release builds
- ✅ API Keys not exposed in code (using BuildConfig)
- ✅ HTTPS only for API communication

---

## Permissions

| Permission | Usage |
|-----------|-------|
| `ACCESS_FINE_LOCATION` | Precise user location |
| `ACCESS_COARSE_LOCATION` | Approximate location (fallback) |
| `INTERNET` | Load maps and API communication |
| `CAMERA` | Take photos for events |
| `READ_EXTERNAL_STORAGE` | Select images from gallery (API < 33) |
| `READ_MEDIA_IMAGES` | Select images from gallery (API >= 33) |

---

## Project Structure

```
our/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/cl/duoc/ourarea/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── UserPreferencesManager.kt
│   │   │   │   │   └── PasswordHasher.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── Event.kt
│   │   │   │   │   ├── UserDao.kt
│   │   │   │   │   └── EventDao.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.kt
│   │   │   │   │   └── EventRepository.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── AuthViewModel.kt
│   │   │   │   │   └── EventViewModel.kt
│   │   │   │   └── ui/
│   │   │   │       ├── AppNavGraph.kt
│   │   │   │       ├── LoginScreen.kt
│   │   │   │       ├── RegisterScreen.kt
│   │   │   │       ├── HomeScreen.kt
│   │   │   │       ├── AddEventScreen.kt
│   │   │   │       ├── EventDetailScreen.kt
│   │   │   │       └── theme/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## Roadmap

### Version 1.1.0
- [ ] Xano API integration
- [ ] Cloud sync for events
- [ ] Social media sharing
- [ ] Push notifications
- [ ] Dark mode

### Version 1.2.0
- [ ] Event chat
- [ ] Attendance confirmation
- [ ] Calendar integration
- [ ] Export events to PDF

### Version 2.0.0
- [ ] Real-time updates with WebSockets
- [ ] Event ratings and reviews
- [ ] Premium events
- [ ] In-app payments

---

## Contributing

Contributions are welcome! Please:

1. Fork the project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: add some amazing feature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style Guidelines

- Follow Kotlin conventions
- Use Jetpack Compose for UI
- Document public functions
- Write unit tests for business logic
- Follow conventional commits format

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

---

## Contact & Support

**Developers**: OurArea Team - Jorge Vergara & Fernando Villalón
**GitHub**: https://github.com/JorgeVergar4/OurArea
**Email**: contact@ourarea.app

### Report Bugs

To report bugs, please create an issue on GitHub with:
- Problem description
- Steps to reproduce
- Android version
- Screenshots (if applicable)

---

<div align="center">

**Built with ❤️ using Jetpack Compose**

[![GitHub](https://img.shields.io/badge/GitHub-OurArea-181717?logo=github)](https://github.com/JorgeVergar4/OurArea)

[⬆ Back to top](#ourarea)

</div>
