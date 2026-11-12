# Add project specific ProGuard rules here.
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }
-keepnames class com.example.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.firebase.**
-dontwarn org.slf4j.**
