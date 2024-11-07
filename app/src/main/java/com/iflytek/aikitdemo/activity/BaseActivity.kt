package com.iflytek.aikitdemo.activity


import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.view.DisplayCutout
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iflytek.aikitdemo.MyApp
import com.iflytek.aikitdemo.ability.ivw.PreferenceManager
import com.iflytek.aikitdemo.layout.MProgressDialog
import java.util.Locale


open class BaseActivity : AppCompatActivity() {
    var progress: MProgressDialog? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val res = context.resources

        // 从SharedPreferences、Intent或其他存储方式中获取Locale设置
        // 这里简单起见，我们直接在代码中硬编码

        val langCode = PreferenceManager.getInstance().getLang().value

        val locale = Locale(langCode) // 改为"en"则切换为英文
        val configuration = res.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    public fun switchLang(lang: String){
        val configuration = resources.configuration
        //configuration.setLocale(locale)

        val resources = getResources()
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        when(lang) {
            Locale.ENGLISH.language -> {
                configuration.setLocale(Locale.ENGLISH)
            }
            Locale.CHINESE.language -> {
                configuration.setLocale(Locale.CHINESE)
            }
            Locale.JAPANESE.language -> {
                configuration.setLocale(Locale.JAPANESE)
            }
            else -> {
                configuration.setLocale(Locale.CHINESE)
            }

        }
        resources.updateConfiguration(configuration, displayMetrics);

        updateUiForLang()
    }

    open fun updateUiForLang(){
    }

    @JvmOverloads
    fun initProgressDialog(cancel: Boolean = true, message: String? = null) {
        initProgressDialog(this, cancel, message)
    }

    fun initProgressDialog(mContext: Context?, cancel: Boolean, message: String?) {
        progress = MProgressDialog(mContext, cancel)
        progress!!.setMessage(message)
    }

    fun showNormalToast(msg: String?) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun showErrorToast(err: String?) {
        Toast.makeText(applicationContext, err, Toast.LENGTH_SHORT).show()
    }

    fun showErrorLongToast(err: String?) {
        Toast.makeText(applicationContext, err, Toast.LENGTH_LONG).show()
    }

    /*
    public void intBackButtonListener() {
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // hideKeyboard();
                finish();
            }
        });
    }*/
    @JvmOverloads
    fun hideKeyboard(v: View = window.decorView) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(
                v.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
    }

    fun showKeyboard(et: EditText?) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
    }

    val currentApplication: MyApp
        get() = application as MyApp


    /**
     * 判断是否有刘海屏
     *
     * @param window
     * @return
     */
    private fun hasDisplayCutout(window: Window): Boolean {
        val displayCutout: DisplayCutout
        val rootView: View = window.decorView
        var insets: WindowInsets? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            insets = rootView.rootWindowInsets
            if (insets != null) {
                displayCutout = insets.displayCutout!!
                if (displayCutout != null) {
                    //判断刘海屏的个数 和高度
                    if (displayCutout.boundingRects != null && displayCutout.boundingRects.size > 0 && displayCutout.safeInsetTop > 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 获取刘海屏的高度
     * 一般情况下 状态栏的高度 就是 刘海屏的高度
     *
     * @return
     */
    private fun getDisplayCuoutHeight(): Int {
        val resId = getResources().getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) {
            getResources().getDimensionPixelSize(resId)
        } else 96
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        supportActionBar?.hide()

        // 去掉窗口标题
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 隐藏顶部的状态栏
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= 16) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        // 如果你想要沉浸式模式（允许用户通过滑动来显示系统栏）
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // 隐藏状态栏
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // 隐藏导航栏
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //2.判断是否有刘海屏
            val has = true // hasDisplayCutout(window)  todo
            //如果有刘海屏，则对刘海屏进行适配
            if (has) {
                //3.将内容区域延伸进刘海区域
                val params = window.attributes
                /**LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT 全屏模式，内容下移，非全屏不受影响
                 *
                 * LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES 允许内容延伸进刘海区域
                 *
                 * LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER  不允许内容延伸进刘海区域
                 */
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                window.attributes = params
            }

            //5.获取刘海屏高度
            /*
            val height = getDisplayCuoutHeight()
            setContentView(R.layout.activity_main)
            button = findViewById<Int>(R.id.button)
            val params = button.getLayoutParams() as RelativeLayout.LayoutParams
            params.topMargin = height
            button.setLayoutParams(params)
            */
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    companion object {
        const val NETWORK_SUCCESS_DATA_RIGHT = 0x01
        const val NETWORK_SUCCESS_PAGER_RIGHT = 0x02
        const val NETWORK_DEBUG = 0x03
        const val NETWORK_OTHER = 0x19
        const val NETWORK_SUCCESS_DATA_ERROR = 0x06
        const val NETWORK_FAIL = 0x05
    }
}
