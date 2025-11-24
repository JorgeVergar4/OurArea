# OurArea ProGuard Rules
# Optimización para producción

# Mantener información de línea para debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# === Jetpack Compose ===
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# === Room Database ===
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Mantener las clases de entidades de Room
-keep class cl.duoc.ourarea.model.** { *; }

# === Kotlin Coroutines ===
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# === Google Maps ===
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**

# === Google Play Services ===
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.location.** { *; }

# === Coil (Image Loading) ===
-keep class coil.** { *; }
-keep interface coil.** { *; }

# === DataStore ===
-keep class androidx.datastore.*.** { *; }

# === Clases del proyecto ===
-keep class cl.duoc.ourarea.** { *; }
-keepclassmembers class cl.duoc.ourarea.** {
    public <init>(...);
}

# === Prevenir warnings ===
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# === Optimizaciones ===
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# === Mantener enums ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === Serialización ===
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
