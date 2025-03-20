# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile




# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Keep classes that use Gson
-keep class * {
@com.google.gson.annotations.SerializedName *;
}

# Enable shrinking
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remove unused android code
-assumenosideeffects class android.util.Log {
public static *** d(...);
public static *** v(...);
}

# Keep - Applications entry point, replace "YourApplication" with your actual application class name
-keep public class * extends android.app.Application

# Keep - Activities, replace with your actual activities
-keep public class chess.com.** {
public protected *;
}

# Keep - Custom views and classes used in XML
-keepclassmembers class * {
public void set*(***);
public *** get*();
}

# Preserve annotated classes and methods
-keepclasseswithmembers class * {
@YourAnnotationPackage.** <fields>;
@YourAnnotationPackage.** <methods>;
}

# Preserve all native method classes and methods
-keepclasseswithmembernames class * {
native <methods>;
}

# Avoid removing constructors used implicitly by Android
-keepclassmembers class * {
public <init>(android.content.Context, android.util.AttributeSet);
public <init>(android.content.Context, android.util.AttributeSet, int);
}