package net.manhong2112.downloadredirect

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
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
         if (DEBUG) Log.i("Xposed", "Log -> $str")
      }

   }
}

class MainUi : AnkoComponent<Main> {
   fun _RelativeLayout.CLabel(viewId: Int, _text: String) = textView {
      id = viewId
      text = _text
      padding = dip(12)
   }
   fun _RelativeLayout.CLabel(viewId: Int, textResId: Int) =
           CLabel(viewId, resources.getString(textResId))

   fun _RelativeLayout.CSubTitle(viewId: Int, _text: String) = textView {
      elevation = dip(2).toFloat()
      id = viewId
      text = _text
      backgroundColor = 0x505050.opaque
      padding = dip(8)
   }
   fun _RelativeLayout.CSubTitle(viewId: Int, textResId: Int) =
           CSubTitle(viewId, resources.getString(textResId))

   fun _RelativeLayout.CSwitch(viewId: Int, _text: String, onClickF: (View?) -> Unit) = switch {
      id = viewId
      text = _text
      padding = dip(12)
      onClick(onClickF)
   }
   fun _RelativeLayout.CSwitch(viewId: Int, textResId: Int, onClickF: (View?) -> Unit) =
           CSwitch(viewId, resources.getString(textResId), onClickF)

   fun _RelativeLayout.COption(viewId: Int, l : TextView, b: TextView) =
           relativeLayout {
              id = viewId
              l.lparams(height = matchParent) {
                 alignParentLeft()
              }
              b.lparams(height = matchParent) {
                 alignParentRight()
              }
           }

   override fun createView(ui: AnkoContext<Main>) = ui.apply {
      val Pref = ConfigDAO(ctx, ctx.getSharedPreferences("pref", 1))

      val paddingS = dip(4)
      val ColumnHeight = dip(48)
      val SubTitleHeight = dip(36)

      if (Pref.ExistingDownloader.size == 0) {
         toast(R.string.toast_no_supported_downloader)
      }
      scrollView {
         relativeLayout {
            id = Const.id.Pref_Page
            // Debug
            CSubTitle(Const.id.Debug_Label, R.string.label_debug)
                    .lparams {
                       width = matchParent
                       height = SubTitleHeight
                    }
            relativeLayout {
               lparams {
                  width = matchParent
                  below(Const.id.Debug_Label)
               }
               padding = paddingS
               id = Const.id.Debug_List
               CSwitch(Const.id.Debug_Logging_Switch, R.string.switch_debug_logging)
               {
                  view -> Pref.Debug = (view as android.widget.Switch).isChecked
               }.lparams {
                  width = matchParent
                  height = ColumnHeight
               }.isChecked = Pref.Debug
            }


            // Pref
            CSubTitle(Const.id.Pref_Label, R.string.label_preferences).lparams {
               below(Const.id.Debug_List)
               width = matchParent
               height = SubTitleHeight
            }
            relativeLayout {
               lparams {
                  width = matchParent
                  below(Const.id.Pref_Label)
               }
               id = Const.id.Pref_List
               padding = paddingS
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
                  val l = CLabel(Const.id.Pref_Downloader_Selector, R.string.list_change_downloader)
                           .lparams {
                              width = matchParent
                              height = ColumnHeight
                              alignParentLeft()
                              below(Const.id.Pref_Ignore_System_App)
                           }
                  l.textColor = 0xFFFFFF.opaque
                  l.gravity = Gravity.CENTER_VERTICAL

                  val b = CLabel(Const.id.Pref_Using_Downloader, Pref.Downloader)
                           .lparams {
                              rightMargin = dip(12)
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
               }
            }
            // Filter
            CSubTitle(Const.id.Filter_Label, R.string.label_filter).lparams {
               width = matchParent
               height = SubTitleHeight
               below(Const.id.Pref_List)
            }
            relativeLayout {
               lparams {
                  width = matchParent
                  below(Const.id.Filter_Label)
               }
               id = Const.id.Filter_List
               padding = paddingS

               CSwitch(Const.id.Pref_Use_White_List, R.string.switch_white_list)
               {
                  view ->
                  Pref.UsingWhiteList = (view as android.widget.Switch).isChecked
               }.lparams {
                  width = matchParent
                  height = ColumnHeight
                  below(Const.id.Pref_HideIcon_Switch)
               }.isChecked = Pref.UsingWhiteList

               val a = CLabel(Const.id.Link_Filter, R.string.filter_link)
                       .lparams {
                          width = matchParent
                          height = ColumnHeight
                          alignParentLeft()
                          below(Const.id.Pref_Use_White_List)
                       }
               a.textColor = 0xFFFFFF.opaque
               a.gravity = Gravity.CENTER_VERTICAL
               a.onClick {
                  if (!Pref.LinkFilter.isEmpty()) {
                     selector(ctx.getString(R.string.selector_link),
                             Pref.LinkFilter) {
                        i: Int ->
                        toast(ctx.getString(R.string.toast_removed,
                                Pref.LinkFilter[i]))
                        Main.log(Pref.Debug,
                                "Removed \"${Pref.LinkFilter[i]}\" from filter")
                        Pref.LinkFilter.removeAt(i)
                        Pref.updateLinkFilter()
                     }
                  } else {
                     toast(R.string.toast_empty_filter)
                  }
               }

               val x = CLabel(Const.id.Link_Filter_Add, "+")
                       .lparams {
                          width = dip(24)
                          rightMargin = dip(28)
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
                              if (d.length == 0) {
                                 toast(R.string.toast_empty_input)
                              } else if (!Pref.LinkFilter.contains(d)) {
                                 toast(ctx.getString(R.string.toast_added, d))
                                 Main.log(Pref.Debug, "Added \"$d\" to filter")
                                 Pref.LinkFilter.add(d)
                                 Pref.LinkFilter.sort()
                                 Pref.updateLinkFilter()
                              } else {
                                 toast(R.string.toast_link_already_exist)
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
                          alignParentLeft()
                          below(Const.id.Link_Filter)
                       }
               y.textColor = 0xFFFFFF.opaque
               y.gravity = Gravity.CENTER_VERTICAL
               y.onClick {
                  if (!Pref.AppFilter.isEmpty()) {
                     val appNameList = ArrayList<String>()
                     Pref.AppFilter.forEach {
                        val appInfo = ctx.packageManager.getApplicationInfo(it, 0)
                        appNameList.add(
                                ctx.packageManager.getApplicationLabel(appInfo).toString())
                     }
                     selector(ctx.getString(R.string.selector_app), appNameList) {
                        i: Int ->
                        toast(ctx.getString(R.string.toast_removed, appNameList[i]))
                        Main.log(Pref.Debug,
                                "Removed \"${appNameList[i]} | ${Pref.AppFilter[i]}\" from filter")
                        Pref.AppFilter.removeAt(i)
                        Pref.updateAppFilter()
                     }
                  } else {
                     toast(R.string.toast_empty_filter)
                  }
               }

               val z = CLabel(Const.id.App_Filter_Add, "+")
                       .lparams {
                          width = dip(24)
                          rightMargin = dip(28)
                          height = ColumnHeight
                          below(Const.id.Link_Filter_Add)
                          alignParentRight()
                       }
               z.padding = 0
               z.gravity = Gravity.CENTER
               z.textColor = 0xFFFFFF.opaque
               z.textSize = sp(10).toFloat()
               z.onClick {
                  val p = ctx.packageManager.getInstalledPackages(0)
                  val appNameList = mutableListOf<String>()
                  for(l in p) {
                     if(Pref.IgnoreSystemApp &&
                       ((l.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1)) {
                        continue
                     }
                     appNameList.add(ctx.packageManager.getApplicationLabel(l.applicationInfo).toString())
                  }
                  selector(ctx.getString(R.string.selector_app), appNameList) {
                     i: Int ->
                     toast(ctx.getString(R.string.toast_added, appNameList[i]))
                     Main.log(Pref.Debug, "Added \"${appNameList[i]} | ${p[i].packageName}\" to filter")
                     Pref.AppFilter.add(p[i].packageName)
                     Pref.AppFilter.sort()
                     Pref.updateAppFilter()
                  }
               }
            }
            // About
            CSubTitle(Const.id.About_Label, "About").lparams {
               width = matchParent
               height = SubTitleHeight
               below(Const.id.Filter_List)
            }
            relativeLayout {
               lparams {
                  below(Const.id.About_Label)
                  width = matchParent
               }
               id = Const.id.About_ListView
               CLabel(Const.id.About_Version, "${ctx.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}")
               val s = (String(android.util.Base64.decode(ctx.getString(R.string.Info), 0)).split("|"))
               var i = 1
               CLabel(Const.id.About_Author, "${String(android.util.Base64.decode(s[--i], 0))}")
                       .lparams {
                          width = matchParent
                          below(Const.id.About_Version)
                       }
               CLabel(Const.id.About_Email, "${String(android.util.Base64.decode(s[i++ + ++i], 0))}")
                       .lparams {
                          width = matchParent
                          below(Const.id.About_Author)
                       }
               CLabel(Const.id.About_Github, "${String(android.util.Base64.decode(s[--i], 0))}")
                       .lparams {
                          width = matchParent
                          below(Const.id.About_Email)
                       }
            }
         }
      }

   }.view
}
