plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    kotlin("kapt") version "1.9.23"
}

android {
    namespace = "com.example.guide"
    compileSdk = 34
    
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.guide"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    debugImplementation("androidx.fragment:fragment-testing:1.7.1") // Use the latest version
    debugImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    debugImplementation("androidx.test:rules:1.5.0")
    debugImplementation("androidx.test:runner:1.5.2")
    debugImplementation("androidx.lifecycle:lifecycle-runtime-testing:2.8.0")
    debugImplementation("androidx.arch.core:core-testing:2.1.0")

    testImplementation("org.mockito:mockito-core:3.12.4")
    androidTestImplementation("org.mockito:mockito-android:3.12.4")

    androidTestImplementation("org.powermock:powermock-module-junit4:2.0.9")
    //testImplementation("org.powermock:powermock-module-junit4-rule:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
    testImplementation("org.powermock:powermock-classloading-xstream:1.6.6")


    implementation("androidx.test.espresso:espresso-contrib:3.5.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.yandex.android:maps.mobile:4.6.1-full")
    val room_version = "2.6.1"
    val nav_version = "2.7.7"

    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")

    ksp("androidx.room:room-compiler:$room_version")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")
    // To use Kotlin Symbol Processing (KSP)

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}
