import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Required for Hilt
    id("com.google.dagger.hilt.android") // ✅ correct
}

android {
    namespace = "com.wingspan.locationtracking"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wingspan.locationtracking"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    signingConfigs {
        create("release") {
            val keystorePath = project.findProperty("KEYSTORE_FILE") as? String ?: "location.jks"
            val storePassword = project.findProperty("KEYSTORE_PASSWORD") as? String ?: ""
            val keyAlias = project.findProperty("KEY_ALIAS") as? String ?: ""
            val keyPassword = project.findProperty("KEY_PASSWORD") as? String ?: ""
            storeFile = file(keystorePath)
            this.storePassword = storePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }
    buildTypes {
        release {
            signingConfig= signingConfigs.getByName("release")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation (libs.android.maps.utils)
    implementation (libs.osmdroid.android)
    //google map sdk
    implementation(libs.maps.compose) // check latest version
    implementation (libs.play.services.maps)
    // Accompanist Permissions for Jetpack Compose
    implementation(libs.accompanist.permissions)
    implementation(libs.gson)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Kotlin extensions for coroutines

    //gps location
    implementation(libs.play.services.location)

// Annotation processor
    kapt(libs.androidx.room.compiler)
    implementation(libs.hilt.android)

    // Hilt compiler
    kapt(libs.hilt.compiler)

    // Optional: Hilt + Jetpack Compose Navigation
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}