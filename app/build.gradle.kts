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

    // Room
    implementation (libs.androidx.room.runtime.v261)
    annotationProcessor (libs.androidx.room.compiler.v261)

    //Lombok
    compileOnly (libs.lombok.v11838)
    annotationProcessor (libs.lombok.v11838)

}