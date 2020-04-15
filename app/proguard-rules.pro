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
-keep class android.support.v13.** { *; }
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
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class com.android.vending.licensing.ILicensingService

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keep class com.apperhand.** {
*;}


# --------------------------------------------------------------------
# REMOVE all Log messages except warnings and errors
# --------------------------------------------------------------------
#-assumenosideeffects class android.util.Log {
#    public static *** d(...);
#    public static *** v(...);
#    public static *** i(...);
#    public static *** w(...);
#}
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
-keep class com.colorcall.callerscreen.** { *; }
-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }
-keep class com.crashlytics.** { *; }
### greenDAO 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
 }
 -keep class **$Properties

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

### greenDAO 2
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
-dontwarn com.crashlytics.**
-keep public class * extends androidx.core.app.ActivityCompat
-keep class com.google.android.material.** { *; }

-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

-dontwarn androidx.**
-dontwarn rx.**
-dontwarn javax.**
-dontwarn org.**
-dontwarn java.rmi.**
-dontwarn com.sun.**
-dontwarn net.sqlcipher.**

-keep class androidx.** { *; }
-keep class rx.** { *; }
-keep class javax.** { *; }
-keep class org.** { *; }
-keep class java.rmi** { *; }
-keep class net.sqlcipher.** {
    *;
}
-keep class com.sun.** {
    *;
}
-keep interface androidx.** { *; }

-dontnote androidx.renderscript.**
-dontwarn androidx.renderscript.**
-dontwarn androidx.core.app.ActivityCompat
# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
-keep class com.android.internal.telephony.ITelephony { *; }


-keep public class * extends android.support.design.widget.CoordinatorLayout$Behavior {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keep public class com.google.android.gms.ads.**{ *;}
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keep public class com.google.ads.**{ *;}
-keep class com.orhanobut.hawk.** { *; }
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-dontwarn com.google.ads.**
-dontwarn com.google.android.gms.**