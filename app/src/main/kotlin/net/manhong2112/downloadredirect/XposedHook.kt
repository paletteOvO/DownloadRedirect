package net.manhong2112.downloadredirect

import android.app.AndroidAppHelper
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.net.Uri
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedHelpers.*
import java.io.File
import java.util.*

/**
 * Created by manhong2112 on 23/3/2016.
 * Main activity of xposed hook
 */
@Suppress("UNCHECKED_CAST")
class XposedHook : IXposedHookZygoteInit {
   @Throws(Throwable::class)
   override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
      val PackageParser = findClass("android.content.pm.PackageParser", null)
      val m = findMethodExact(PackageParser, "parsePackage", File::class.java, Integer.TYPE)
      m.isAccessible = true

      XposedBridge.hookMethod(m, parsePackageHook)

      findAndHookMethod(DownloadManager::class.java,
              "enqueue", DownloadManager.Request::class.java,
              enqueueHook)
   }

   private val enqueueHook = object : XC_MethodHook() {
      @Throws(Throwable::class)
      override fun afterHookedMethod(param: MethodHookParam) {
         val mUri = getObjectField(param.args[0], "mUri") as Uri
         val ctx = AndroidAppHelper.currentApplication()
         val Pref = getPref(ctx)
         when(true) {
            Pref.ExistingDownloader.size == 0 ->
                    return
            (Pref.IgnoreSystemApp &&
                    ((ctx.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1)) ->
                    return
         }

         if (Pref.UsingWhiteList_App) {
            if (!Pref.AppFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               return
            }
         } else {
            if (Pref.AppFilter.contains(AndroidAppHelper.currentApplication().packageName)) {
               return
            }
         }

         if (Pref.UsingWhiteList_Link) {
            var not_match = true
            Pref.LinkFilter.forEach {
               Main.log(Pref.Debug, "Matching Link Filter-> $it")
               if (mUri.toString().matches(it.toRegex())) {
                  not_match = false
                  return@forEach
               }
            }
            if(not_match) {return}
         } else {
            Pref.LinkFilter.forEach {
               Main.log(Pref.Debug, "Matching Link Filter-> $it")
               if (mUri.toString().matches(it.toRegex())) {
                  return
               }
            }
         }

         Main.log(Pref.Debug, "Redirected Url -> $mUri")

         val existedApi = LinkedList<Class<*>>()

         for (c in Const.ApiList) {
            if (XposedHelpers.callMethod(c.newInstance(), "isExist", ctx) as Boolean) {
               existedApi.add(c)
            }
         }
         if (existedApi.isEmpty()) {
            return
         }

         val choseAPI = Pref.Downloader
         val v:Boolean
         if (!Pref.ExistingDownloader.contains(choseAPI)) {
            v = XposedHelpers.callMethod(existedApi[0].newInstance(), "addDownload", ctx, mUri) as Boolean
            Pref.Downloader = Pref.ExistingDownloader[0]
         } else {
            v = XposedHelpers.callMethod(existedApi[Pref.ExistingDownloader.indexOf(choseAPI)].newInstance(),
                    "addDownload", ctx, mUri) as Boolean
         }
         Main.log(Pref.Debug, "Redirection: ${if(v) "Success" else "Failed"}")
         if(!v) {
            return
         }
         (param.thisObject as DownloadManager).remove(param.result as Long)
         param.result = 0
      }
   }

   private val parsePackageHook = object : XC_MethodHook() {
      val ActivityIntentInfo = findClass("android.content.pm.PackageParser\$ActivityIntentInfo", null)
      @Throws(Throwable::class)
      override
      fun afterHookedMethod(param: MethodHookParam) {
         if(param.result == null) {
            Main.log(true, "${param.args[0]}")
            return
         }
         val activities = getObjectField(param.result, "activities") as ArrayList<*>
         if (activities.isEmpty()) return
         // List of Activity
         for (activity in activities) {
            // obj.activity
            val info = getObjectField(activity, "info") as ActivityInfo
            when (info.name) {
               "com.dv.adm.pay.AEditor", "com.dv.adm.AEditor" -> {
                  Main.log(true, "Inject Redirect Intent")
                  val intent = newInstance(ActivityIntentInfo, activity) as IntentFilter
                  intent.addDataScheme("http")
                  intent.addDataScheme("https")
                  intent.addAction(Const.ACTION_DOWNLOAD_REDIRECT)
                  intent.addCategory(Intent.CATEGORY_DEFAULT)

                  callMethod(getObjectField(activity, "intents"), "add", intent)
               }
            }
         }
      }
   }

   private fun getPref(ctx: Context): ConfigDAO {
      val pref = XSharedPreferences(Const.PACKAGE_NAME, "pref")
      pref.makeWorldReadable()
      pref.reload()
      return ConfigDAO(ctx, pref)
   }
}
