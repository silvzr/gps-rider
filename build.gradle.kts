// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// تمت إزالة قسم allprojects.repositories لتجنب التعارض مع dependencyResolutionManagement في settings.gradle.kts