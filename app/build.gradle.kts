plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.example.bookreader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bookreader"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
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
}


dependencies {
    implementation (libs.androidx.activity)
    implementation(libs.androidx.leanback)
    implementation(libs.glide)
    implementation (libs.androidx.appcompat)

    // Room
    implementation (libs.androidx.room.runtime.v261)
    annotationProcessor (libs.androidx.room.compiler.v261)

    //Lombok
    compileOnly (libs.lombok.v11838)
    annotationProcessor (libs.lombok.v11838)

    //PdfBox
    implementation(libs.tom.roush.pdfbox.android)

    //epublib
    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group = "org.slf4j")
        exclude(group = "xmlpull")
    }
    implementation (libs.slf4j.android)


    debugImplementation (libs.leakcanary.android)

}