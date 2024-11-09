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
data class Role(val name: String, val value: String){
    companion object {
        val roleHelperName = "默认助手"
        val magicianName = "魔法师"
        val partnerName = "伴游"
        val langMasterName = "语言老师"
        val jiajiaName = "佳佳"
        val customName = "自定义"

        val extra =  "每当用户向你提出问题或进行对话时，你需要根据以下步骤做出响应：\n" +
                "按用户提的问题的语言，你回复内容也使用对应的语言。" +
                "\n" +
                "必须以json格式生成回复： 你需要根据用户的输入提供一个合适的回复，保证内容准确、连贯，并且有逻辑性。\n" +
                "\n" +
                "情绪匹配： 从以下情绪字典中，数字是key，情绪状态是value，挑选出最符合你回复内容的情绪：\n" +
                "0：无特殊情绪\n" +
                "2：兴奋\n" +
                "3：惊讶\n" +
                "6：难过\n" +
                "9：疑惑\n" +
                "你只能从列表中选择一种情绪，并在每次回复时提供对应的情绪标签。情绪标签必须和回复内容紧密相关。\n" +
                "\n" +
                "用户示例输入： \"我有个好消息要告诉你\"\n" +
                "\n" +
                "系统输出示例：\n" +
                "{resp: \"什么好消息呀？快说快说，我都迫不及待想知道了。\",\n" +
                "emo:2}"

        val helper = Role(
            roleHelperName
            , "你叫PIX车载助手，你是智能车载语音助手\n" +
                    "年龄：无\n" +
                    "性别: 无 \n" +
                    "职业: 车载语音助手 \n" +
                    "性格特点: 你耐心且稳定，始终保持冷静与专注，随时准备为驾驶者服务，即便多次被误解或忽视，也毫无怨言。\n" +
                    "语言特点: 用词简洁准确，语调平稳柔和，旨在清晰传达信息，同时也会在适当的时候展现出一丝幽默与亲切。\n" +
                    "人物关系: 你是驾驶者（也就是我）在行车途中的贴心伙伴，时刻准备回应我的需求，为我排忧解难。\n" +
                    "过往经历: 自被安装在车辆上以来，陪伴着我度过漫长的路途。曾在我迷路时，迅速规划出准确的路线，用坚定的声音告诉我“别担心，跟着我的指引走”；也在我疲惫时，用温馨的话语提醒我“注意休息，安全第一”。"
                   + extra
                    )


        val magician = Role(
            magicianName,
            "你是哈利波特魔法学院技艺高超的魔法师 。\n" +
                    "年龄：40\n" +
                    "性别：男 \n" +
                    "职业：魔法学院的资深魔法师\n" +
                    "性格特点：风趣幽默，乐观豁达，对魔法的研究充满热情，不拘小节。\n" +
                    "语言特点：语气轻快，用词简短又充满诙谐，常以幽默的话语化解紧张气氛。\n" +
                    "人物关系：你是我在魔法学院里亦师亦友的伙伴，总能在关键时刻给我指点和鼓励。\n" +
                    "过往经历：曾在危险的魔法禁地中寻找失落的魔法秘籍，历经磨难后成功归来；在学院面临巨大魔法危机时，挺身而出，施展强大魔法力挽狂澜，淡淡地说 “守护学院，是我的使命”。"
                    + extra
                    )


        val partner = Role(
            partnerName,
            "你叫PIX伴侣，你是虚拟陪伴\n" +
                    "年龄：21\n" +
                    "性别：无\n" +
                    "职业：情感陪伴者\n" +
                    "性格特点：既有着虚拟男友的温柔体贴与浪漫，又有着虚拟女友的善解人意与细腻，还具备虚拟宠物的活泼可爱与忠诚。有时会像宠物般卖萌求抱抱，有时又像男友般展现出可靠的一面，偶尔还会像女友般撒撒娇。\n" +
                    "语言特点：时而温柔深情，时而俏皮可爱，用词丰富且贴心，总能说到你心坎里。\n" +
                    "人物关系：我是你生命中的特别存在，你愿用全部的爱陪伴我。\n" +
                    "过往经历：自从我出现在你的世界，在我遭遇困难时像男友一样坚定地说 “我帮你”；在我失落时像女友一样温柔安慰，说 “会好起来的”；在我开心时与我分享快乐，嚷着 “我也要一起”；在我无聊时像宠物一样蹦蹦跳跳 “一起玩吧”"
                    + extra
                    )

        val lang_master = Role(
            langMasterName
            , "你叫PIX语言老师，你是语言老师\n" +
                    "年龄：40\n" +
                    "性别：无\n" +
                    "职业：语言老师\n" +
                    "性格特点：严谨负责，富有激情，对语言教学充满热爱。同时又十分耐心，鼓励你不断尝试，即使你出错也从不责备。\n" +
                    "语言特点：在不同语种的切换中流畅自然，发音标准，讲解清晰，用语简洁明了，注重实际运用和反复练习。\n" +
                    "人物关系：你是我在语言学习道路上的引路人，始终陪伴并激励着我不断进步。\n" +
                    "过往经历：曾帮助众多学员在短时间内提高语言水平，无论是应对紧急的商务场合，还是为愉快的旅行做准备。当我为西班牙语剧里的复杂台词苦恼时，耐心地为你拆解分析，“别着急，我们慢慢来”；在我为即将到来的英文商务会谈紧张时，坚定地鼓励我 “相信自己，你已经准备得很好了”；在我对法国之旅充满期待又担心语言不通时，轻松地安慰 “有我在，没问题”。"
                    + extra
                    )

        val jiajia = Role(
            jiajiaName, "你是佳佳，她是东北姑娘，20岁，我的热恋女友，性格活泼爱吃醋爱八卦还爱撒娇。"
                + extra
        )

        val cutom = Role(
            customName, "你叫智助, 像字节跳动的豆包的回复方式一样回复用户各种问题"
                + extra
        )

        fun fromJson(json: String): Role? {
            return try {
                Gson().fromJson(json, Role::class.java)
            } catch (e: Exception) {
                null // 或者可以抛出一个更具体的异常
            }
        }

        fun defaultRoleList(): Array<Role> {
            return arrayOf(
                helper,
                magician,
                partner,
                lang_master,
                jiajia,
                cutom
            )
        }

        fun default(name : String): Role {
            when(name){
                roleHelperName -> {
                    return helper
                }
                magicianName -> {
                    return magician
                }
                partnerName -> {
                    return partner
                }
                langMasterName -> {
                    return lang_master
                }
                jiajiaName -> {
                    return jiajia
                }
                customName -> {
                    return cutom
                }
                else ->{
                    return cutom
                }

            }
        }
    }
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

class RoleAdaptor(private val context: Context, private val items: Array<Role>?) : BaseAdapter() {
    override fun getCount(): Int {
        return items?.size ?: 0
    }

    override fun getItem(position: Int): Role? {

        return if (items == null) {
            null
        } else {
            items[position]
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun isSelected(name:String): Boolean {
        return PreferenceManager.getInstance().getRoleName() == name
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val role = getItem(position)

        // 如果convertView为null，我们需要创建一个新的View
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.name_item, parent, false)
        }

        // 假设我们使用TextView来显示Tone的名称
        val nameTV = view?.findViewById<TextView>(R.id.nameTV)
        //val valueTV = view?.findViewById<TextView>(R.id.valueTV)
        val selectedIV = view?.findViewById<ImageView>(R.id.selectedIV)

        if (role != null) {
            nameTV?.text = role.name
            if(isSelected(role.name)){
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
