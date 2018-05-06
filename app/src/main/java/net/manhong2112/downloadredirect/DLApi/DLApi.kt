package net.manhong2112.downloadredirect.DLApi

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import net.manhong2112.downloadredirect.Main
import net.manhong2112.downloadredirect.ConfigDAO

/**
 * Created by manhong2112 on 12/9/2017.
 */

typealias Name = String
typealias Value = String
typealias Header = List<Pair<String, String>>
object DLApi {
   fun addDownload(ctx: Context, url: Uri, mRequestHeaders: List<Pair<String, String>>): Boolean {
      val Pref = ConfigDAO.getXPref()
      val DEBUG = Pref.Debug
      Main.log("addDownload(Context, Uri, List)", DEBUG)
      val intent = Intent(Intent.ACTION_VIEW, url)
      mRequestHeaders.forEach {
         (name, value) ->
         when (name.toLowerCase()) {
            "cookies", "cookie" -> {
               intent.putExtra("Cookies", value)
               intent.putExtra("cookies", value)
               intent.putExtra("Cookie", value)
               intent.putExtra("cookie", value)
               Main.log("Put 'Cookie' -> ${value}", DEBUG)
            }
            "referer" -> {
               intent.putExtra("referer", value)
               intent.putExtra("Referer", value)
               Main.log("Put 'Referer' -> ${value}", DEBUG)
            }
            else -> {
               intent.putExtra(name, value)
               Main.log("Put ${name} -> ${value}", DEBUG)
            }
         }
      }
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.addCategory(Intent.CATEGORY_DEFAULT)

      return try {
         ctx.startActivity(intent)
         true
      } catch (e: ActivityNotFoundException) {
         false
      }
   }

   fun addDownload(ctx: Context, url: Uri, mRequestHeaders: List<Pair<String, String>>, downloadConfig: DownloadConfig): Boolean {
      val Pref = ConfigDAO.getXPref()
      val DEBUG = Pref.Debug
      Main.log("addDownload(Context, Uri, List, DownloadConfig)", DEBUG)
      val intent = Intent(downloadConfig.intent, url)
      intent.`package` = downloadConfig.packageName
      mRequestHeaders.forEach {
         (name, value) ->
         when (name.toLowerCase()) {
            "cookies", "cookie" -> {
               val k = downloadConfig.headers.firstOrNull { (name, _) -> name == "Cookie" }?.second ?: ""
               intent.putExtra(k, value)
               Main.log("Put ${k} -> ${value}", DEBUG)
            }
            "referer" -> {
               val k = downloadConfig.headers.firstOrNull { (name, _) -> name == "Referer" }?.second ?: ""
               intent.putExtra(k, value)
               Main.log("Put ${k} -> ${value}", DEBUG)
            }
            else -> {
               intent.putExtra(name, value)
               Main.log("Put ${name} -> ${value}", DEBUG)
            }
         }
      }
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.addCategory(Intent.CATEGORY_DEFAULT)

      return try {
         ctx.startActivity(intent)
         true
      } catch (e: ActivityNotFoundException) {
         false
      }
   }

}



