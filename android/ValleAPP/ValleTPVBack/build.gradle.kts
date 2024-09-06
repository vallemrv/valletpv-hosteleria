plugins {
    id("com.android.application")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.valleapp.valletpv"
        minSdk = 21
        targetSdk = 33
        versionCode = 4
        versionName = "3.0.2.1"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        useLibrary("org.apache.http.legacy")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "/META-INF/androidx.localbroadcastmanager_localbroadcastmanager.version"
            )
        }
    }

    namespace = "com.valleapp.valletpv"
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation("com.getbase:floatingactionbutton:1.10.1")
    implementation("com.android.support.constraint:constraint-layout:2.0.4")
    implementation("android.arch.navigation:navigation-fragment:1.0.0")
    implementation("android.arch.navigation:navigation-ui:1.0.0")
    implementation("org.slf4j:slf4j-simple:1.6.1")
    implementation(project(":ValleTPVLib"))
}
