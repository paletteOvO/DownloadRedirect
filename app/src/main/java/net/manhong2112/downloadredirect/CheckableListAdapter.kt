package net.manhong2112.downloadredirect

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding


/**
 * Created by manhong2112 on 31/12/2016.
 *
 */
class CheckableListAdapter(list: List<String>,
                           private val ctx: Context,
                           private val callback: (CheckableListAdapter, CheckBox, Int, Boolean) -> Unit)
   : ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, list) {
   val isSelected = BooleanArray(list.size)
   override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
      val cv = (convertView ?: CheckBox(ctx)) as CheckBox
      cv.isChecked = isSelected[pos]
      cv.text = getItem(pos)
      cv.minHeight = ctx.dip(52)
      cv.padding = ctx.dip(8)
      cv.setOnClickListener {
         it as CheckBox
         callback(this, it, pos, it.isChecked)
      }
      return cv
   }
}

