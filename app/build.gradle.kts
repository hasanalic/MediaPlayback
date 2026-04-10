plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinAndroidKsp)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.mediaplayback"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mediaplayback"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Core dependencies
    implementation (libs.androidx.core.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.startup.runtime)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Compose dependencies
    implementation (libs.androidx.activity.compose)
    implementation (platform(libs.androidx.compose.bom))
    implementation (libs.androidx.ui)
    implementation (libs.androidx.ui.graphics)
    implementation (libs.androidx.ui.tooling.preview)
    implementation (libs.androidx.material3)
    implementation (libs.androidx.compose.foundation.layout)
    debugImplementation (libs.androidx.ui.tooling)
    debugImplementation (libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Navigation & Serialization
    implementation(libs.navigation.compose)
    implementation(libs.bundles.serialization)

    // Coil
    implementation (libs.bundles.coil)

    // ExoPlayer
    implementation (libs.bundles.media3)

    // Material
    implementation (libs.bundles.material)
}