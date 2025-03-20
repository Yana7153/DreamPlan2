plugins {
    alias(libs.plugins.android.application)
 //   id("kotlin-kapt") // Add this for Java projects
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.runtime)
  //  kapt(libs.room.compiler) // Use kapt for Java projects
  //  implementation(libs.room.ktx) // Optional: For Room's Kotlin extensions
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}