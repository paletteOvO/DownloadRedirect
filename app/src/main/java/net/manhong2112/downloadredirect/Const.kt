package net.manhong2112.downloadredirect

import android.os.Build
import android.os.Environment
import net.manhong2112.downloadredirect.DLApi.*
import java.util.*

/**
 * Created by manhong2112 on 11/4/2016.
 * Const Data
 */
object Const {
   const val ACTION_DOWNLOAD_REDIRECT = "net.manhong2112.intent.DOWNLOAD_REDIRECTION"
   const val PACKAGE_NAME = "net.manhong2112.downloadredirect"
   // version greater or equal than xxx
   val VER_GE_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
   val VER_GE_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

   val LOGCAT_PATH = Environment.getExternalStorageDirectory().path + "/download_redirect.log"
   const val PACKAGE_NAME_ADM = "com.dv.adm"
   const val PACKAGE_NAME_ADMPro = "com.dv.adm.pay"
   val defaultDownloadConfig: HashMap<String, DownloadConfig> by lazy {
      hashMapOf(
            "ADM" to DownloadConfig("ADM", "com.dv.adm", ACTION_DOWNLOAD_REDIRECT, listOf(Pair("Cookie", "Cookies"))),
            "ADMPro" to DownloadConfig("ADMPro", "com.dv.adm.pay", ACTION_DOWNLOAD_REDIRECT, listOf(Pair("Cookie", "Cookies"))),
            "LoaderDroid" to DownloadConfig("LoaderDroid", "org.zloy.android.downloader", "org.zloy.android.downloader.action.ADD_LOADING", listOf(Pair("Cookie", "cookies"), Pair("Referer", "referer"))),
            "QKADM" to DownloadConfig("QKADM", "com.vanda_adm.vanda", "com.vanda.adm.friends", listOf(Pair("Cookie", "cookie")))
      )
   }


   object id {
      const val Pref_Page = 0
      const val Debug_Label = 1
      const val Debug_List = 2
      const val Debug_Logging_Switch = 3
      const val Pref_Label = 4
      const val Pref_List = 5
      const val Pref_HideIcon_Switch = 6
      const val Filter_Label = 7
      const val Filter_List = 8
      const val Link_Filter = 9
      const val Link_Filter_Add = 10
      const val App_Filter = 11
      const val App_Filter_Add = 12
      const val About_Label = 13
      const val About_Version = 14
      const val Pref_Downloader_Selector = 15
      const val Pref_Ignore_System_App = 16
      const val Pref_Use_White_List = 17
      const val Pref_Using_Downloader = 18
      const val About_Github = 19
      const val About_Email = 20
      const val About_Author = 21
      const val About_ListView = 22
      const val Debug_Experiment_Switch = 23
      const val Debug_Redirection_Test = 24
      const val Pref_NotSpecifyDownloader_Switch = 25
   }
}

