# Keep line numbers for Crashlytics/Retrace without exposing source filenames.
-renamesourcefileattribute SourceFile

# Release-only log removal. Keep these calls in non-minified debug builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}
