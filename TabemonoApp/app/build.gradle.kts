plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.aplikasichefai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aplikasichefai"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE.txt")
        }
    }
}

dependencies {
    // AndroidX & Material Components
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation(libs.appcompat)
    implementation(libs.material)

    // Networking
    implementation(libs.volley)
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.8.9")

    // Firebase Services
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.9.0")

    // JavaMail dependencies for email sending
    implementation("com.sun.mail:android-mail:1.6.5")
    implementation("com.sun.mail:android-activation:1.6.5")

    // Add these to avoid conflicts with Android SDK
    implementation("androidx.core:core:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
}