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
    override fun getName(): String {
        return "LoaderDroid"
    }

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

    override fun getVersion(ctx: Context): Int {
        val packageManager = ctx.packageManager ?: return -1
        val packages = packageManager.getInstalledPackages(0) ?: return -1
        for (info in packages) {
            if (info != null && LOADER_DROID_PACKAGE == info.packageName) {
                return info.versionCode
            }

        }
        return -1
    }


    override fun isExist(ctx: Context): Boolean {
        return getVersion(ctx) != -1
    }

    private val ACTION_ADD_LOADING = "org.zloy.android.downloader.action.ADD_LOADING"
    private val LOADER_DROID_PACKAGE = "org.zloy.android.downloader"
}
