package net.manhong2112.downloadredirect

import android.content.Context
import android.content.SharedPreferences
import de.robv.android.xposed.XSharedPreferences
import net.manhong2112.downloadredirect.DLApi.DLApi

/**
 * Created by manhong2112 on 18/7/2016.
 * config manager
 */

class ConfigDAO(pref: SharedPreferences) {
   companion object {
      fun getPref(): ConfigDAO {
         val pref = XSharedPreferences(Const.PACKAGE_NAME, "pref")
         pref.makeWorldReadable()
         pref.reload()
         return ConfigDAO(pref)
      }
   }

   private val Pref = pref
   private var ExistingDownloader: List<String>? = null
   fun getExistingDownloader(ctx: Context): List<String> {
      ExistingDownloader = ExistingDownloader ?:
              Const.ApiList
                      .filter { (it.newInstance() as DLApi).isExist(ctx) }
                      .map { (it.newInstance() as DLApi).APP_NAME }
      return ExistingDownloader!!
   }

   val LinkFilter by lazy {
      Pref.getStringSet("LinkFilter", setOf<String>()).toSortedSet()
   }

   val AppFilter by lazy {
      Pref.getStringSet("AppFilter", setOf<String>()).toSortedSet()
   }

   var Experiment = Pref.getBoolean("Experiment", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("Experiment", b).apply()
      }


   var Debug = Pref.getBoolean("Debug", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("Debug", b).apply()
      }

   var HideIcon = Pref.getBoolean("HideIcon", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("HideIcon", b).apply()
      }

   var Downloader: String? = null
      set(s) {
         field = s
         Pref.edit().putString("Downloader", s).apply()
      }

   fun getDownloader(ctx: Context): String {
      Downloader = Downloader ?: Pref.getString("Downloader", null) ?: getExistingDownloader(ctx).first()
      return Downloader!!
   }

   var UsingWhiteList_Link = Pref.getBoolean("UsingWhiteList_Link", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("UsingWhiteList_Link", b).apply()
      }

   var UsingWhiteList_App = Pref.getBoolean("UsingWhiteList_App", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("UsingWhiteList_App", b).apply()
      }

   var IgnoreSystemApp = Pref.getBoolean("IgnoreSystemApp", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("IgnoreSystemApp", b).apply()
      }

   var FirstRun = Pref.getBoolean("FirstRun", true)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("FirstRun", b).apply()
      }

   fun updateLinkFilter() {
      Pref.edit().putStringSet("LinkFilter", LinkFilter.toSet()).apply()
   }

   fun updateAppFilter() {
      Pref.edit().putStringSet("AppFilter", AppFilter.toSet()).apply()
   }

}
