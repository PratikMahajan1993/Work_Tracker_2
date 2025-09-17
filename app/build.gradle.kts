import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services) // Added Google Services plugin
    // id("kotlin-kapt") // Removed for KSP
    id("com.google.dagger.hilt.android") // Added for Hilt
    id("com.google.devtools.ksp") // Added for KSP
}

// Read local.properties file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.example.worktracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.worktracker"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Expose DB_WIPE_PASSWORD from local.properties as a BuildConfig field
        buildConfigField("String", "DB_WIPE_PASSWORD", "\"${localProperties.getProperty("DB_WIPE_PASSWORD") ?: ""}\"")
        // Expose OPERATOR_INFO_PASSWORD from local.properties as a BuildConfig field
        buildConfigField("String", "OPERATOR_INFO_PASSWORD", "\"${localProperties.getProperty("OPERATOR_INFO_PASSWORD") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Updated to JVM 17
        targetCompatibility = JavaVersion.VERSION_17 // Updated to JVM 17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) // Updated to JVM 17
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true // Enable BuildConfig
    }
}

dependencies {

    implementation(platform(libs.firebase.bom)) // Firebase BOM
    implementation(libs.firebase.auth)      // Firebase Auth
    implementation(libs.firebase.common)    // Common Firebase utilities
    implementation(libs.firebase.firestore)
    // implementation(libs.firebase.firestore.ktx) // Commented out to use direct declaration
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0") // Added direct declaration with version

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.generativeai)

    implementation(libs.hilt.android) // com.google.dagger:hilt-android
    implementation(libs.androidx.hilt.navigation.compose) // androidx.hilt:hilt-navigation-compose
    ksp(libs.hilt.compiler) // com.google.dagger:hilt-compiler

    // Hilt WorkManager Integration
    implementation(libs.androidx.hilt.work) // androidx.hilt:hilt-work 
    ksp(libs.androidx.hilt.compiler.ksp)    // Corrected: androidx.hilt:hilt-compiler for KSP

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.navigation.compose)

    // Credential Manager dependencies
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth) // This is the correct one
    implementation(libs.googleid) // Added for GoogleIdTokenCredential

    // WorkManager Runtime
    implementation(libs.androidx.work.runtime.ktx) // Added WorkManager

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
