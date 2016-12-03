package net.manhong2112.downloadredirect

import android.app.AndroidAppHelper
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.util.DisplayMetrics
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.*
import net.manhong2112.downloadredirect.DLApi.ADMApi
import net.manhong2112.downloadredirect.DLApi.DLApi
import java.io.File
import java.lang.reflect.Method
import java.util.*


/**
 * Created by manhong2112 on 23/3/2016.
 * Main activity of xposed hook
 */
@Suppress("UNCHECKED_CAST")
class XposedHook : IXposedHookZygoteInit {
   fun log(str: String, DEBUG: Boolean = true) {
      if (DEBUG) {
         XposedBridge.log("DownloadRedirect -> $str")
      }
   }
   @Throws(Throwable::class)
   override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
      log("module initing")
      log("hooking android.app.DownloadManager.enqueue()")

      findAndHookMethod(DownloadManager::class.java,
              "enqueue", DownloadManager.Request::class.java,
              enqueueHook)

      log("hooking android.content.pm.PackageParser.parsePackage()")
      val PackageParser = findClass("android.content.pm.PackageParser", null)
      val m: Method
      try {
         if(android.os.Build.VERSION.SDK_INT >= 21) {
            m = findMethodExact(PackageParser, "parsePackage", File::class.java, Integer.TYPE)
         } else {
            m = findMethodExact(PackageParser, "parsePackage",
                    File::class.java,
                    String::class.java,
                    DisplayMetrics::class.java,
                    Integer.TYPE)
         }
         m.isAccessible = true

         XposedBridge.hookMethod(m, parsePackageHook)
      } catch (e: NoSuchMethodError) {
         log("Failed to Hook parsePackage")
      }
      log("init ended")

   }

   private val enqueueHook = object : XC_MethodHook() {
      @Throws(Throwable::class)
      override fun afterHookedMethod(param: MethodHookParam) {
         val ctx = AndroidAppHelper.currentApplication()
         val Pref = ConfigDAO.getPref(ctx)
         log("received download request", Pref.Debug)
         val mUri = getObjectField(param.args[0], "mUri") as Uri
         when(true) {
            Pref.ExistingDownloader.isEmpty() -> {
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
            if (!Pref.AppFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               log("app is not in the list, aborted", Pref.Debug)
               return
            }
         } else {
            log("filtering app with blacklist rule", Pref.Debug)
            if (Pref.AppFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               log("app is in the list, aborted", Pref.Debug)
               return
            }
         }

         if (Pref.UsingWhiteList_Link) {
            log("Matching link with whitelist rule", Pref.Debug)
            var not_match = true
            Pref.LinkFilter.forEach {
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
            Pref.LinkFilter.forEach {
               Main.log("Matching -> $it", Pref.Debug)
               if (mUri.toString().matches(it.toRegex())) {
                  log("matched $it, aborted", Pref.Debug)
                  return
               }
            }
         }

         log("Redirecting Url -> $mUri", Pref.Debug)

         val existedApi = Const.ApiList.filterTo(LinkedList<Class<*>>()) {
            (it.newInstance() as DLApi).isExist(ctx)
         }

         if (existedApi.isEmpty()) {
            log("doesn't exist any downloader, aborted", Pref.Debug)
            return
         }

         val choseAPI = Pref.Downloader
         val v:Boolean
         if (!Pref.ExistingDownloader.contains(choseAPI)) {
            v = (existedApi[0].newInstance() as DLApi).addDownload(ctx, mUri)
            Pref.Downloader = Pref.ExistingDownloader[0]
         } else {
            v = (existedApi[Pref.ExistingDownloader.indexOf(choseAPI)].newInstance() as DLApi).addDownload(ctx, mUri)
         }
         log("Redirection: ${if (v) "Success" else "Failed"}", Pref.Debug)
         if(!v) {
            return
         }
         log("removing original download", Pref.Debug)
         (param.thisObject as DownloadManager).remove(param.result as Long)
         param.result = -1
      }
   }

   private val parsePackageHook = object : XC_MethodHook() {
      val ActivityIntentInfo = findClass("android.content.pm.PackageParser\$ActivityIntentInfo", null)
      @Throws(Throwable::class)
      override
      fun afterHookedMethod(param: MethodHookParam) {
         val Pref = ConfigDAO.getPref()
         if(param.result == null) {
            log("param.result is null", Pref.Debug)
            log("${param.args[0]}", Pref.Debug)
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
         log("searching com.dv.adm{|.pay}.AEditor at ${param.args[0]}", Pref.Debug)
         for (activity in activities) {
            // obj.activity
            val info = getObjectField(activity, "info") as ActivityInfo
            when (info.name) {
               "com.dv.adm.pay.AEditor", "com.dv.adm.AEditor" -> {
                  log("Injecting Redirect Intent", Pref.Debug)
                  val intent = newInstance(ActivityIntentInfo, activity) as IntentFilter
                  intent.addDataScheme("http")
                  intent.addDataScheme("https")
                  intent.addAction(Const.ACTION_DOWNLOAD_REDIRECT)
                  intent.addCategory(Intent.CATEGORY_DEFAULT)

                  callMethod(getObjectField(activity, "intents"), "add", intent)
                  log("Injected Redirect Intent", Pref.Debug)
                  return
               }
            }
         }
      }
   }
}
