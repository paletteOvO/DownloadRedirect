package net.manhong2112.downloadredirect.DLApi

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import net.manhong2112.downloadredirect.Const.ACTION_DOWNLOAD_REDIRECT

/**
 * Created by manhong2112 on 12/4/2016.
 */

class ADMApi : DLApi() {
   override val APP_NAME = "ADM"
   override val PACKAGE_NAME = "com.dv.adm"
   val PACKAGE_NAME_PAY = "com.dv.adm.pay"

   override fun getVersion(ctx: Context): Int {
      val packageManager = ctx.packageManager ?: return -1
      val packages = packageManager.getInstalledPackages(0)

      for (info in packages) {
         when (info.packageName) {
            PACKAGE_NAME, PACKAGE_NAME_PAY ->
               return info.versionCode
         }
      }
      return -1
   }

   override fun addDownload(ctx: Context, url: Uri, cookies: String): Boolean {
      if (!isExist(ctx)) {
         return false
      }

      val intent = Intent(ACTION_DOWNLOAD_REDIRECT, url)
      intent.putExtra("Cookies", cookies)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.addCategory(Intent.CATEGORY_DEFAULT)

      try {
         ctx.startActivity(intent)
         return true
      } catch (e: ActivityNotFoundException) {
         return false
      }

   }
}
