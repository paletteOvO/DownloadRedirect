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
import net.manhong2112.downloadredirect.DLApi.ADMApi
import net.manhong2112.downloadredirect.DLApi.DLApi
import java.util.*


/**
 * Created by manhong2112 on 23/3/2016.
 * Main activity of xposed hook
 */
@Suppress("UNCHECKED_CAST")
class XposedHook : IXposedHookZygoteInit, IXposedHookLoadPackage {
   override fun handleLoadPackage(params: XC_LoadPackage.LoadPackageParam?) {
      val Pref = ConfigDAO.getPref()
      if (Pref.Experiment &&
              params != null &&
              params.packageName == "android") {
         val clsPMS = XposedHelpers.findClass("android.content.pm.PackageParser", params.classLoader)
         if (Const.VER_GE_LOLLIPOP) {
            log("enabled experiment function, hooking android.content.pm.PackageParser.parseBaseApk()")
            XposedBridge.hookAllMethods(clsPMS,
                    "parseBaseApk",
                    injectFilter)
            log("Hooked parseBaseApk()")
         } else {
            log("enabled experiment function, hooking android.content.pm.PackageParser.parsePackage()")
            XposedBridge.hookAllMethods(clsPMS,
                    "parsePackage",
                    injectFilter)
            log("Hooked parsePackage()")
         }

      }
   }

   fun log(str: String, DEBUG: Boolean = true) {
      if (DEBUG) {
         XposedBridge.log("DownloadRedirect -> $str")
      }
   }
   @Throws(Throwable::class)
   override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
      val Pref = ConfigDAO.getPref()
      log("module initing")
      log("hooking android.app.DownloadManager.enqueue()")

      XposedBridge.hookAllMethods(DownloadManager::class.java, "enqueue", enqueueHook)

      try {
         val EXPERIMENT = Pref.Experiment
         if (!EXPERIMENT) {
            log("hooking android.content.pm.PackageParser.parsePackage()")
            val PackageParser = findClass("android.content.pm.PackageParser", null)
            XposedBridge.hookAllMethods(PackageParser, "parsePackage", injectFilter)
         }
      } catch (e: NoSuchMethodError) {
         log("Failed to Hook parsePackage()")
      }
      log("init ended")

   }

   private val enqueueHook = object : XC_MethodHook() {
      @Throws(Throwable::class)
      override fun beforeHookedMethod(param: MethodHookParam) {
         val ctx = AndroidAppHelper.currentApplication()
         val Pref = ConfigDAO.getPref()
         val existingDownloader = Pref.getExistingDownloader(ctx)
         val appFilter = Pref.AppFilter
         val linkFilter = Pref.LinkFilter
         log("received download request", Pref.Debug)
         val req = param.args[0] as DownloadManager.Request
         val mUri = getObjectField(req, "mUri") as Uri
         val cookies = with((getObjectField(req, "mRequestHeaders") as List<Pair<String, String>>).filter {
                           it.first == "Cookies"
                       }) {
                           if(size==0) "" else get(0).second
                       }
         when(true) {
            existingDownloader.isEmpty() -> {
               log("do not detected any supported downloader, aborted", Pref.Debug)
               return
            }
            (Pref.IgnoreSystemApp &&
                    ((ctx.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1)) -> {
               log("enabled ignore system & detected system app, aborted", Pref.Debug)
               return
            }
         }

         if (Pref.UsingWhiteList_App) {
            log("filtering app with whitelist rule", Pref.Debug)
            if (!appFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               log("app is not in the list, aborted", Pref.Debug)
               return
            }
         } else {
            log("filtering app with blacklist rule", Pref.Debug)
            if (appFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               log("app is in the list, aborted", Pref.Debug)
               return
            }
         }

         if (Pref.UsingWhiteList_Link) {
            log("Matching link with whitelist rule", Pref.Debug)
            var not_match = true
            linkFilter.forEach {
               Main.log("Matching -> $it", Pref.Debug)
               if (mUri.toString().matches(it.toRegex())) {
                  not_match = false
                  return@forEach
               }
            }
            if (not_match) {
               log("doesn't matched any regex, aborted", Pref.Debug)
               return
            }
         } else {
            log("Matching link with blacklist rule", Pref.Debug)
            linkFilter.forEach {
               Main.log("Matching -> $it", Pref.Debug)
               if (mUri.toString().matches(it.toRegex())) {
                  log("matched $it, aborted", Pref.Debug)
                  return
               }
            }
         }

         log("Url to be redirected -> $mUri", Pref.Debug)
         val existingApi = Const.ApiList.filterTo(LinkedList<Class<*>>()) {
            (it.newInstance() as DLApi).isExist(ctx)
         }

         if (existingApi.isEmpty()) {
            log("doesn't exist any downloader, aborted", Pref.Debug)
            return
         }

         val choseAPI = Pref.Downloader
         val v:Boolean
         if (existingDownloader.contains(choseAPI)) {
            v = (existingApi[existingDownloader.indexOf(choseAPI)].newInstance() as DLApi).addDownload(ctx, mUri, cookies)
         } else {
            v = (existingApi[0].newInstance() as DLApi).addDownload(ctx, mUri, cookies)
            val i = Intent(Const.ACTION_RESET_DOWNLOADER)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            ctx.sendBroadcast(i)
         }

         log("Redirection: ${if (v) "Success" else "Failed"}", Pref.Debug)
         if (v) {
            param.result = 0
         }
      }
   }

   private val injectFilter = object : XC_MethodHook() {
      @Throws(Throwable::class)
      override fun afterHookedMethod(param: MethodHookParam) {
         val ActivityIntentInfo = findClass("android.content.pm.PackageParser\$ActivityIntentInfo", null)
         val DEBUG: Boolean = ConfigDAO.getPref().Debug
         if(param.result == null) {
            log("param.result is null", DEBUG)
            log("${param.args[0]}", DEBUG)
            return
         }
         val packageName = getObjectField(param.result, "packageName")
         if (packageName != ADMApi().PACKAGE_NAME &&
                 packageName != ADMApi().PACKAGE_NAME_PAY) {
            return
         }
         log("found ADM package")
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
