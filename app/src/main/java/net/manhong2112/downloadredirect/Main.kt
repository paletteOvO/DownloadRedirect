package net.manhong2112.downloadredirect

import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import net.manhong2112.downloadredirect.DLApi.DLApi
import org.jetbrains.anko.*
import java.util.*


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
   @SuppressLint("NewApi")
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
      val Pref = ConfigDAO(ctx.getSharedPreferences("pref", 0))
      val af = Pref.AppFilter
      val lf = Pref.LinkFilter

      val ColumnHeight = dip(48)
      val SubTitleHeight = dip(36)

      if (Pref.getExistingDownloader(ctx).isEmpty()) {
         toast(R.string.toast_no_supported_downloader)
      }
      if (Pref.FirstRun) {
         Pref.FirstRun = false
         alert(R.string.first_run_message, R.string.first_run) {
            positiveButton(R.string.button_ok) {}
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
               onLongClick {
                  alert(R.string.debug_logging_help, R.string.switch_debug_logging) {
                     positiveButton(R.string.button_ok) {}
                  }.show()
                  true
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
               onLongClick {
                  alert(R.string.debug_experiment_help, R.string.switch_debug_experiment) {
                     positiveButton(R.string.button_ok) {}
                  }.show()
                  true
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
               onLongClick {
                  alert(R.string.pref_hide_icon_help, R.string.switch_hide_app_icon) {
                     positiveButton(R.string.button_ok) {}
                  }.show()
                  true
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
               onLongClick {
                  alert(R.string.pref_hide_ignore_system_app_help, R.string.switch_ignore_system_app) {
                     positiveButton(R.string.button_ok) {}
                  }.show()
                  true
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
                  onLongClick {
                     alert(R.string.pref_using_downloader_help, R.string.selector_downloader) {
                        positiveButton(R.string.button_ok) {}
                     }.show()
                     true
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
                  alert {
                     customView {
                        verticalLayout {
                           title(R.string.selector_whitelist)
                           val choiceList = arrayListOf(
                                   ctx.getString(R.string.filter_link),
                                   ctx.getString(R.string.filter_app))
                           val aa = CheckableListAdapter(choiceList, ctx) {
                              adapter, view, pos, isChecked ->
                              when (pos) {
                                 0 -> Pref.UsingWhiteList_Link = isChecked
                                 1 -> Pref.UsingWhiteList_App = isChecked
                              }
                           }
                           aa.isSelected[0] = Pref.UsingWhiteList_Link
                           aa.isSelected[1] = Pref.UsingWhiteList_App
                           listView {
                              divider = null
                              isVerticalScrollBarEnabled = false
                              lparams {
                                 padding = dip(4)
                                 height = 0
                                 weight = 1f
                                 width = matchParent
                              }
                              adapter = aa
                           }
                        }
                     }
                  }.show()
               }
               onLongClick {
                  alert(R.string.filter_whitelist_help, R.string.switch_white_list) {
                     positiveButton(R.string.button_ok) {}
                  }.show()
                  true
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
                  alert {
                     customView {
                        verticalLayout {
                           lparams {
                              height = matchParent
                              verticalPadding = dip(16)
                           }
                           val x = ArrayList<String>(lf)
                           val aa = ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, x)
                           title(R.string.list_filter_link)
                           listView {
                              divider = null
                              isVerticalScrollBarEnabled = false
                              lparams {
                                 height = 0
                                 weight = 1f
                                 width = matchParent
                              }
                              adapter = aa
                              onItemClickListener =
                                      AdapterView.OnItemClickListener {
                                         parent, view, position, id ->
                                         alert("Are you sure to blabla..") {
                                            positiveButton(R.string.button_confirm) {
                                               val item = aa.getItem(position)
                                               toast(ctx.getString(R.string.toast_removed, item[0]))
                                               lf.remove(item)
                                               Pref.updateLinkFilter()
                                               x.remove(item)
                                               aa.notifyDataSetChanged()
                                            }
                                            negativeButton(R.string.button_cancel)
                                         }.show()
                                      }
                           }
                           linearLayout {
                              lparams {
                                 width = matchParent
                                 horizontalMargin = dip(4)
                                 gravity = Gravity.CENTER
                              }
                              val link = editText {
                                 maxLines = 1
                                 inputType = android.text.InputType.TYPE_CLASS_TEXT
                                 lparams {
                                    weight = 1f
                                 }
                                 hint = ctx.getString(R.string.label_regex)
                              }
                              button {
                                 text = ctx.getString(R.string.button_add)
                                 onClick {
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
                                          x.add(d)
                                          x.sort()
                                          aa.notifyDataSetChanged()
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }

                  }.show().dialog!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
               }
               onLongClick {
                  alert(R.string.filter_link_help, R.string.filter_link) {
                     positiveButton(R.string.button_ok) {}
                  }.show()
                  true
               }
            }

            with(CLabel(ctx, Const.id.App_Filter, R.string.filter_app)) {
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  alignParentLeft()
                  below(Const.id.Link_Filter)
               }
               onClick {
                  alert {
                     customView {
                        verticalLayout {
                           lparams {
                              height = matchParent
                              verticalPadding = dip(16)
                           }
                           val appList = arrayListOf<String>()

                           val toBeRemove = mutableSetOf<String>()
                           af.forEach {
                              if (Main.isPackageInstalled(it, ctx.packageManager)) {
                                 val appInfo = ctx.packageManager.getApplicationInfo(it, 0)
                                 appList.add(ctx.packageManager.getApplicationLabel(appInfo).toString() + "\n " + it)
                              } else {
                                 toBeRemove.add(it)
                              }
                           }
                           toBeRemove.forEach {
                              af.remove(it)
                           }
                           Pref.updateAppFilter()

                           loop@ for (l in ctx.packageManager.getInstalledPackages(0)) {
                              when (true) {
                                 Pref.IgnoreSystemApp &&
                                         ((l.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1) ->
                                    continue@loop
                                 af.contains(l.packageName) ->
                                    continue@loop
                                 else ->
                                    appList.add(
                                            ctx.packageManager.getApplicationLabel(l.applicationInfo).toString() + "\n " + l.packageName)
                              }
                           }

                           val cla = CheckableListAdapter(appList, ctx) {
                              adapter, view, pos, isChecked ->
                              val item = adapter.getItem(pos)!!.split("\n ")
                              if (item[1] in af) {
                                 Main.log("Removed '${item[0]} | ${item[1]}' from filter", Pref.Debug)
                                 toast(ctx.getString(R.string.toast_removed, item[0]))
                                 af.remove(item[1])
                              } else {
                                 Main.log("Added '${item[0]} | ${item[1]}' to filter", Pref.Debug)
                                 toast(ctx.getString(R.string.toast_added, item[0]))
                                 af.add(item[1])
                              }
                              Pref.updateAppFilter()
                           }
                           for (i in 0..af.size - 1) {
                              cla.isSelected[i] = true
                           }
                           title(R.string.list_filter_app)
                           listView {
                              divider = null
                              isVerticalScrollBarEnabled = false
                              lparams {
                                 height = 0
                                 weight = 1f
                                 width = matchParent
                              }
                              adapter = cla
                           }
                           editText {
                              maxLines = 1
                              inputType = android.text.InputType.TYPE_CLASS_TEXT
                              lparams {
                                 width = matchParent
                                 horizontalMargin = dip(4)
                              }
                              hint = ctx.getString(R.string.label_search)
                              addTextChangedListener(object : TextWatcher {
                                 override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                 }

                                 override fun afterTextChanged(p0: Editable?) {
                                 }

                                 override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                                    cla.filter.filter(cs)
                                 }
                              })
                           }
                        }
                     }
                  }.show().dialog!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
               }
               onLongClick {
                  alert(R.string.filter_app_help, R.string.filter_app) {
                     positiveButton(R.string.button_ok) {}
                  }.show()
                  true
               }
            }

            // About
            CSubTitle(ctx, Const.id.About_Label, R.string.label_about).lparams {
               width = matchParent
               height = SubTitleHeight
               below(Const.id.App_Filter)
            }

            val ver = CLabel(ctx, Const.id.About_Version, "${ctx.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}")
                    .lparams {
                       height = ColumnHeight
                       width = matchParent
                       below(Const.id.About_Label)
                    }
            ver.textColor = getColor(ctx, R.color.label_about_text)
            ver.onLongClick {
               toast("Why you think there is any explanation?")
               true
            }
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