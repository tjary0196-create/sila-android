plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

import java.util.Properties

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}
val webClientId: String = localProps.getProperty("FIREBASE_WEB_CLIENT_ID", "")
// NOTE: IMGBB_API_KEY is intentionally NOT read here anymore. It now lives only as a
// Cloud Functions secret (see /functions) and is never embedded in the client binary.
// See functions/src/index.ts's `uploadProfilePhoto` callable.

val composeVersion = "1.5.0"
val iconsVersion = "1.6.7"
val material3Version = "1.1.0"
val kotlinVersion = "1.9.20"
val firebaseBom = "31.5.0"

android {
    namespace = "com.sila.messaging"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sila.messaging"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
    }

    buildTypes {
        release {
            // Code shrinking/obfuscation enabled for release — this is a real hardening
            // measure against reverse engineering (class/method names are stripped, dead
            // code is removed). Paired with proguard-rules.pro's Firebase/Coil keep rules
            // so reflection-based SDK internals still work post-shrink.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            // Debug builds talk to Firebase App Check's debug provider (see SilaApp.kt),
            // which requires no extra config here — App Check handles the environment
            // switch automatically based on build type via the SDK we initialize.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.4" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:$material3Version")
    implementation("androidx.compose.material:material-icons-core:$iconsVersion")
    implementation("androidx.compose.material:material-icons-extended:$iconsVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation(platform("com.google.firebase:firebase-bom:$firebaseBom"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-functions-ktx") // calls the uploadProfilePhoto Cloud Function

    // App Check — attests that Firestore/Functions calls are coming from this app's real,
    // unmodified binary running on a genuine device, not a script, emulator farm, or a
    // patched/rebuilt APK replaying captured requests. This is a meaningful anti-abuse
    // layer independent of Firestore Rules (Rules check WHO you are; App Check checks
    // WHAT is calling).
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    debugImplementation("com.google.firebase:firebase-appcheck-debug")

    implementation("com.google.android.gms:play-services-auth:21.4.0")

    implementation("io.coil-kt:coil-compose:2.5.0")
    // okhttp3 / gson removed: no longer used now that image upload goes through the
    // uploadProfilePhoto Cloud Function instead of a direct client->imgbb HTTP call.
}
