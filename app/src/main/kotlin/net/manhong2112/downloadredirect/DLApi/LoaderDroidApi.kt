package net.manhong2112.downloadredirect.DLApi

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Created by manhong2112 on 11/4/2016.
 * Modify from LoaderDroidPublicApi
 */
class LoaderDroidApi : DLApi() {
   override val APP_NAME = "LoaderDroid"
   private val ACTION_ADD_LOADING = "org.zloy.android.downloader.action.ADD_LOADING"
   override val PACKAGE_NAME = "org.zloy.android.downloader"

   override fun addDownload(ctx: Context, url: Uri): Boolean {
      if (!isExist(ctx)) {
         return false
      }

      val intent = Intent(ACTION_ADD_LOADING, url)
      intent.putExtra("ask_for_directory", true)
      intent.putExtra("allowed_connection", 1)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

      try {
         ctx.startActivity(intent)
         return true
      } catch (e: ActivityNotFoundException) {
         return false
      }

   }
}
