package net.manhong2112.downloadredirect

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.ViewManager
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.TextView
import net.manhong2112.downloadredirect.DLApi.DownloadConfig
import org.jetbrains.anko.*


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
      fun log(str: String, debug: Boolean = true) {
         if (debug) {
            Log.i("Xposed", "DownloadRedirect -> $str")
         }
      }

      fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
         return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
         } catch (e: PackageManager.NameNotFoundException) {
            false
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
   //TODO Undo Bar

   @SuppressLint("NewApi")
   fun getColor(ctx: Context, id: Int): Int {
      if (Const.VER_GE_MARSHMALLOW) {
         return ctx.getColor(id)
      } else {
         @Suppress("deprecation")
         return ctx.resources.getColor(id)
      }
   }

   fun _RelativeLayout.label(ctx: Context, viewId: Int, _text: String, init: TextView.() -> Unit = {}) = textView {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
      gravity = Gravity.CENTER_VERTICAL
      textColor = getColor(ctx, R.color.label_text)
      init()
   }

   fun _RelativeLayout.label(ctx: Context, viewId: Int, textResId: Int, init: TextView.() -> Unit = {}) =
         label(ctx, viewId, resources.getString(textResId), init)

   @SuppressLint("NewApi")
   fun _RelativeLayout.subtitle(ctx: Context, viewId: Int, _text: String, init: TextView.() -> Unit = {}) = textView {
      if (Const.VER_GE_LOLLIPOP) {
         elevation = dip(2).toFloat()
      }
      gravity = Gravity.CENTER_VERTICAL
      id = viewId
      text = _text
      textColor = getColor(ctx, R.color.subtitle_text)
      backgroundColor = getColor(ctx, R.color.subtitle_bg)
      setPadding(dip(12), 0, 0, 0)
      init()
   }

   fun _RelativeLayout.subtitle(ctx: Context, viewId: Int, textResId: Int, init: TextView.() -> Unit = {}) =
         subtitle(ctx, viewId, resources.getString(textResId), init)

   fun _RelativeLayout.prefSwitch(viewId: Int, _text: String, init: Switch.() -> Unit = {}) = switch {
      id = viewId
      text = _text
      setPadding(dip(16), 0, dip(16), 0)
      init()
   }

   fun _RelativeLayout.prefSwitch(viewId: Int, textResId: Int, init: Switch.() -> Unit = {}) = prefSwitch(viewId, resources.getString(textResId), init)

   fun AnkoContext<*>.showAlert(message: Int, title: Int?, dsl: AlertDialogBuilder.() -> Unit = {}): AlertDialogBuilder {
      return alert(message, title) {
         dsl()
      }.show()
   }

   inline fun AnkoContext<*>.showAlert(crossinline dsl: AlertDialogBuilder.() -> Unit): AlertDialogBuilder {
      return alert {
         dsl()
      }.show()
   }

   inline fun AnkoContext<*>.showCustomAlert(crossinline dsl: AlertDialogBuilder.() -> ViewManager.() -> Unit): AlertDialogBuilder {
      return alert {
         customView {
            dsl()()
         }
      }.show()
   }

   fun AnkoContext<*>.downloaderConfigEditDialog(init: DownloadConfig? = null, callback: (DownloadConfig) -> Unit): AlertDialogBuilder {
      var g: (() -> Unit)? = null
      val k = showCustomAlert {{
            verticalLayout {
               padding = dip(16)
               val name = editText {
                  text = SpannableStringBuilder(init?.name ?: "")
                  hint = "name"
               }
               val packageName = editText {
                  text = SpannableStringBuilder(init?.packageName ?: "")
                  hint = "package name"
               }
               val intent = editText {
                  hint = "intent"
                  text = SpannableStringBuilder(init?.intent ?: "")
               }
               val cookies = editText {
                  hint = "Cookies (optional)"
                  text = SpannableStringBuilder(init?.headers?.firstOrNull { (name, _) -> name == "Cookie" }?.second ?: "")
               }
               val referer = editText {
                  hint = "Referer (optional)"
                  text = SpannableStringBuilder(init?.headers?.firstOrNull { (name, _) -> name == "Referer" }?.second ?: "")
               }
               positiveButton(android.R.string.ok, {})
               g = {
                  if (name.text.isEmpty() or packageName.text.isEmpty() or intent.text.isEmpty()) {
                     toast("Please fill in name, package name and intent ")
                  } else {
                     toast("${name.text}, ${packageName.text}, ${intent.text}, ${cookies.text}, ${referer.text}")
                     callback(DownloadConfig(
                           name.text.toString(),
                           packageName.text.toString(),
                           intent.text.toString(),
                           listOf("Cookie" to cookies.text.toString(),
                                 "Referer" to referer.text.toString())
                     ))
                     dismiss()
                  }
               }
            }
         }
      }
      k.dialog!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
      k.dialog!!.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { g!!() }
      return k
   }

   override fun createView(ui: AnkoContext<Main>) = with(ui) {
      val Pref = ConfigDAO.getPref(ctx)
      val DEBUG = Pref.Debug
      val af = Pref.AppFilter
      val lf = Pref.LinkFilter
      val ColumnHeight = dip(48)
      val SubTitleHeight = dip(36)

      if (Pref.FirstRun) {
         Pref.FirstRun = false
         showAlert(R.string.first_run_message, R.string.first_run) {
            positiveButton(android.R.string.ok) {}
         }
      }


      scrollView {
         relativeLayout {
            lparams {
               width = matchParent
            }
            id = Const.id.Pref_Page
            // Debug
            subtitle(ctx, Const.id.Debug_Label, R.string.label_debug)
                  .lparams {
                     width = matchParent
                     height = SubTitleHeight
                  }

            prefSwitch(Const.id.Debug_Logging_Switch, R.string.switch_debug_logging) {
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

            prefSwitch(Const.id.Debug_Experiment_Switch, R.string.switch_debug_experiment) {
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
            subtitle(ctx, Const.id.Pref_Label, R.string.label_preferences).lparams {
               below(Const.id.Debug_Experiment_Switch)
               width = matchParent
               height = SubTitleHeight
            }

            prefSwitch(Const.id.Pref_HideIcon_Switch, R.string.switch_hide_app_icon) {
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


            prefSwitch(Const.id.Pref_Ignore_System_App, R.string.switch_ignore_system_app) {
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


            prefSwitch(Const.id.Pref_NotSpecifyDownloader_Switch, R.string.switch_not_specify_downloader) {
               isChecked = Pref.NotSpecifyDownloader
               onClick {
                  Pref.NotSpecifyDownloader = (it as Switch).isChecked
               }
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  below(Const.id.Pref_Ignore_System_App)
               }
            }

            val selectedDownloader = Pref.SelectedDownloader
            val b = with(label(ctx, Const.id.Pref_Using_Downloader, selectedDownloader.name)) {
               lparams {
                  rightMargin = dip(16)
                  height = ColumnHeight
                  below(Const.id.Pref_NotSpecifyDownloader_Switch)
                  alignParentRight()
               }
            }

            with(label(ctx, Const.id.Pref_Downloader_Selector, R.string.list_change_downloader)) {
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  alignParentLeft()
                  below(Const.id.Pref_NotSpecifyDownloader_Switch)
               }
               onClick {
                  showCustomAlert {
                     {
                        verticalLayout {
                           lparams {
                              height = matchParent
                              verticalPadding = dip(16)
                           }
                           title(R.string.selector_downloader)
                           val j = listView {
                              divider = null
                              isVerticalScrollBarEnabled = false
                              lparams {
                                 height = 0
                                 weight = 1f
                                 width = matchParent
                              }
                              adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, Pref.DownloadConfigs.keys.sorted())
                              onItemClick { adapterView, view, i, id ->
                                 adapterView!!
                                 val downloader = Pref.DownloadConfigs[adapterView.getItemAtPosition(i) as String]!!
                                 toast("Selected ${downloader.name}")
                                 b.text = downloader.name
                                 Pref.SelectedDownloader = downloader
                                 dismiss()
                              }

                              onItemLongClick { adapterView, view, i, id ->
                                 adapterView!!
                                 selector("", listOf("Edit", "Delete")) { index ->
                                    val name = adapterView.getItemAtPosition(i) as String
                                    when (index) {
                                       0 ->
                                          downloaderConfigEditDialog(Pref.DownloadConfigs[name]) { config ->
                                             Pref.DownloadConfigs.remove(name)
                                             Pref.DownloadConfigs[config.name] = config
                                          }
                                       1 ->
                                          Pref.DownloadConfigs.remove(name)
                                    }
                                    Pref.updateDownloadConfigs()
                                    adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, Pref.DownloadConfigs.keys.sorted())
                                    (adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                 }
                                 true
                              }
                           }
                           button {
                              text = "Add"
                              onClick {
                                 downloaderConfigEditDialog { config ->
                                    Pref.DownloadConfigs[config.name] = config
                                    Pref.updateDownloadConfigs()
                                    j.adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, Pref.DownloadConfigs.keys.sorted())
                                    (j.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            // Filter
            subtitle(ctx, Const.id.Filter_Label, R.string.label_filter).lparams {
               width = matchParent
               height = SubTitleHeight
               below(Const.id.Pref_Using_Downloader)
            }

            label(ctx, Const.id.Pref_Use_White_List, R.string.switch_white_list) {
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  below(Const.id.Filter_Label)
               }
               onClick {
                  showCustomAlert {{
                        verticalLayout {
                           title(R.string.selector_whitelist)
                           val choiceList = arrayListOf(
                                 ctx.getString(R.string.filter_link),
                                 ctx.getString(R.string.filter_app))
                           val aa = CheckableListAdapter(choiceList, ctx) { adapter, view, pos, isChecked ->
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
                  }
               }
            }

            label(ctx, Const.id.Link_Filter, R.string.filter_link) {
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  alignParentLeft()
                  below(Const.id.Pref_Use_White_List)
               }
               onClick {
                  showCustomAlert {
                     {
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
                              onItemClick { parent, view, position, id ->
                                 alert("Are you sure to remove") {
                                    positiveButton(android.R.string.yes) {
                                       val item = aa.getItem(position)
                                       toast(ctx.getString(R.string.toast_removed, item[0]))
                                       lf.remove(item)
                                       Pref.updateLinkFilter()
                                       x.remove(item)
                                       aa.notifyDataSetChanged()
                                    }
                                    negativeButton(android.R.string.no)
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
                                          Main.log("Added \"$d\" to filter", DEBUG)
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

                  }.dialog!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
               }
            }

            label(ctx, Const.id.App_Filter, R.string.filter_app) {
               lparams {
                  width = matchParent
                  height = ColumnHeight
                  alignParentLeft()
                  below(Const.id.Link_Filter)
               }
               onClick {
                  showCustomAlert {{
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

                           val cla = CheckableListAdapter(appList, ctx) { adapter, view, pos, isChecked ->
                              val item = adapter.getItem(pos)!!.split("\n ")
                              if (item[1] in af) {
                                 Main.log("Removed '${item[0]} | ${item[1]}' from filter", DEBUG)
                                 toast(ctx.getString(R.string.toast_removed, item[0]))
                                 af.remove(item[1])
                              } else {
                                 Main.log("Added '${item[0]} | ${item[1]}' to filter", DEBUG)
                                 toast(ctx.getString(R.string.toast_added, item[0]))
                                 af.add(item[1])
                              }
                              Pref.updateAppFilter()
                           }
                           for (i in 0 until af.size) {
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
                  }.dialog!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
               }
            }

            // About
            subtitle(ctx, Const.id.About_Label, R.string.label_about).lparams {
               width = matchParent
               height = SubTitleHeight
               below(Const.id.App_Filter)
            }

            val ver = label(ctx, Const.id.About_Version, "${ctx.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}")
                  .lparams {
                     height = ColumnHeight
                     width = matchParent
                     below(Const.id.About_Label)
                  }
            ver.textColor = getColor(ctx, R.color.label_about_text)
            val s = String(android.util.Base64.decode(ctx.getString(R.string.Info), 0)).split("|")
            var i = 1
            label(ctx, Const.id.About_Author, String(Base64.decode(s[--i], 0)))
                  .lparams {
                     height = ColumnHeight
                     width = matchParent
                     below(Const.id.About_Version)
                  }.textColor = getColor(ctx, R.color.label_about_text)
            label(ctx, Const.id.About_Email, String(Base64.decode(s[(i++).plus(++i)], 0)))
                  .lparams {
                     height = ColumnHeight
                     width = matchParent
                     below(Const.id.About_Author)
                  }.textColor = getColor(ctx, R.color.label_about_text)
            label(ctx, Const.id.About_Github, String(Base64.decode(s[--i], 0)))
                  .lparams {
                     height = ColumnHeight
                     width = matchParent
                     below(Const.id.About_Email)
                  }.textColor = getColor(ctx, R.color.label_about_text)

         }
      }


   }

}