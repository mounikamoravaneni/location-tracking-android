// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.24" apply false
}

buildscript {

    dependencies {
        // Only include Hilt Gradle plugin here if you want old style
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.47")
        // Kotlin Gradle plugin is already handled by alias(libs.plugins.kotlin.android)
    }
}