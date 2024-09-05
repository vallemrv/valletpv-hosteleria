plugins {
    id("com.android.application")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.valleapp.vallecom"
        minSdk = 15
        versionCode = 5
        versionName = "3.3.3"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    useLibrary("org.apache.http.legacy")

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    namespace = "com.valleapp.vallecom"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.floatingactionbutton)
    implementation(libs.constraint.layout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.java.websocket)
    implementation(libs.slf4j.simple)
}
