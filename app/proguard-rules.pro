# ProGuard rules for TyrePressure app.
#
# ProGuard shrinks and obfuscates release builds. These rules tell it
# what NOT to remove or rename.
#
# Currently minifyEnabled is false in build.gradle, so these rules are not
# active. They are provided here for when you enable minification later.

# Keep Room database classes (Room uses reflection to find them)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Keep MPAndroidChart classes
-keep class com.github.mikephil.charting.** { *; }
