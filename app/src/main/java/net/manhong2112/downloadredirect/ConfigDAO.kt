package net.manhong2112.downloadredirect

import android.content.SharedPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.robv.android.xposed.XSharedPreferences
import net.manhong2112.downloadredirect.DLApi.DownloadConfig
import java.util.*
import android.content.Context
import android.content.Context.MODE_WORLD_READABLE

/**
 * Created by manhong2112 on 18/7/2016.
 * config manager
 */

class ConfigDAO(private val pref: SharedPreferences) {
   companion object {
      fun getXPref(): ConfigDAO {
         val pref = XSharedPreferences(Const.PACKAGE_NAME, "pref")
         pref.makeWorldReadable()
         pref.reload()
         return ConfigDAO(pref)
      }

      fun getPref(ctx: Context, prefName: String = "pref"): ConfigDAO {
         return ConfigDAO(ctx.getSharedPreferences(prefName, MODE_WORLD_READABLE))
      }
   }

   val DownloadConfigs: HashMap<String, DownloadConfig> by lazy {
      val mapper = ObjectMapper().registerModule(KotlinModule())
      val k = pref.getStringSet("DownloadConfigs", null) ?: return@lazy Const.defaultDownloadConfig
      val map = HashMap<String, DownloadConfig>()
      k.forEach {
         i ->
            val v: DownloadConfig = mapper.readValue(i)
            map[v.name] = v
      }
      return@lazy map
   }

   fun updateDownloadConfigs() {
      val mapper = ObjectMapper().registerModule(KotlinModule())
      val set: Set<String> = DownloadConfigs.values.map {
         i: DownloadConfig ->
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(i)
      }.toSet()
      pref.edit().putStringSet("DownloadConfigs", set).apply()
   }

   val LinkFilter by lazy {
      pref.getStringSet("LinkFilter", setOf<String>()).toSortedSet()
   }

   val AppFilter by lazy {
      pref.getStringSet("AppFilter", setOf<String>()).toSortedSet()
   }

   var Experiment = pref.getBoolean("Experiment", false)
      set(b) {
         field = b
         pref.edit().putBoolean("Experiment", b).apply()
      }

   var NotSpecifyDownloader = pref.getBoolean("NotSpecifyDownloader", false)
      set(b) {
         field = b
         pref.edit().putBoolean("NotSpecifyDownloader", b).apply()
      }

   var Debug = pref.getBoolean("Debug", false)
      set(b) {
         field = b
         pref.edit().putBoolean("Debug", b).apply()
      }

   var HideIcon = pref.getBoolean("HideIcon", false)
      set(b) {
         field = b
         pref.edit().putBoolean("HideIcon", b).apply()
      }

   var SelectedDownloader: DownloadConfig = DownloadConfigs[pref.getString("SelectedDownloader", "ADM")]!!
      set(s) {
         field = s
         pref.edit().putString("SelectedDownloader", s.name).apply()
      }

   var UsingWhiteList_Link = pref.getBoolean("UsingWhiteList_Link", false)
      set(b) {
         field = b
         pref.edit().putBoolean("UsingWhiteList_Link", b).apply()
      }

   var UsingWhiteList_App = pref.getBoolean("UsingWhiteList_App", false)
      set(b) {
         field = b
         pref.edit().putBoolean("UsingWhiteList_App", b).apply()
      }

   var IgnoreSystemApp = pref.getBoolean("IgnoreSystemApp", false)
      set(b) {
         field = b
         pref.edit().putBoolean("IgnoreSystemApp", b).apply()
      }

   var FirstRun = pref.getBoolean("FirstRun", true)
      set(b) {
         field = b
         pref.edit().putBoolean("FirstRun", b).apply()
      }

   fun updateLinkFilter() {
      pref.edit().putStringSet("LinkFilter", LinkFilter.toSet()).apply()
   }

   fun updateAppFilter() {
      pref.edit().putStringSet("AppFilter", AppFilter.toSet()).apply()
   }

}
