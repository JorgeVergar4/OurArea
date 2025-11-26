import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

android {
    namespace = "cl.duoc.ourarea"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.duoc.ourarea"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Leer API Keys desde archivo apikeys.properties
        val apiKeysFile = rootProject.file("apikeys.properties")
        val apiKeys = Properties()
        if (apiKeysFile.exists()) {
            apiKeys.load(FileInputStream(apiKeysFile))
            buildConfigField("String", "MAPS_API_KEY", "\"${apiKeys["MAPS_API_KEY"]}\"")
            manifestPlaceholders["MAPS_API_KEY"] = apiKeys["MAPS_API_KEY"] ?: ""

            // Xano API Configuration
            buildConfigField("String", "XANO_BASE_URL", "\"${apiKeys.getProperty("XANO_BASE_URL", "")}\"")
            buildConfigField("String", "XANO_API_KEY", "\"${apiKeys.getProperty("XANO_API_KEY", "")}\"")
        } else {
            // Fallback si no existe el archivo
            buildConfigField("String", "MAPS_API_KEY", "\"\"")
            manifestPlaceholders["MAPS_API_KEY"] = ""
            buildConfigField("String", "XANO_BASE_URL", "\"\"")
            buildConfigField("String", "XANO_API_KEY", "\"\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // Cambiar en producción real
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Jetpack Compose BOM - actualizado
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Room - actualizado con KSP
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Google Maps - VERSIONES ACTUALIZADAS
    implementation("com.google.maps.android:maps-compose:6.2.1")
    implementation("com.google.maps.android:maps-compose-utils:6.2.1")
    implementation("com.google.maps.android:maps-compose-widgets:6.2.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Coil para imágenes
    implementation("io.coil-kt:coil-compose:2.7.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Security (para cifrado de contraseñas)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Activity Result API
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Core AndroidX
    implementation("androidx.core:core-ktx:1.15.0")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit for Xano API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}