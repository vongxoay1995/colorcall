# Preserve actionable stack traces for minified builds. Release replaces the
# original source filename in proguard-release-rules.pro.
-keepattributes SourceFile,LineNumberTable

# Older app versions stored these objects directly through Hawk. Hawk embeds
# runtime class names in its payload and resolves them later with Class.forName().
# New writes use stable JSON strings, but these names are retained for migration.
-keepnames class com.colorcall.callerscreen.database.Background
-keepnames class com.colorcall.callerscreen.model.AdsConfig
