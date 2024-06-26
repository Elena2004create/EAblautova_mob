// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
    kotlin("kapt") version "1.9.23"
}
buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.google.com/")
        }
    }
    dependencies {
        val nav_version = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath("org.powermock:powermock-module-junit4:2.0.9")
        classpath("org.powermock:powermock-api-mockito2:2.0.9")
    }
}