package net.manhong2112.downloadredirect.DLApi

import android.content.Context
import android.net.Uri

/**
 * Created by manhong2112 on 14/4/2016.
 */
abstract class DLApi {
    abstract val PACKAGE_NAME: String
    abstract val APP_NAME: String

    open fun getName():String {
        return APP_NAME
    }
    open fun getVersion(ctx: Context): Int {
        val packageManager = ctx.packageManager ?: return -1
        val packages = packageManager.getInstalledPackages(0)
        packages.forEach {
            when (it.packageName) {
                PACKAGE_NAME ->
                    return it.versionCode
            }
        }
        return -1
    }
    abstract fun addDownload(ctx: Context, url: Uri): Boolean
    open fun isExist(ctx: Context): Boolean {
        return getVersion(ctx) != -1
    }
}
