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
-keepattributes SourceFile,LineNumberTable

-renamesourcefileattribute ''
-repackageclasses ''
-allowaccessmodification

-optimizationpasses 99

-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

-dontwarn android.support.**

-keep class ** implements de.robv.android.xposed.IXposedHookLoadPackage {
     public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
}

-keep class net.manhong2112.downloadredirect.DLApi.DownloadConfig { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.internal.** { *; }
-keep class kotlin.* { *; }
-optimizationpasses 99