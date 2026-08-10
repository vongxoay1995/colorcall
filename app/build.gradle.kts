plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)

}
android {
    namespace = "com.colorcall.callerscreen"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.colorcall.callerscreen"
        minSdk = 24
        targetSdk = 36
        versionCode = 82
        versionName = "1.7.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders["enableCrashReporting"] = "false"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders["enableCrashReporting"] = "true"

        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = false
        disable.add("NonConstantResourceId")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation(project(":commons"))
    implementation(project(":hawk"))
    implementation(libs.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation("junit:junit:4.13.2")
    implementation ("com.google.firebase:firebase-messaging:24.1.0")
    implementation("com.intuit.ssp:ssp-android:1.0.6")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.airbnb.android:lottie:6.4.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    // multidex not needed with minSdk=24
    implementation("com.makeramen:roundedimageview:2.3.0")
    // Matisse 0.5.2 is abandoned & crashes on Android 10+
    // TODO: Replace with PhotoPicker API or another maintained library
    implementation("com.zhihu.android:matisse:0.5.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.ybq:Android-SpinKit:1.4.0")
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.50")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    // ExoPlayer 2.x is deprecated — migrate to Media3 when ready
    // TODO: Replace with androidx.media3:media3-exoplayer
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-analytics:22.4.0")
    implementation("com.google.firebase:firebase-config:22.0.1")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-crashlytics:19.4.1")
    implementation("com.google.android.gms:play-services-ads:25.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.indicator.fast.scroll)
    implementation(libs.autofit.text.view)
    implementation("com.tbuonomo:dotsindicator:5.1.0")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

   // api(libs.koin.core)
    //api(libs.koin.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.billing)
}

