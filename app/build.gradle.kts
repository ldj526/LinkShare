import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.linkshare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.linkshare"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${localProperties["naver_client_id"]}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${localProperties["naver_client_secret"]}\"")

        manifestPlaceholders += mapOf("NAVER_MAP_CLIENT_ID" to localProperties.getProperty("naver_map_client_id"))
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
    viewBinding {
        enable = true
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    // Splash
    implementation("androidx.core:core-splashscreen:1.1.0-alpha02")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // Add firebase realtime database
    implementation("com.google.firebase:firebase-database-ktx")
    // Add firebase storage
    implementation("com.google.firebase:firebase-storage-ktx")
    // Add firebase firestore
    implementation("com.google.firebase:firebase-firestore")

    // Add the dependency for the Firebase Authentication library
    implementation("com.google.firebase:firebase-auth:22.3.0")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // Naver Map SDK
    implementation("com.naver.maps:map-sdk:3.17.0")
    // Naver Map FusedLocationSource
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")

    // FlexBoxLayout
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // viewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}