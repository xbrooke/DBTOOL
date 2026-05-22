# Xposed Module ProGuard Rules
-keep class de.robv.android.xposed.** { *; }
-keep class com.unpacker.hook.** { *; }

# 保持native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持反射调用
-keepclassmembers class * {
    @de.robv.android.xposed.* <methods>;
}
