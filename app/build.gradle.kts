import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true // Enable BuildConfig
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended) // Added extended material icons
    implementation(libs.generativeai) // Added Gemini API

    implementation(libs.hilt.android) // Updated Hilt version
    implementation(libs.androidx.hilt.navigation.compose) // Added Hilt Navigation Compose
    //kapt("com.google.dagger:hilt-android-compiler:2.57.1") // Removed for KSP
    ksp(libs.hilt.compiler) // Switched to KSP for Hilt

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
