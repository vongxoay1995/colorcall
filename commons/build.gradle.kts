import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.parcelize)
}

android {
    compileSdk = libs.versions.app.build.compileSDKVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.app.build.minimumSDK.get().toInt()
        vectorDrawables.useSupportLibrary = true
        consumerProguardFiles("consumer-rules.pro")
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }


    compileOptions {
        val currentJavaVersionFromLibs = JavaVersion.valueOf(libs.versions.app.build.javaVersion.get().toString())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
    namespace = libs.versions.app.version.groupId.get()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xcontext-receivers"
        )
    }
}


dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(libs.kotlin.immutable.collections)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.ez.vcard)


    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.compose)
    implementation(libs.compose.view.binding)
    debugImplementation(libs.bundles.compose.preview)

    api(libs.joda.time)
    api(libs.recyclerView.fastScroller)
    api(libs.reprint)
    api(libs.patternLockView)
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.gson)

    api(libs.glide)

    api(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
}
