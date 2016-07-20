package net.manhong2112.downloadredirect.DLApi

import android.content.Context
import android.net.Uri

/**
 * Created by manhong2112 on 14/4/2016.
 */
abstract class DLApi {
    abstract fun getName(): String
    abstract fun getVersion(ctx: Context): Int
    abstract fun addDownload(ctx: Context, url: Uri): Boolean
    abstract fun isExist(ctx: Context): Boolean
}
