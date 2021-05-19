# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\MinhVu\AppData\Local\Android\sdk1/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontskipnonpubliclibraryclassmembers
-dontwarn **CompatHoneycomb
-ignorewarnings

#-libraryjars /libs/StartAppInApp-2.4.11.jar

#-dontwarn com.slidingmenu.*

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable,!class/unboxing/enum

#class like a, b, c...
-repackageclasses ''
-allowaccessmodification

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keep class com.apperhand.** {
*;}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
#-keep class com.colorcall.callerscreen.** { *; }
-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }
#keep model database
-keep class com.colorcall.callerscreen.database.* { *; }
### greenDAO 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
 }
 -keep class **$Properties

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

-keep class **$Properties
-keep public class * extends androidx.core.app.ActivityCompat
-dontwarn net.sqlcipher.**
-keep class net.sqlcipher.** {
    *;
}

-dontwarn androidx.core.app.ActivityCompat
-keep class com.android.internal.telephony.ITelephony { *; }

-keep public class com.google.android.gms.ads.**{ *;}
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keep public class com.google.ads.**{ *;}
-keep class com.orhanobut.hawk.** { *; }
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
-dontwarn com.google.ads.**
-dontwarn com.google.android.gms.**

# retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
# For okhttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepattributes Signature
# Retain service method parameters.
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
#-keep class com.response.** {*;}
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}