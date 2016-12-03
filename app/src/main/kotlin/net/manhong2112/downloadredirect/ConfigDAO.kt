package net.manhong2112.downloadredirect

import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import de.robv.android.xposed.XSharedPreferences
import net.manhong2112.downloadredirect.DLApi.DLApi
import java.util.*

/**
 * Created by manhong2112 on 18/7/2016.
 * config manager
 */

class ConfigDAO(ctx: Context, pref: SharedPreferences) {
   companion object {
      fun getPref(ctx: Context = AndroidAppHelper.currentApplication()): ConfigDAO {
         val pref = XSharedPreferences(Const.PACKAGE_NAME, "pref")
         pref.makeWorldReadable()
         pref.reload()
         return ConfigDAO(ctx, pref)
      }
   }

   private val Pref = pref
   val ExistingDownloader = Const.ApiList
           .filter { (it.newInstance() as DLApi).isExist(ctx) }
           .map { (it.newInstance() as DLApi).getName() }
      get() = field

   val LinkFilter: HashSet<String> =
           Pref.getStringSet("LinkFilter", setOf<String>()).toHashSet()
      get() = field

   val AppFilter: HashSet<String> =
           Pref.getStringSet("AppFilter", setOf<String>()).toHashSet()
      get() = field



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

   var Downloader: String = ""
      get() {
         if (field == "") {
            field = Pref.getString("Downloader", null) ?: this.ExistingDownloader.first()
         }
         return field
      }
      set(s) {
         field = s
         Pref.edit().putString("Downloader", s).apply()
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

   fun updateLinkFilter() {
      Pref.edit().putStringSet("LinkFilter", LinkFilter.toSet()).apply()
   }

   fun updateAppFilter() {
      Pref.edit().putStringSet("AppFilter", AppFilter.toSet()).apply()
   }

}
