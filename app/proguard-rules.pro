# FileGuardPro ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room entities
-keep class com.fileguardpro.scanner.database.** { *; }
-keep class com.fileguardpro.scanner.model.** { *; }

# Apache POI
-dontwarn org.apache.poi.**
-keep class org.apache.poi.** { *; }

# PDFBox
-dontwarn com.tom_roush.pdfbox.**
-keep class com.tom_roush.pdfbox.** { *; }

# Tika
-dontwarn org.apache.tika.**
-keep class org.apache.tika.** { *; }
