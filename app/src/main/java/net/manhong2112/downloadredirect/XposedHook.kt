package net.manhong2112.downloadredirect

import android.app.AndroidAppHelper
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.net.Uri
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.manhong2112.downloadredirect.DLApi.DLApi
import java.util.*
import android.util.Log


/**
 * Created by manhong2112 on 23/3/2016.
 * Main activity of xposed hook
 */
@Suppress("UNCHECKED_CAST")
class XposedHook : IXposedHookZygoteInit, IXposedHookLoadPackage {
   fun log(str: String, debug: Boolean = true) {
      if(debug) {
         Log.i("Xposed", "DownloadRedirect -> $str")
      }
   }
   override fun handleLoadPackage(params: XC_LoadPackage.LoadPackageParam?) {
      val Pref = ConfigDAO.getXPref()
      val DEBUG = Pref.Debug
      if (Pref.Experiment &&
              params != null &&
              params.packageName == "android") {
         val clsPMS = XposedHelpers.findClass("android.content.pm.PackageParser", params.classLoader)
         if (Const.VER_GE_LOLLIPOP) {
            log("enabled experiment function, hooking android.content.pm.PackageParser.parseBaseApk()", DEBUG)
            XposedBridge.hookAllMethods(clsPMS,
                    "parseBaseApk",
                    injectFilter)
            log("Hooked parseBaseApk()", DEBUG)
         } else {
            log("enabled experiment function, hooking android.content.pm.PackageParser.parsePackage()", DEBUG)
            XposedBridge.hookAllMethods(clsPMS,
                    "parsePackage",
                    injectFilter)
            log("Hooked parsePackage()", DEBUG)
         }

      }
   }

   @Throws(Throwable::class)
   override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
      val Pref = ConfigDAO.getXPref()
      val DEBUG = Pref.Debug
      log("module initing", DEBUG)
      log("hooking android.app.DownloadManager.enqueue()", DEBUG)

      XposedBridge.hookAllMethods(DownloadManager::class.java, "enqueue", enqueueHook)

      try {
         val EXPERIMENT = Pref.Experiment
         if (!EXPERIMENT) {
            log("hooking android.content.pm.PackageParser.parsePackage()", DEBUG)
            val PackageParser = findClass("android.content.pm.PackageParser", null)
            XposedBridge.hookAllMethods(PackageParser, "parsePackage", injectFilter)
         }
      } catch (e: NoSuchMethodError) {
         log("Failed to Hook parsePackage()", DEBUG)
      }
      log("init ended", DEBUG)

   }

   private val enqueueHook = object : XC_MethodHook() {
      @Throws(Throwable::class)
      override fun beforeHookedMethod(param: MethodHookParam) {
         val ctx = AndroidAppHelper.currentApplication()
         val Pref = ConfigDAO.getXPref()
         val appFilter = Pref.AppFilter
         val linkFilter = Pref.LinkFilter
         val DEBUG = Pref.Debug
         log("received download request", DEBUG)
         val mUri = getObjectField(param.args[0], "mUri") as Uri
         val mRequestHeaders = getObjectField(param.args[0], "mRequestHeaders") as List<android.util.Pair<String, String>>

         when(true) {
            (Pref.IgnoreSystemApp &&
                    ((ctx.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1)) -> {
               log("enabled ignore system & detected system app, aborted", DEBUG)
               return
            }
         }

         if (Pref.UsingWhiteList_App) {
            log("filtering app with whitelist rule", DEBUG)
            if (!appFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               log("app is not in the list, aborted", DEBUG)
               return
            }
         } else {
            log("filtering app with blacklist rule", DEBUG)
            if (appFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               log("app is in the list, aborted", DEBUG)
               return
            }
         }

         if (Pref.UsingWhiteList_Link) {
            log("Matching link with whitelist rule", DEBUG)
            var not_match = true
            linkFilter.forEach {
               Main.log("Matching -> $it", DEBUG)
               if (mUri.toString().matches(it.toRegex())) {
                  not_match = false
                  return@forEach
               }
            }
            if (not_match) {
               log("doesn't matched any regex, aborted", DEBUG)
               return
            }
         } else {
            log("Matching link with blacklist rule", DEBUG)
            linkFilter.forEach {
               log("Matching -> $it", DEBUG)
               if (mUri.toString().matches(it.toRegex())) {
                  log("matched $it, aborted", DEBUG)
                  return
               }
            }
         }

         log("Url to be redirected -> $mUri", DEBUG)
         val selectedDownloader = Pref.SelectedDownloader
         log("Selected Downloader -> name -> ${selectedDownloader.name}", DEBUG)
         log("Selected Downloader -> intent -> ${selectedDownloader.intent}", DEBUG)
         log("NotSpecifyDownloader -> ${Pref.NotSpecifyDownloader}", DEBUG)

         val v = if(Pref.NotSpecifyDownloader)
                  DLApi.addDownload(ctx, mUri, mRequestHeaders)
                 else (DLApi.addDownload(ctx, mUri, mRequestHeaders, selectedDownloader) || DLApi.addDownload(ctx, mUri, mRequestHeaders))
         log("Redirection: ${if (v) "Success" else "Failed"}", DEBUG)
         if (v) {
            param.result = 0
         }
      }
   }

   private val injectFilter = object : XC_MethodHook() {
      @Throws(Throwable::class)
      override fun afterHookedMethod(param: MethodHookParam) {
         val Pref = ConfigDAO.getXPref()
         val DEBUG = Pref.Debug
         val ActivityIntentInfo = findClass("android.content.pm.PackageParser\$ActivityIntentInfo", null)
         if(param.result == null) {
            log("param.result is null", DEBUG)
            log("${param.args[0]}", DEBUG)
            return
         }
         val packageName = getObjectField(param.result, "packageName")
         if (packageName != Const.PACKAGE_NAME_ADM &&
                 packageName != Const.PACKAGE_NAME_ADMPro) {
            return
         }
         log("found ADM package", DEBUG)
         val activities = getObjectField(param.result, "activities") as ArrayList<*>
         if (activities.isEmpty()) return
         // List of Activity
         log("searching com.dv.adm{|.pay}.AEditor at ${param.args[0]}", DEBUG)
         for (activity in activities) {
            // obj.activity
            val info = getObjectField(activity, "info") as ActivityInfo
            when (info.name) {
               "com.dv.adm.pay.AEditor", "com.dv.adm.AEditor" -> {
                  log("Injecting Redirect Intent", DEBUG)
                  val intent = newInstance(ActivityIntentInfo, activity) as IntentFilter
                  intent.addDataScheme("http")
                  intent.addDataScheme("https")
                  intent.addAction(Const.ACTION_DOWNLOAD_REDIRECT)
                  intent.addCategory(Intent.CATEGORY_DEFAULT)

                  callMethod(getObjectField(activity, "intents"), "add", intent)
                  log("Injected Redirect Intent", DEBUG)
                  return
               }
            }
         }
      }
   }
}
