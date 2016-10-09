package net.manhong2112.downloadredirect

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.ArrayAdapter
import org.jetbrains.anko.*
import java.util.*


/**
 * Created by manhong2112 on 10/4/2016.
 * Setting Page
 */

class Main : Activity() {
   override fun onCreate(bundle: Bundle?) {
      super.onCreate(bundle)
      MainUi().setContentView(this)
   }

   companion object {
      fun log(DEBUG: Boolean, str: String) {
         if (DEBUG) Log.d("Xposed", "DownloadRedirect -> $str")
      }
   }
}

class MainUi : AnkoComponent<Main> {
   fun _RelativeLayout.CLabel(viewId: Int, _text: String) = textView {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
      gravity = Gravity.CENTER_VERTICAL
   }

   fun _RelativeLayout.CLabel(viewId: Int, textResId: Int) =
           CLabel(viewId, resources.getString(textResId))

   fun _RelativeLayout.CSubTitle(viewId: Int, _text: String) = textView {
      if (android.os.Build.VERSION.SDK_INT >= 21) {
         elevation = dip(2).toFloat()
      }
      gravity = Gravity.CENTER_VERTICAL
      id = viewId
      text = _text
      backgroundColor = 0x505050.opaque
      setPadding(dip(8), 0, 0, 0)
   }

   fun _RelativeLayout.CSubTitle(viewId: Int, textResId: Int) =
           CSubTitle(viewId, resources.getString(textResId))

   fun _RelativeLayout.CSwitch(viewId: Int, _text: String, onClickF: (View?) -> Unit) = switch {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
      onClick(onClickF)
   }

   fun _RelativeLayout.CSwitch(viewId: Int, textResId: Int, onClickF: (View?) -> Unit) =
           CSwitch(viewId, resources.getString(textResId), onClickF)

   override fun createView(ui: AnkoContext<Main>) = ui.apply {
      val Pref = ConfigDAO(ctx, ctx.getSharedPreferences("pref", 1))

      val ColumnHeight = dip(48)
      val SubTitleHeight = dip(36)

      if (Pref.ExistingDownloader.size == 0) {
         toast(R.string.toast_no_supported_downloader)
      }
      scrollView {
         relativeLayout {
            lparams {
               width = matchParent
            }
            id = Const.id.Pref_Page
            // Debug
            CSubTitle(Const.id.Debug_Label, R.string.label_debug)
                    .lparams {
                       width = matchParent
                       height = SubTitleHeight
                    }

            CSwitch(Const.id.Debug_Logging_Switch, R.string.switch_debug_logging) { view ->
               Pref.Debug = (view as android.widget.Switch).isChecked
            }.lparams {
               width = matchParent
               height = ColumnHeight
               below(Const.id.Debug_Label)
            }.isChecked = Pref.Debug

            // Pref
            CSubTitle(Const.id.Pref_Label, R.string.label_preferences).lparams {
               below(Const.id.Debug_Logging_Switch)
               width = matchParent
               height = SubTitleHeight
            }

            CSwitch(Const.id.Pref_HideIcon_Switch, R.string.switch_hide_app_icon)
            { view ->
               Pref.HideIcon = (view as android.widget.Switch).isChecked
               ctx.packageManager.setComponentEnabledSetting(
                       ComponentName(ctx, "net.manhong2112.downloadredirect.Main-Icon"),
                       if (Pref.HideIcon)
                          PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                       else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                       PackageManager.DONT_KILL_APP)
            }.lparams {
               below(Const.id.Pref_Label)
               width = matchParent
               height = ColumnHeight
            }.isChecked = Pref.HideIcon


            CSwitch(Const.id.Pref_Ignore_System_App, R.string.switch_ignore_system_app)
            {
               view ->
               Pref.IgnoreSystemApp = (view as android.widget.Switch).isChecked
            }.lparams {
               width = matchParent
               height = ColumnHeight
               below(Const.id.Pref_HideIcon_Switch)
            }.isChecked = Pref.IgnoreSystemApp

            val existedApiName =
                    Const.ApiList
                            .filter {
                               it.getMethod("isExist", Context::class.java)
                                       .invoke(it.newInstance(), ctx) as Boolean
                            }
                            .map {
                               it.getMethod("getName")
                                       .invoke(it.newInstance()) as String
                            }

            if (existedApiName.size > 1) {
               val b = CLabel(Const.id.Pref_Using_Downloader, Pref.Downloader)
                       .lparams {
                          rightMargin = dip(16)
                          height = ColumnHeight
                          below(Const.id.Pref_Ignore_System_App)
                          alignParentRight()
                       }
               b.textColor = 0xFFFFFF.opaque
               b.gravity = Gravity.CENTER_VERTICAL
               b.onClick {
                  selector(ctx.getString(R.string.selector_downloader), existedApiName) {
                     i: Int ->
                     toast(ctx.getString(R.string.toast_change_downloader, existedApiName[i]))
                     Pref.Downloader = existedApiName[i]
                     b.text = existedApiName[i]
                  }
               }

               val l = CLabel(Const.id.Pref_Downloader_Selector, R.string.list_change_downloader)
                       .lparams {
                          width = matchParent
                          height = ColumnHeight
                          alignParentLeft()
                          below(Const.id.Pref_Ignore_System_App)
                       }
               l.textColor = 0xFFFFFF.opaque
               l.onClick {
                  selector(ctx.getString(R.string.selector_downloader), existedApiName) {
                     i: Int ->
                     toast(ctx.getString(R.string.toast_change_downloader, existedApiName[i]))
                     Pref.Downloader = existedApiName[i]
                     b.text = existedApiName[i]
                  }
               }
            }

            // Filter
            CSubTitle(Const.id.Filter_Label, R.string.label_filter).lparams {
               width = matchParent
               height = SubTitleHeight
               if (existedApiName.size > 1) {
                  below(Const.id.Pref_Using_Downloader)
               } else {
                  below(Const.id.Pref_Ignore_System_App)
               }
            }

            val b = CLabel(Const.id.Pref_Use_White_List, R.string.switch_white_list)
                    .lparams {
                       width = matchParent
                       height = ColumnHeight
                       below(Const.id.Filter_Label)
                    }
            b.textColor = 0xFFFFFF.opaque
            b.onClick {
               AlertDialog.Builder(ctx)
                       .setTitle(R.string.selector_whitelist)
                       .setMultiChoiceItems(
                               arrayOf(ctx.getString(R.string.filter_link),
                                       ctx.getString(R.string.filter_app)),
                               booleanArrayOf(Pref.UsingWhiteList_Link,
                                       Pref.UsingWhiteList_App),
                               {
                                  dialog, which, isChecked ->
                                  when (which) {
                                     0 -> Pref.UsingWhiteList_Link = isChecked
                                     1 -> Pref.UsingWhiteList_App = isChecked
                                  }
                               }).setPositiveButton(R.string.button_confirm, null).create().show()
            }

            val a = CLabel(Const.id.Link_Filter, R.string.filter_link)
                    .lparams {
                       width = matchParent
                       height = ColumnHeight
                       alignParentLeft()
                       below(Const.id.Pref_Use_White_List)
                    }
            a.textColor = 0xFFFFFF.opaque
            a.onClick {
               if (!Pref.LinkFilter.isEmpty()) {
                  val lf = Pref.LinkFilter.sorted()
                  selector(ctx.getString(R.string.list_filter_link), lf) {
                     i: Int ->
                     val x = lf[i]
                     toast(ctx.getString(R.string.toast_removed, x))
                     Main.log(Pref.Debug,
                             "Removed \"$x\" from filter")
                     Pref.LinkFilter.remove(x)
                     Pref.updateLinkFilter()
                  }
               } else {
                  toast(R.string.toast_empty_filter)
               }
            }

            val x = CLabel(Const.id.Link_Filter_Add, "+")
                    .lparams {
                       width = dip(36)
                       rightMargin = dip(20)
                       height = ColumnHeight
                       alignParentRight()
                       below(Const.id.Pref_Use_White_List)
                    }
            x.padding = 0
            x.gravity = Gravity.CENTER
            x.textColor = 0xFFFFFF.opaque
            x.textSize = sp(10).toFloat()
            x.onClick {
               alert {
                  customView {
                     verticalLayout {
                        padding = dip(24)
                        textView {
                           textResource = R.string.filter_link
                        }
                        val link = editText {
                           hint = ctx.getString(R.string.label_regex)
                        }
                        positiveButton(R.string.button_confirm) {
                           val d = link.text.trim().toString()
                           when (true) {
                              d.length == 0 ->
                                 toast(R.string.toast_empty_input)
                              Pref.LinkFilter.contains(d) ->
                                 toast(R.string.toast_rule_already_exist)
                              else -> {
                                 toast(ctx.getString(R.string.toast_added, d))
                                 Main.log(Pref.Debug, "Added \"$d\" to filter")
                                 Pref.LinkFilter.add(d)
                                 Pref.updateLinkFilter()
                              }
                           }
                        }
                        negativeButton(R.string.button_cancel) {}
                     }
                  }
               }.show()
            }

            val y = CLabel(Const.id.App_Filter, R.string.filter_app)
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       alignParentLeft()
                       below(Const.id.Link_Filter)
                    }
            y.textColor = 0xFFFFFF.opaque
            y.onClick {
               if (!Pref.AppFilter.isEmpty()) {
                  val appNameList = arrayListOf<String>()
                  Pref.AppFilter.forEach {
                     val appInfo = ctx.packageManager.getApplicationInfo(it, 0)
                     appNameList.add(ctx.packageManager.getApplicationLabel(appInfo).toString() + "\n " +
                                     it)
                  }
                  appNameList.sortBy(String::toLowerCase)
                  selector(ctx.getString(R.string.list_filter_app), appNameList) {
                     i: Int ->
                     val app = appNameList[i].split("\n ")
                     toast(ctx.getString(R.string.toast_removed, app[0]))
                     Main.log(Pref.Debug,
                             "Removed \"${app[0]} | ${app[1]}\" from filter")
                     Pref.AppFilter.remove(app[1])
                     Pref.updateAppFilter()
                  }
               } else {
                  toast(R.string.toast_empty_filter)
               }
            }

            val z = CLabel(Const.id.App_Filter_Add, "+")
                    .lparams {
                       width = dip(36)
                       rightMargin = dip(20)
                       height = ColumnHeight
                       alignParentRight()
                       below(Const.id.Link_Filter_Add)
                    }
            z.padding = 0
            z.gravity = Gravity.CENTER
            z.textColor = 0xFFFFFF.opaque
            z.textSize = sp(10).toFloat()
            z.onClick {
               val appNameList = arrayListOf<String>()
               loop@for (l in ctx.packageManager.getInstalledPackages(0)) {
                  when (true) {
                     (Pref.IgnoreSystemApp &&
                             ((l.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1)) ->
                        continue@loop
                     (Pref.AppFilter.contains(l.packageName)) ->
                        continue@loop
                  }
                  appNameList.add(
                          ctx.packageManager.getApplicationLabel(l.applicationInfo).toString() + "\n " +
                                  l.packageName)
               }
               appNameList.sortBy(String::toLowerCase)
               alert {
                  customView {
                     verticalLayout {
                        padding = dip(12)
                        val aa = ArrayAdapter<String>(ctx,
                                android.R.layout.simple_list_item_1,
                                ArrayList<String>(appNameList))
                        listView {
                           title(R.string.selector_app)
                           adapter(aa) {
                              i: Int ->
                              val item = aa.getItem(i).split("\n ")
                              when (true) {
                                 Pref.AppFilter.contains(item[1]) ->
                                    toast(R.string.toast_rule_already_exist)
                                 else -> {
                                    Main.log(Pref.Debug, "Added \"${item[0]} | ${item[1]}\" to filter")
                                    toast(ctx.getString(R.string.toast_added, item[0]))
                                    Pref.AppFilter.add(item[1])
                                    Pref.updateAppFilter()
                                 }
                              }
                           }
                        }
                        relativeLayout {
                           val s = editText {
                              hint = ctx.getString(R.string.label_search)
                              singleLine = true
                              imeOptions = IME_ACTION_DONE
                           }.lparams() {
                              alignParentLeft()
                              width = matchParent
                           }
                           s.setOnEditorActionListener {
                              textView, i, keyEvent ->
                              aa.clear()
                              aa.addAll(appNameList.filter { it -> it.split("\n ")[0].contains(s.text) })
                              false
                           }
                        }
                     }
                  }
               }.show().dialog!!.window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }


            // About
            CSubTitle(Const.id.About_Label, R.string.label_about).lparams {
               width = matchParent
               height = SubTitleHeight
               below(Const.id.App_Filter)
            }

            CLabel(Const.id.About_Version, "${ctx.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}")
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Label)
                    }
            val s = (String(android.util.Base64.decode(ctx.getString(R.string.Info), 0)).split("|"))
            var i = 1
            CLabel(Const.id.About_Author, String(Base64.decode(s[--i], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Version)
                    }
            CLabel(Const.id.About_Email, String(Base64.decode(s[(i++).plus(++i)], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Author)
                    }
            CLabel(Const.id.About_Github, String(Base64.decode(s[--i], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Email)
                    }

         }
      }
   }.view

}