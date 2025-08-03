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
//        ndk {
//            abiFilters += listOf("arm64-v8a")
//        }
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

//    sourceSets {
//        getByName("main") {
//            jniLibs.srcDirs("src/main/jniLibs")
//        }
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


dependencies {
    implementation (libs.androidx.activity)
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.leanback.tab)
    implementation(libs.androidx.leanback.preference)
    implementation (libs.androidx.appcompat)

    //Glide
    implementation(libs.glide)
    implementation(libs.glide.transformations)
    implementation(libs.androidx.preference)
    annotationProcessor (libs.compiler)

    // https://mvnrepository.com/artifact/net.jpountz.lz4/lz4
    implementation(libs.lz4)

    // Room
    implementation (libs.androidx.room.runtime.v261)
    implementation(libs.androidx.fragment)
    annotationProcessor (libs.androidx.room.compiler.v261)

    //Lombok
    compileOnly (libs.lombok.v11838)
    annotationProcessor (libs.lombok.v11838)

    //Pdf
    implementation(libs.tom.roush.pdfbox.android)
    implementation(libs.mhiew.pdfium.android)



    //epublib
    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group = "org.slf4j")
        exclude(group = "xmlpull")
    }

    // https://mvnrepository.com/artifact/net.java.truevfs/truevfs-access
    implementation("net.java.truevfs:truevfs-profile-default:0.14.0") {
        exclude(group = "com.google.code.findbugs", module = "annotations")
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }
    implementation("net.java.truevfs:truevfs-driver-zip-raes:0.14.0") {
        exclude(group = "com.google.code.findbugs", module = "annotations")
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }

//    //RAR
//    implementation(libs.junrar)


//    dependencies {
//        implementation(files("libs/pdfbox-app-3.0.5.jar"))
//    }

    implementation (libs.slf4j.android)

    //debugImplementation (libs.leakcanary.android)
}