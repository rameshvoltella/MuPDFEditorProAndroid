plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    kotlin("kapt")


}

android {
    namespace = "com.rameshvoltella.pdfeditorpro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rameshvoltella.pdfeditorpro"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters.addAll(arrayOf("armeabi", "armeabi-v7a", "x86", "mips", "arm64-v8a"))
        }
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.h6ah4i.android.widget.verticalseekbar:verticalseekbar:1.0.0")

    implementation(project(":mupdf"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // Room components
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    kapt (libs.androidx.room.compiler)
//    implementation(libs.androidx.hilt.common)
//    implementation(libs.androidx.hilt.work)
//    implementation (libs.androidx.hilt.navigation.compose)
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.github.hearsilent:DiscreteSlider:1.2.1")
//    implementation ("androidx.media3:media3-exoplayer:1.4.3")
//    implementation ("androidx.media3:media3-ui:1.4.3")
    implementation ("org.jsoup:jsoup:1.14.3")

    implementation("androidx.media3:media3-exoplayer:1.0.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.0.0")
    implementation("androidx.media3:media3-ui:1.X.X")
    implementation ("io.github.chochanaresh:filepicker:0.1.9")
}
kapt {
    correctErrorTypes = true
}