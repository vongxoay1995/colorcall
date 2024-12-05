//plugins {
//    id 'com.android.application' version '8.1.2' apply false
//    id 'com.google.gms.google-services' version '4.3.4' apply false
//    id 'org.jetbrains.kotlin.android' version '1.7.10' apply false
//   // id 'org.greenrobot.greendao'
//    id 'com.google.firebase.crashlytics' version '2.9.2' apply false
//}
plugins {
    alias(libs.plugins.android).apply(false)
    alias(libs.plugins.googleServices).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.firebaseCrashlytics).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.parcelize).apply(false)
    alias(libs.plugins.library).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
}