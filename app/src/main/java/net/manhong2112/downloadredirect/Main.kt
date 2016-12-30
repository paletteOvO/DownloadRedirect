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
      if (intent.action == Const.ACTION_RESET_DOWNLOADER) {
         val Pref = ConfigDAO(ctx.getSharedPreferences("pref", 1))
         Pref.Downloader = Pref.getExistingDownloader(this)[0]
      } else {
         MainUi().setContentView(this)
      }
   }

   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      return super.onCreateOptionsMenu(menu)
   }

   companion object {
      fun log(str: String, DEBUG: Boolean = true) {
         if (DEBUG) {
            Log.i("Xposed", "DownloadRedirect -> $str")
         }
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
   fun getColor(ctx: Context, id: Int): Int {
      if (Const.VER_GE_MARSHMALLOW) {
         return ctx.getColor(id)
      } else {
         @Suppress("deprecation")
         return ctx.resources.getColor(id)
      }
   }

   fun _RelativeLayout.CLabel(ctx: Context, viewId: Int, _text: String) = textView {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
      gravity = Gravity.CENTER_VERTICAL
      textColor = getColor(ctx, R.color.label_text)
   }

   fun _RelativeLayout.CLabel(ctx: Context, viewId: Int, textResId: Int) =
           CLabel(ctx, viewId, resources.getString(textResId))

   fun _RelativeLayout.CSubTitle(ctx: Context, viewId: Int, _text: String) = textView {
      if (Const.VER_GE_LOLLIPOP) {
         elevation = dip(2).toFloat()
      }
      gravity = Gravity.CENTER_VERTICAL
      id = viewId
      text = _text
      textColor = getColor(ctx, R.color.subtitle_text)
      backgroundColor = getColor(ctx, R.color.subtitle_bg)
      setPadding(dip(12), 0, 0, 0)
   }

   fun _RelativeLayout.CSubTitle(ctx: Context, viewId: Int, textResId: Int) =
           CSubTitle(ctx, viewId, resources.getString(textResId))

   fun _RelativeLayout.CSwitch(viewId: Int, _text: String) = switch {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
   }

   fun _RelativeLayout.CSwitch(viewId: Int, textResId: Int) =
           CSwitch(viewId, resources.getString(textResId))

   override fun createView(ui: AnkoContext<Main>) = with(ui) {
      val Pref = ConfigDAO(ctx.getSharedPreferences("pref", 1))
      val af = Pref.AppFilter
      val lf = Pref.LinkFilter

      val ColumnHeight = dip(48)
      val SubTitleHeight = dip(36)

      if (Pref.getExistingDownloader(ctx).isEmpty()) {
         toast(R.string.toast_no_supported_downloader)
      }
      if (Pref.FirstRun) {
         Pref.FirstRun = false
         alert("msg", "FirstRun") {

         }.show()
      }

      scrollView {
         relativeLayout {
            lparams {
               width = matchParent
            }
            id = Const.id.Pref_Page
            // Debug
            CSubTitle(ctx, Const.id.Debug_Label, R.string.label_debug)
                    .lparams {
                       width = matchParent
                       height = SubTitleHeight
                    }

            with(CSwitch(Const.id.Debug_Logging_Switch, R.string.switch_debug_logging)) {
               isChecked = Pref.Debug
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  below(Const.id.Debug_Label)
               }
               onClick {
                  Pref.Debug = (it as Switch).isChecked
               }
            }

            with(CSwitch(Const.id.Debug_Experiment_Switch, R.string.switch_debug_experiment)) {
               isChecked = Pref.Experiment
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  below(Const.id.Debug_Logging_Switch)
               }
               onClick {
                  Pref.Experiment = (it as Switch).isChecked
               }
            }

            // Pref
            CSubTitle(ctx, Const.id.Pref_Label, R.string.label_preferences).lparams {
               below(Const.id.Debug_Experiment_Switch)
               width = matchParent
               height = SubTitleHeight
            }

            with(CSwitch(Const.id.Pref_HideIcon_Switch, R.string.switch_hide_app_icon)) {
               isChecked = Pref.HideIcon
               onClick {
                  it as Switch
                  Pref.HideIcon = it.isChecked
                  if (it.isChecked) {
                     Main.hideIcon(ctx)
                  } else {
                     Main.displayIcon(ctx)
                  }
               }
               lparams {
                  below(Const.id.Pref_Label)
                  width = matchParent
                  height = ColumnHeight
               }
            }


            with(CSwitch(Const.id.Pref_Ignore_System_App, R.string.switch_ignore_system_app))
            {
               isChecked = Pref.IgnoreSystemApp
               onClick {
                  Pref.IgnoreSystemApp = (it as Switch).isChecked
               }
               lparams {

                  width = matchParent
                  height = ColumnHeight
                  below(Const.id.Pref_HideIcon_Switch)
               }
            }

            val existedApiName =
                    Const.ApiList
                            .filter {
                               (it.newInstance() as DLApi).isExist(ctx)
                            }
                            .map {
                               (it.newInstance() as DLApi).APP_NAME
                            }

            if (existedApiName.size > 1) {
               val b =
                       with(CLabel(ctx, Const.id.Pref_Using_Downloader, Pref.getDownloader(ctx))) {
                          lparams {
                             rightMargin = dip(16)
                             height = ColumnHeight
                             below(Const.id.Pref_Ignore_System_App)
                             alignParentRight()
                          }
                          onClick {
                             selector(ctx.getString(R.string.selector_downloader), existedApiName) {
                                i: Int ->
                                toast(ctx.getString(R.string.toast_change_downloader, existedApiName[i]))
                                Pref.Downloader = existedApiName[i]
                                text = existedApiName[i]
                             }
                          }
                          this
                       }

               with(CLabel(ctx, Const.id.Pref_Downloader_Selector, R.string.list_change_downloader)) {
                  lparams {
                     width = matchParent
                     height = ColumnHeight
                     alignParentLeft()
                     below(Const.id.Pref_Ignore_System_App)
                  }
                  onClick {
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
            CSubTitle(ctx, Const.id.Filter_Label, R.string.label_filter).lparams {
               width = matchParent
               height = SubTitleHeight
               if (existedApiName.size > 1) {
                  below(Const.id.Pref_Using_Downloader)
               } else {
                  below(Const.id.Pref_Ignore_System_App)
               }
            }

            with(CLabel(ctx, Const.id.Pref_Use_White_List, R.string.switch_white_list)) {
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  below(Const.id.Filter_Label)
               }
               onClick {
                  AlertDialog.Builder(ctx)
                          .setTitle(R.string.selector_whitelist)
                          .setMultiChoiceItems(
                                  arrayOf(ctx.getString(R.string.filter_link),
                                          ctx.getString(R.string.filter_app)),
                                  booleanArrayOf(Pref.UsingWhiteList_Link,
                                          Pref.UsingWhiteList_App),
                                  { dialog, which, isChecked ->
                                     when (which) {
                                        0 -> Pref.UsingWhiteList_Link = isChecked
                                        1 -> Pref.UsingWhiteList_App = isChecked
                                     }
                                  })
                          .setPositiveButton(R.string.button_confirm, null)
                          .create().show()
               }
            }

            with(CLabel(ctx, Const.id.Link_Filter, R.string.filter_link)) {
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  alignParentLeft()
                  below(Const.id.Pref_Use_White_List)
               }
               onClick {
                  if (lf.isNotEmpty()) {
                     val sortedLF = lf.sorted()
                     selector(ctx.getString(R.string.list_filter_link), sortedLF) {
                        i: Int ->
                        alert {
                           customView {
                              verticalLayout {
                                 padding = dip(24)
                                 val x = sortedLF[i]
                                 textView {
                                    text = ctx.getString(R.string.dialog_remove_confirm, x)
                                 }
                                 positiveButton(R.string.button_confirm) {
                                    toast(ctx.getString(R.string.toast_removed, x))
                                    Main.log("Removed \"$x\" from filter", Pref.Debug)
                                    lf.remove(x)
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
            }

            with(CLabel(ctx, Const.id.Link_Filter_Add, "+")) {
               lparams {
                  width = dip(36)
                  rightMargin = dip(20)
                  height = ColumnHeight
                  alignParentRight()
                  below(Const.id.Pref_Use_White_List)
               }
               padding = 0
               gravity = Gravity.CENTER
               textSize = sp(10).toFloat()
               onClick {
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
                                 d.isEmpty() ->
                                    toast(R.string.toast_empty_input)
                                 lf.contains(d) ->
                                    toast(R.string.toast_rule_already_exist)
                                 else -> {
                                    toast(ctx.getString(R.string.toast_added, d))
                                    Main.log("Added \"$d\" to filter", Pref.Debug)
                                    lf.add(d)
                                    Pref.updateLinkFilter()
                                 }
                              }
                           }
                           negativeButton(R.string.button_cancel) {}
                        }
                     }
                  }.show()
               }
            }

            with(CLabel(ctx, Const.id.App_Filter, R.string.filter_app)) {
               lparams {
                  height = ColumnHeight
                  width = matchParent
                  alignParentLeft()
                  below(Const.id.Link_Filter)
               }
               onClick {
                  if (af.isNotEmpty()) {
                     val appNameList = arrayListOf<String>()
                     af.forEach {
                        if (Main.isPackageInstalled(it, ctx.packageManager)) {
                           val appInfo = ctx.packageManager.getApplicationInfo(it, 0)
                           appNameList.add(ctx.packageManager.getApplicationLabel(appInfo).toString() + "\n " + it)
                        } else {
                           af.remove(it)
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
                                    Main.log("Removed \"${app[0]} | ${app[1]}\" from filter", Pref.Debug)
                                    af.remove(app[1])
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
            }

            with(CLabel(ctx, Const.id.App_Filter_Add, "+")) {
               lparams {
                  width = dip(36)
                  rightMargin = dip(20)
                  height = ColumnHeight
                  alignParentRight()
                  below(Const.id.Link_Filter_Add)
               }
               padding = 0
               gravity = Gravity.CENTER
               textSize = sp(10).toFloat()
               onClick {
                  val appNameList = arrayListOf<String>()
                  loop@ for (l in ctx.packageManager.getInstalledPackages(0)) {
                     when (true) {
                        Pref.IgnoreSystemApp &&
                                ((l.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1) ->
                           continue@loop
                        af.contains(l.packageName) ->
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
                           val aa = ArrayAdapter<String>(
                                   ctx,
                                   android.R.layout.simple_list_item_1,
                                   ArrayList<String>(appNameList))
                           listView {
                              title(R.string.selector_app)
                              adapter(aa) {
                                 val item = aa.getItem(it).split("\n ")
                                 if (item[1] in af) {
                                    toast(R.string.toast_rule_already_exist)
                                 } else {
                                    Main.log("Added \"${item[0]} | ${item[1]}\" to filter", Pref.Debug)
                                    toast(ctx.getString(R.string.toast_added, item[0]))
                                    af.add(item[1])
                                    Pref.updateAppFilter()
                                 }
                              }
                           }
                           relativeLayout {
                              val s = editText {
                                 hint = ctx.getString(R.string.label_search)
                                 singleLine = true
                              }.lparams {
                                 alignParentLeft()
                                 width = matchParent
                              }
                              s.addTextChangedListener(
                                      object : TextWatcher {
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
                                               s2.matcher(it).find()
                                            })
                                         }
                                      }
                              )
                           }
                        }
                     }
                  }.show().dialog!!.window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
               }
            }

            // About
            CSubTitle(ctx, Const.id.About_Label, R.string.label_about).lparams {
               width = matchParent
               height = SubTitleHeight
               below(Const.id.App_Filter)
            }

            CLabel(ctx, Const.id.About_Version, "${ctx.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}")
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Label)
                    }.textColor = getColor(ctx, R.color.label_about_text)
            val s = String(android.util.Base64.decode(ctx.getString(R.string.Info), 0)).split("|")
            var i = 1
            CLabel(ctx, Const.id.About_Author, String(Base64.decode(s[--i], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Version)
                    }.textColor = getColor(ctx, R.color.label_about_text)
            CLabel(ctx, Const.id.About_Email, String(Base64.decode(s[(i++).plus(++i)], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Author)
                    }.textColor = getColor(ctx, R.color.label_about_text)
            CLabel(ctx, Const.id.About_Github, String(Base64.decode(s[--i], 0)))
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Email)
                    }.textColor = getColor(ctx, R.color.label_about_text)

         }
      }


   }

}