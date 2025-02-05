plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-kapt")
    alias(libs.plugins.kotlinSerialization)

}
android {
    namespace = "com.colorcall.callerscreen"
    compileSdk = 34
    buildToolsVersion = "33.0.1"

    defaultConfig {
        applicationId = "com.colorcall.callerscreen"
        minSdk = 23
        targetSdk = 34
        versionCode = 57
        versionName = "1.5.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable.add("NonConstantResourceId")
    }

    configurations.all {
        resolutionStrategy.force("com.google.android.gms:play-services-ads:21.3.0")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation(project(":commons"))
    implementation(libs.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation("junit:junit:4.13.2")
    implementation("com.intuit.ssp:ssp-android:1.0.6")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.airbnb.android:lottie:3.6.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.android.support:multidex:1.0.3")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.zhihu.android:matisse:0.5.2")
    implementation("com.orhanobut:hawk:2.0.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.ybq:Android-SpinKit:1.4.0")
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.8")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.9")
    implementation("com.google.android.exoplayer:exoplayer:2.15.1")
    implementation("com.squareup.okhttp3:okhttp:3.12.12")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.12")
    implementation("com.google.android.ump:user-messaging-platform:2.1.0")
    implementation("com.google.android.material:material:1.5.0-alpha01")
    implementation("com.google.firebase:firebase-analytics:21.3.0")
    implementation("com.google.firebase:firebase-config:21.4.1")
    implementation("com.google.firebase:firebase-core:21.1.1")  
    implementation("com.google.firebase:firebase-crashlytics:18.4.3")
    implementation("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:3.0.0-RC2")
    implementation("com.google.android.gms:play-services-ads:22.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.room:room-runtime:2.6.0")
    implementation(libs.indicator.fast.scroll)
    implementation(libs.autofit.text.view)

    kapt("androidx.room:room-compiler:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    implementation(libs.kotlinx.serialization.json)

}

