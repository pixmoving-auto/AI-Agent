package com.example.robobus_voicebot.audio
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.robobus_voicebot.manager.PreferenceManager
import com.google.gson.Gson
import com.iflytek.aikitdemo.R

data class Lang(val name: String, val value: String) {
    // 静态方法，用于从JSON字符串构建Lang对象
    companion object {
        fun fromJson(json: String): Lang? {
            return try {
                Gson().fromJson(json, Lang::class.java)
            } catch (e: Exception) {
                null // 或者可以抛出一个更具体的异常
            }
        }

        fun default(): Lang {
            return Lang("中文", "zh")
        }
    }

    // 使用Gson来重写toString方法，返回JSON描述
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

class LangAdaptor(private val context: Context, private val items: Array<Lang>?) : BaseAdapter() {
    override fun getCount(): Int {
        return items?.size ?: 0
    }

    override fun getItem(position: Int): Lang? {
        return if (items == null) {
            null
        } else {
            items[position]
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun isSelected(value:String): Boolean {
        var lang = PreferenceManager.getInstance().getLang();

        if (lang != null) {
            return lang.name == value
        }

        return false
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val lang = getItem(position)

        // 如果convertView为null，我们需要创建一个新的View
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.name_item, parent, false)
        }

        // 假设我们使用TextView来显示Tone的名称
        val nameTV = view?.findViewById<TextView>(R.id.nameTV)
        //val valueTV = view?.findViewById<TextView>(R.id.valueTV)
        val selectedIV = view?.findViewById<ImageView>(R.id.selectedIV)

        if (lang != null) {
            nameTV?.text = lang.name
            if(isSelected(lang.name)){
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
