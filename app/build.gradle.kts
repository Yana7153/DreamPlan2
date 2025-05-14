plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.dreamplan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dreamplan"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Firebase (with BoM)
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Hilt (for Java)
    implementation("com.google.dagger:hilt-android:2.48")
    annotationProcessor("com.google.dagger:hilt-compiler:2.48")  // annotationProcessor for Java

    // UI
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")  // Fixed the quote typo
    implementation(libs.constraintlayout)

    // Architecture Components
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)  // For Java

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}