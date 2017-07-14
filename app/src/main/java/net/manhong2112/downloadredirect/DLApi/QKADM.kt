package net.manhong2112.downloadredirect.DLApi

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import net.manhong2112.downloadredirect.Const

/**
 * Created by manhong2112 on 14/7/2017.
 */
class QKADM : DLApi() {
   override val PACKAGE_NAME: String = "com.vanda_adm.vanda"
   override val APP_NAME: String = "QKADM"

   override fun addDownload(ctx: Context, url: Uri, cookies: String): Boolean {
      if (!isExist(ctx)) {
         return false
      }

      val intent = Intent("com.vanda.adm.friends", url)
      intent.putExtra("cookie", cookies)
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