# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep model classes
-keep class com.dtool.** { *; }

# Keep service classes
-keep class com.dtool.service.** { *; }
-keep class com.dtool.receiver.** { *; }
-keep class com.dtool.provider.** { *; }
