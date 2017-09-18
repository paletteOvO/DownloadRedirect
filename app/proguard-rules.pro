# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\manhong\WorkSpace\Android\SDK/tools/proguard/proguard-android.txt
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
-verbose
-dontnote **
-dontwarn **
-keep class net.manhong2112.downloadredirect.XposedHook {*;}
-keep class net.manhong2112.downloadredirect.DLApi.DownloadConfig {*;}

-optimizationpasses 99
-dontoptimize


-keep class kotlin.Metadata { *; }

-keepattributes *Annotation*
-keep class org.jetbrains.kotlin.** { *; }
-keep class org.jetbrains.annotations.** { *; }
-keepclassmembers class ** {
  @org.jetbrains.annotations.ReadOnly public *;
}