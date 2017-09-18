package net.manhong2112.downloadredirect

import android.content.SharedPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.robv.android.xposed.XSharedPreferences
import net.manhong2112.downloadredirect.DLApi.DownloadConfig
import java.util.*

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
      Pref.edit().putStringSet("DownloadConfigs", set).apply()
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

   var NotSpecifyDownloader = Pref.getBoolean("NotSpecifyDownloader", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("NotSpecifyDownloader", b).apply()
      }

   var Debug = Pref.getBoolean("Debug", false)
      get() = field
      set(b) {
         field = b
         Main.DEBUG = b
         Pref.edit().putBoolean("Debug", b).apply()
      }

   var HideIcon = Pref.getBoolean("HideIcon", false)
      get() = field
      set(b) {
         field = b
         Pref.edit().putBoolean("HideIcon", b).apply()
      }

   var SelectedDownloader: DownloadConfig = DownloadConfigs[Pref.getString("SelectedDownloader", "ADM")]!!
      get() = field
      set(s) {
         field = s
         Pref.edit().putString("SelectedDownloader", s.name).apply()
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
