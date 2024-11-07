package com.iflytek.aikitdemo.ability.ivw
import android.content.Context
import android.content.SharedPreferences
import com.iflytek.aikitdemo.MyApp
class PreferenceManager private constructor(context: Context) {
    // 定义SharedPreferences文件名
    private val PREF_NAME = "MyPreferences"

    // 定义键（key）为单独的变量
    private val KEY_TONE = "tone"
    private val KEY_ROLE_NAME = "role_name"
    private val KEY_ROLE_INFO = "role_info"
    private val KEY_ROLE_PREFIX = "__role__"
    private val KEY_APP_LANG = "app_lang"
    private val KEY_LLM = "llm"

    // 获取SharedPreferences实例
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(MyApp.CONTEXT).also { instance = it }
            }
        }
    }

    // 保存配置
    fun saveTone(tone: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_TONE, tone)
            apply()
        }
    }

    fun saveRoleName(name: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_ROLE_NAME, name)
            apply()
        }
    }

    fun saveLang(lang: Lang) {
        with(sharedPreferences.edit()) {
            putString(KEY_APP_LANG, lang.toString())
            apply()
        }
    }

    fun saveRole(role: Role) {
        if(role.name.trim() == ""){
            return
        }

        with(sharedPreferences.edit()) {
            putString(KEY_ROLE_PREFIX + role.name, role.toString())
            apply()
        }
    }

    fun saveLlm(llm: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_LLM, llm)
            apply()
        }
    }

    // 读取配置
    fun getTone(): String? {
        return sharedPreferences.getString(KEY_TONE, null)
            ?: return "zh_female_shuangkuaisisi_moon_bigtts"
    }


    fun getRoleName(): String? {
        return sharedPreferences.getString(KEY_ROLE_NAME, null)
            ?: return "自定义"
    }

    fun getRole(name: String): Role{
        val role = sharedPreferences.getString(KEY_ROLE_PREFIX + name, null)?.let{ Role.fromJson(it) }

        return role ?: Role.default(name)
    }

    fun getLang(): Lang{
        val lang = sharedPreferences.getString(KEY_APP_LANG, null)?.let { Lang.fromJson(it) }

        return lang ?: Lang.default()
    }

    fun getLlm(): String? {
        return sharedPreferences.getString(KEY_LLM, null)
            ?: return "doubao"
    }
}