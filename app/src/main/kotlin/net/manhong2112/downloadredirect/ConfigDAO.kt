package net.manhong2112.downloadredirect

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by manhong2112 on 18/7/2016.
 * config manager
 */

class ConfigDAO(ctx: Context, pref: SharedPreferences) {
   private val Pref = pref
   val ExistingDownloader = Const.ApiList
           .filter { it.getMethod("isExist", Context::class.java).invoke(it.newInstance(), ctx) as Boolean }
           .map { it.getMethod("getName").invoke(it.newInstance()) as String }
      get() = field

   val LinkFilter: MutableList<String> =
           (Pref.getStringSet("LinkFilter", null) ?: setOf<String>())
           .sorted().toMutableList()
      get() = field

   val AppFilter: MutableList<String> =
           (Pref.getStringSet("AppFilter", null) ?: setOf<String>())
           .sorted().toMutableList()
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
