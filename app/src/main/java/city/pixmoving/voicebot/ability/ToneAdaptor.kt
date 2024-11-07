package com.example.robobus_voicebot.ability
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.robobus_voicebot.manager.PreferenceManager
import com.iflytek.aikitdemo.R
data class Tone(val name: String, val value: String, val isSelected: Boolean)

class ToneAdaptor(private val context: Context, private val items: Array<Tone>?) : BaseAdapter() {
    override fun getCount(): Int {
        return items?.size ?: 0
    }

    override fun getItem(position: Int): Tone? {
        return if (items == null) {
            null
        } else {
            items[position]
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun isSelected(value:String): Boolean {
        return PreferenceManager.getInstance().getTone() == value
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val tone = getItem(position)

        // 如果convertView为null，我们需要创建一个新的View
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.name_item, parent, false)
        }

        // 假设我们使用TextView来显示Tone的名称
        val nameTV = view?.findViewById<TextView>(R.id.nameTV)
        //val valueTV = view?.findViewById<TextView>(R.id.valueTV)
        val selectedIV = view?.findViewById<ImageView>(R.id.selectedIV)

        if (tone != null) {
            nameTV?.text = tone.name
            if(isSelected(tone.value)){
                if (selectedIV != null) {
                    selectedIV.visibility = View.VISIBLE
                }
            }else{
                if (selectedIV != null) {
                    selectedIV.visibility = View.INVISIBLE
                }
            }
        }

        return view!!
    }
}
