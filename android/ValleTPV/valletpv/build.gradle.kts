plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}



android {
    namespace = "com.valleapp.valletpv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.valleapp.valletpv"
        minSdk = 26
        targetSdk = 33
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

    buildFeatures {
        compose = true
    }

}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.ui:ui-android:1.6.0-alpha02")
    implementation("androidx.compose.material:material:1.6.0-alpha02")
    implementation("androidx.compose.ui:ui-tooling:1.6.0-alpha02")
    implementation (project(":ValleTPVLib"))
    implementation("androidx.activity:activity-compose:1.4.0-alpha01")
    implementation("androidx.compose.runtime:runtime:1.6.0-alpha02")


}