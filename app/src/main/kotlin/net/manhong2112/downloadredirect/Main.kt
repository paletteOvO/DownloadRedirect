package net.manhong2112.downloadredirect

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.Switch
import net.manhong2112.downloadredirect.DLApi.DLApi
import org.jetbrains.anko.*
import java.util.*
import java.util.regex.Pattern


/**
 * Created by manhong2112 on 10/4/2016.
 * Setting Page
 */

class Main : Activity() {
   override fun onCreate(bundle: Bundle?) {
      super.onCreate(bundle)
      MainUi().setContentView(this)
   }

   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      return super.onCreateOptionsMenu(menu)
   }

   companion object {
      fun log(DEBUG: Boolean, str: String) {
         if (DEBUG) Log.d("Xposed", "DownloadRedirect -> $str")
      }

      fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
         try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
         } catch (e: PackageManager.NameNotFoundException) {
            return false
         }
      }

      fun hideIcon(ctx: Context) {
         ctx.packageManager.setComponentEnabledSetting(
                 ComponentName(ctx, "net.manhong2112.downloadredirect.Main-Icon"),
                 PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                 PackageManager.DONT_KILL_APP)
      }

      fun displayIcon(ctx: Context) {
         ctx.packageManager.setComponentEnabledSetting(
                 ComponentName(ctx, "net.manhong2112.downloadredirect.Main-Icon"),
                 PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                 PackageManager.DONT_KILL_APP)
      }
   }
}

class MainUi : AnkoComponent<Main> {
   fun _RelativeLayout.CLabel(viewId: Int, _text: String) = textView {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
      gravity = Gravity.CENTER_VERTICAL
      textColor = 0xFFFFFF.opaque
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

   fun _RelativeLayout.CSwitch(viewId: Int, _text: String, onClickF: (Switch) -> Unit) = switch {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
      onClick({view -> onClickF(view as Switch)})
   }

   fun _RelativeLayout.CSwitch(viewId: Int, textResId: Int, onClickF: (Switch) -> Unit) =
           CSwitch(viewId, resources.getString(textResId), onClickF)

   override fun createView(ui: AnkoContext<Main>) = with(ui) {
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

            CSwitch(Const.id.Debug_Logging_Switch, R.string.switch_debug_logging)
            { view ->
               Pref.Debug = view.isChecked
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
            {
               view -> Pref.HideIcon = view.isChecked
               if(view.isChecked) {
                  Main.hideIcon(ctx)
               } else {
                  Main.displayIcon(ctx)
               }
            }.lparams {
               below(Const.id.Pref_Label)
               width = matchParent
               height = ColumnHeight
            }.isChecked = Pref.HideIcon


            CSwitch(Const.id.Pref_Ignore_System_App, R.string.switch_ignore_system_app)
            {
               view -> Pref.IgnoreSystemApp = view.isChecked
            }.lparams {
               width = matchParent
               height = ColumnHeight
               below(Const.id.Pref_HideIcon_Switch)
            }.isChecked = Pref.IgnoreSystemApp

            val existedApiName =
                    Const.ApiList
                            .filter {
                               (it.newInstance() as DLApi).isExist(ctx)
                            }
                            .map {
                               (it.newInstance() as DLApi).getName()
                            }

            if (existedApiName.size > 1) {
               val b = CLabel(Const.id.Pref_Using_Downloader, Pref.Downloader)
                       .lparams {
                          rightMargin = dip(16)
                          height = ColumnHeight
                          below(Const.id.Pref_Ignore_System_App)
                          alignParentRight()
                       }
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
            a.onClick {
               if (!Pref.LinkFilter.isEmpty()) {
                  val lf = Pref.LinkFilter.sorted()
                  selector(ctx.getString(R.string.list_filter_link), lf) {
                     i: Int ->
                     alert {
                        customView {
                           verticalLayout {
                              padding = dip(24)
                              val x = lf[i]
                              textView {
                                 text = ctx.getString(R.string.dialog_remove_confirm, x)
                              }
                              positiveButton(R.string.button_confirm) {
                                 toast(ctx.getString(R.string.toast_removed, x))
                                 Main.log(Pref.Debug, "Removed \"$x\" from filter")
                                 Pref.LinkFilter.remove(x)
                                 Pref.updateLinkFilter()
                              }
                              negativeButton(R.string.button_cancel) {}
                           }
                        }
                     }.show()
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
            y.onClick {
               if (!Pref.AppFilter.isEmpty()) {
                  val appNameList = arrayListOf<String>()
                  Pref.AppFilter.forEach {
                     if(!Main.isPackageInstalled(it, ctx.packageManager)) {
                        Pref.AppFilter.remove(it)
                     } else {
                        val appInfo = ctx.packageManager.getApplicationInfo(it, 0)
                        appNameList.add(ctx.packageManager.getApplicationLabel(appInfo).toString() + "\n " +
                                it)
                     }
                     Pref.updateAppFilter()
                  }
                  appNameList.sortBy(String::toLowerCase)
                  selector(ctx.getString(R.string.list_filter_app), appNameList) {
                     i: Int ->
                     alert {
                        customView {
                           verticalLayout {
                              padding = dip(24)
                              val app = appNameList[i].split("\n ")
                              textView {
                                 text = ctx.getString(R.string.dialog_remove_confirm, app[0])
                              }
                              positiveButton(R.string.button_confirm) {
                                 toast(ctx.getString(R.string.toast_removed, app[0]))
                                 Main.log(Pref.Debug,
                                         "Removed \"${app[0]} | ${app[1]}\" from filter")
                                 Pref.AppFilter.remove(app[1])
                                 Pref.updateAppFilter()
                              }
                              negativeButton(R.string.button_cancel) {}
                           }
                        }
                     }.show()

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
                           }.lparams() {
                              alignParentLeft()
                              width = matchParent
                           }
                           s.addTextChangedListener(
                                object: TextWatcher {
                                   override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                   }
                                   override fun afterTextChanged(p0: Editable?) {
                                   }
                                   override
                                   fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                      aa.clear()
                                      //TODO 待優化...
                                      val s2 = Pattern.compile(Pattern.quote(p0.toString()), Pattern.CASE_INSENSITIVE)
                                      aa.addAll(appNameList.filter {
                                         it -> s2.matcher(it).find()
                                      })
                                   }
                                }
                           )
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
                    }.textColor = 0x9E9E9E.opaque
            val s = String(android.util.Base64.decode(ctx.getString(R.string.Info), 0)).split("|")
            var i = 1
            CLabel(Const.id.About_Author, String(Base64.decode(s[--i], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Version)
                    }.textColor = 0x9E9E9E.opaque
            CLabel(Const.id.About_Email, String(Base64.decode(s[(i++).plus(++i)], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Author)
                    }.textColor = 0x9E9E9E.opaque
            CLabel(Const.id.About_Github, String(Base64.decode(s[--i], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Email)
                    }.textColor = 0x9E9E9E.opaque

         }
      }


   }

}