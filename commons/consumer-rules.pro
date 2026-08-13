# Optional annotation package referenced by joda-time.
-dontwarn org.joda.convert.**

# Optional desktop API referenced by ez-vcard's FreeMarker integration.
-dontwarn javax.swing.tree.TreeNode

# FAQItem is passed through Intent extras using Java serialization.
-keepclassmembers class com.simplemobiletools.commons.models.FAQItem {
    private static final long serialVersionUID;
}
