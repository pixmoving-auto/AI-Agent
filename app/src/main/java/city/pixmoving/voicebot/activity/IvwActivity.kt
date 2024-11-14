package city.pixmoving.voicebot.activity


import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import city.pixmoving.voicebot.ability.KeywordEngineListener
import city.pixmoving.voicebot.ability.Tone
import city.pixmoving.voicebot.ability.ToneAdaptor
import city.pixmoving.voicebot.ability.TtsServerListener
import city.pixmoving.voicebot.audio.AudioPlayer
import city.pixmoving.voicebot.audio.AudioPlayerListener
import city.pixmoving.voicebot.audio.Lang
import city.pixmoving.voicebot.audio.LangAdaptor
import city.pixmoving.voicebot.audio.MicrophoneStream
import city.pixmoving.voicebot.audio.Role
import city.pixmoving.voicebot.audio.RoleAdaptor
import city.pixmoving.voicebot.manager.KeywordEngineManager
import city.pixmoving.voicebot.manager.PreferenceManager
import city.pixmoving.voicebot.manager.TtsServerManager
import com.bumptech.glide.Glide

import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.iflytek.aikitdemo.R

import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChunk
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole
import com.volcengine.ark.runtime.service.ArkService
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.Consumer
import kotlin.math.log10
import kotlin.system.exitProcess

//0：无特殊情绪
//2：兴奋
//3：惊讶
//6：难过
//9：疑惑
data class RespWithEmo(val resp: String, val emo: Int) {
    // 静态方法，用于从JSON字符串构建Lang对象
    companion object {
        fun fromJson(json: String): RespWithEmo? {
            return try {
                Gson().fromJson(json, RespWithEmo::class.java)
            } catch (e: Exception) {
                null // 或者可以抛出一个更具体的异常
            }
        }
        val NORMAL = 0
        val EXCITING = 2
        val SURPRISED = 3
        val SAD = 6
        val CONFUSED = 9
    }

    // 使用Gson来重写toString方法，返回JSON描述
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

class IvwActivity: BaseActivity(), TtsServerListener, KeywordEngineListener,
    AudioPlayerListener {
    private val TAG = "avbs IvwActivity"
    private val ASR_TIMEOUT = 800 //800
    private val AUTO_DISCONNECT_TIMEOUT = 1000 * 60 * 10

    private lateinit var tvKeyword: AppCompatEditText
    private lateinit var startOrStopBtn: MaterialButton
    private lateinit var btnTest: MaterialButton
    private lateinit var tvAudioRecord: AppCompatTextView
    private lateinit var btnAudioFile: MaterialButton
    private lateinit var tvResult: AppCompatTextView
    private lateinit var hearingTV: TextView
    private lateinit var hearingTitleTV: TextView

    private lateinit var emoLayout: RelativeLayout
    private lateinit var emoIV: ImageView
    private lateinit var emoShowOrHideBtn:Button
    private lateinit var menuLayout: RelativeLayout

    private lateinit var appStopBtn:Button
    private lateinit var llmBtn:Button
    private lateinit var roleBtn:Button
    private lateinit var toneBtn: Button
    private lateinit var promptBtn: Button
    private lateinit var musicBtn:Button
    private lateinit var langBtn: Button

    private lateinit var connStatusTV: TextView

    private lateinit var promptEditLayout: LinearLayout
    private lateinit var promptCloseBtn:Button
    private lateinit var promptSaveBtn:Button
    private lateinit var promptTitleTV:TextView
    private lateinit var promptETV:EditText

    private val bluetoothConnectedIntentFilter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
    private var bluetoothConnectedRegistered = false


    private val handler = Handler()
    private var speechRecoResultTimeoutRunnable: Runnable? = null
    private var speechRecoResult: String = ""

    private var isSynthesizing = false

    private lateinit var llmPopupWindow: ListPopupWindow
    private lateinit var rolePopupWindow: ListPopupWindow
    private lateinit var tonePopupWindow: ListPopupWindow
    private lateinit var promptPopupWindow: ListPopupWindow

    private lateinit var musicMenuPopupWindow: ListPopupWindow

    private var menuIsShowed = true
    private lateinit var respWithEmo: RespWithEmo

    private var mediaPlayer: MediaPlayer? = null


    private fun startRecoResultTimer(result: String) {
        if (speechRecoResultTimeoutRunnable != null) {
            handler.removeCallbacks(speechRecoResultTimeoutRunnable!!)
        }
        speechRecoResultTimeoutRunnable = Runnable {
            updateTvResult("我:$result")
            //sendTextToChatGPT(result + "。你的回答尽量不要超过60个文字，并且最多不要超过120个文字，并且你的回答中不要体现对你的这两点要求。")

            sendTextToChatVolc1(result);
            speechRecoResult = ""
        }
        handler.postDelayed(speechRecoResultTimeoutRunnable!!, ASR_TIMEOUT.toLong())
    }
    private fun stopRecoResultTimer() {
        if (speechRecoResultTimeoutRunnable != null) {
            handler.removeCallbacks(speechRecoResultTimeoutRunnable!!)
            speechRecoResultTimeoutRunnable = null
        }
    }

    private var autoDisconnectRunnable: Runnable? = null
    private fun startAutoDisconnectTimer() {
        if (autoDisconnectRunnable != null) {
            handler.removeCallbacks(autoDisconnectRunnable!!)
        }
        autoDisconnectRunnable = Runnable {
            Log.i(TAG, "timeout, auto disconnect")
            showNormalToast(getString(R.string.auto_disconnect))
            stopRobot()
        }
        handler.postDelayed(autoDisconnectRunnable!!, AUTO_DISCONNECT_TIMEOUT.toLong())
    }

    private fun stopAutoDisconnectTimer() {
        if (autoDisconnectRunnable != null) {
            handler.removeCallbacks(autoDisconnectRunnable!!)
            autoDisconnectRunnable = null
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
            }
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    // Permission is granted
                } else {
                    // Permission is denied
                }
            }
        }

    private fun initView(){
        startOrStopBtn = findViewById(R.id.startOrStopBtn)
        btnTest = findViewById(R.id.btnTest)
        tvAudioRecord = findViewById(R.id.tvAudioRecord)
        btnAudioFile = findViewById(R.id.btnAudioFile)
        tvResult = findViewById(R.id.tvResult)
//        progressThreshold = findViewById(R.id.progressThreshold)
        hearingTV = findViewById(R.id.hearingTV)
        hearingTitleTV = findViewById(R.id.hearingTitleTV)
        emoLayout = findViewById(R.id.emoLayout)

        appStopBtn =findViewById(R.id.appStopBtn)
        llmBtn = findViewById(R.id.lllmBtn)
        roleBtn = findViewById(R.id.roleBtn)
        toneBtn = findViewById(R.id.toneBtn)
        promptBtn = findViewById(R.id.promptBtn)
        musicBtn = findViewById(R.id.musicBtn)
        langBtn = findViewById(R.id.langBtn)
        connStatusTV = findViewById(R.id.connStatusTV)

        promptEditLayout = findViewById(R.id.promptEditLayout)
        promptCloseBtn = findViewById(R.id.promptCloseBtn)
        promptSaveBtn = findViewById(R.id.promptSaveBtn)
        promptTitleTV = findViewById(R.id.promptTitleTV)
        promptETV = findViewById(R.id.promptETV)

        promptBtn.setOnClickListener {
            val roleName = PreferenceManager.getInstance().getRoleName()
            val role = roleName?.let { PreferenceManager.getInstance().getRole(it) }
            val prompt = role!!.value

            promptETV.setText(prompt)
            promptEditLayout.visibility = View.VISIBLE
        }

        promptSaveBtn.setOnClickListener {
            val roleName = PreferenceManager.getInstance().getRoleName()
            val role = Role(roleName!!, promptETV.text.toString())

            PreferenceManager.getInstance().saveRole(role)

            showNormalToast(getString(R.string.saved))
        }

        promptCloseBtn.setOnClickListener {
            promptEditLayout.visibility = View.INVISIBLE
        }

        initTones()
        initRoles()
        initLangs()
        initMusicMenu()

        appStopBtn.setOnClickListener {
            exitProcess(0)
        }

        connStatusTV.setOnClickListener{
            stopRobot()

            startRobot()

            showNormalToast(getString(R.string.reconnect_info))
        }

        btnTest.addOnCheckedChangeListener { button, isChecked ->

            sendTextToChatVolc4("");
        }

        emoIV = findViewById<ImageView>(R.id.emoIV)
        menuLayout = findViewById<RelativeLayout>(R.id.menuLayout)

        emoShowOrHideBtn = findViewById<Button>(R.id.emoShowOrHideBtn)
        emoShowOrHideBtn.setOnClickListener {
            if(isEmoVisible()){
                emoLayout.visibility = View.INVISIBLE
                emoShowOrHideBtn.text = getString(R.string.emo_on)
            }
            else{
                emoLayout.visibility = View.VISIBLE
                emoShowOrHideBtn.text = getString(R.string.emo_off)
            }
        }

        emoIV.setOnClickListener{
            if(menuIsShowed){
                menuLayout.visibility = View.INVISIBLE
                emoShowOrHideBtn.visibility = View.INVISIBLE
                startOrStopBtn.visibility = View.INVISIBLE

            }else{
                menuLayout.visibility = View.VISIBLE
                emoShowOrHideBtn.visibility = View.VISIBLE
                startOrStopBtn.visibility = View.VISIBLE
            }
            menuIsShowed = !menuIsShowed
        }


        initAudioRelatedBtns(true)

        initStartOrStopBtn(false)

        showEmo(Emo.IDLE)
    }

    fun isEmoVisible() : Boolean{
        return emoLayout.isVisible
    }

    val SAMPLE_RATE_IN_HZ = 8000
    val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE_IN_HZ,
        AudioFormat.CHANNEL_IN_DEFAULT,
        AudioFormat.ENCODING_PCM_16BIT
    )
    var mAudioRecord: AudioRecord? = null
    var isGetVoiceRun = false

    fun initTones(){
        // 初始化ListPopupWindow
        tonePopupWindow = ListPopupWindow(this)
        // 设置列表项

        val toneArray = arrayOf(
            Tone("爽快思思/Skye", "zh_female_shuangkuaisisi_moon_bigtts", false),
            Tone("温暖阿虎/Alvin", "zh_male_wennuanahu_moon_bigtts", false),
            Tone("少年梓辛/Brayan", "zh_male_shaonianzixin_moon_bigtts", false),
            Tone("かずね（和音）/Javier or Álvaro", "multi_male_jingqiangkanye_moon_bigtts", false),
            Tone("はるこ（晴子）/Esmeralda", "multi_female_shuangkuaisisi_moon_bigtts", false),
            Tone("あけみ（朱美）", "multi_female_gaolengyujie_moon_bigtts", false),
            Tone("ひろし（広志）/Roberto", "multi_male_wanqudashu_moon_bigtts", false),
            Tone("邻家女孩", "zh_female_linjianvhai_moon_bigtts", false),
            Tone("渊博小叔", "zh_male_yuanboxiaoshu_moon_bigtts", false),
            Tone("阳光青年", "zh_male_yangguangqingnian_moon_bigtts", false),
            Tone("京腔侃爷/Harmony", "zh_male_jingqiangkanye_moon_bigtts", false),
            Tone("湾湾小何", "zh_female_wanwanxiaohe_moon_bigtts", false),
            Tone("湾区大叔", "zh_female_wanqudashu_moon_bigtts", false), // 注意：这里应该是zh_male，但按照列表保持不变
            Tone("呆萌川妹", "zh_female_daimengchuanmei_moon_bigtts", false),
            Tone("广州德哥", "zh_male_guozhoudege_moon_bigtts", false),
            Tone("北京小爷", "zh_male_beijingxiaoye_moon_bigtts", false),
            Tone("浩宇小哥", "zh_male_haoyuxiaoge_moon_bigtts", false),
            Tone("广西远舟", "zh_male_guangxiyuanzhou_moon_bigtts", false),
            Tone("妹坨洁儿", "zh_female_meituojieer_moon_bigtts", false),
            Tone("豫州子轩", "zh_male_yuzhouzixuan_moon_bigtts", false),
            Tone("高冷御姐", "zh_female_gaolengyujie_moon_bigtts", false),
            Tone("傲娇霸总", "zh_male_aojiaobazong_moon_bigtts", false),
            Tone("魅力女友", "zh_female_meilinvyou_moon_bigtts", false),
            Tone("深夜播客", "zh_male_shenyeboke_moon_bigtts", false),
            Tone("柔美女友", "zh_female_sajiaonvyou_moon_bigtts", false),
            Tone("撒娇学妹", "zh_female_yuanqinvyou_moon_bigtts", false)
        )

        val adapter = ToneAdaptor(this, toneArray)

        tonePopupWindow.setAdapter(adapter)

        val widthInDp = 260 // 你想要设置的宽度，单位dp
        val scale: Float = getResources().displayMetrics.density
        val widthInPx = (widthInDp * scale + 0.5f).toInt()
        tonePopupWindow.width = widthInPx
        tonePopupWindow.height = ListPopupWindow.WRAP_CONTENT
        tonePopupWindow.anchorView = toneBtn;
        tonePopupWindow.isModal = true;
        tonePopupWindow.clearListSelection()
        tonePopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            tonePopupWindow.dismiss() // 关闭弹出窗口

            PreferenceManager.getInstance().saveTone(toneArray[position].value)
        }

        toneBtn.setOnClickListener {             // 显示ListPopupWindow
            tonePopupWindow.show()
        }
    }

    fun initMicView() {
        if (isGetVoiceRun) {
            Log.e(TAG, "还在录着呢")
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mAudioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE
        )
        if (mAudioRecord == null) {
            Log.e("sound", "mAudioRecord初始化失败")
        }
        isGetVoiceRun = true
        Thread {
            mAudioRecord!!.startRecording()
            val buffer = ShortArray(BUFFER_SIZE)
            while (isGetVoiceRun) {

                //r是实际读取的数据长度，一般而言r会小于buffersize
                val r = mAudioRecord!!.read(buffer, 0, BUFFER_SIZE)
                var v: Long = 0
                // 将 buffer 内容取出，进行平方和运算
                for (i in buffer.indices) {
                    v += (buffer[i] * buffer[i]).toLong()
                }

                // 平方和除以数据总长度，得到音量大小。
                val mean = v / r.toDouble()
                var volume = 10 * log10(mean)

                val intVolume = volume.toInt()

                runOnUiThread{
                    hearingTV.text = "$intVolume"
                }

                Thread.sleep(100)

                // Log.d(TAG, "分贝值:" + volume);
            }
            mAudioRecord!!.stop()
            mAudioRecord!!.release()
            mAudioRecord = null
        }.start()
    }



    fun initRoles(){
        // 初始化ListPopupWindow
        rolePopupWindow = ListPopupWindow(this)
        // 设置列表项

        val roleArray = Role.defaultRoleList()

        val adapter = RoleAdaptor(this, roleArray)

        rolePopupWindow.setAdapter(adapter)

        val widthInDp = 260 // 你想要设置的宽度，单位dp
        val scale: Float = getResources().displayMetrics.density
        val widthInPx = (widthInDp * scale + 0.5f).toInt()
        rolePopupWindow.width = widthInPx
        rolePopupWindow.height = ListPopupWindow.WRAP_CONTENT
        rolePopupWindow.anchorView = roleBtn;
        rolePopupWindow.isModal = true;
        rolePopupWindow.clearListSelection()
        rolePopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            rolePopupWindow.dismiss() // 关闭弹出窗口
            clearMessages()

            PreferenceManager.getInstance().saveRoleName(roleArray[position].name)
        }

        roleBtn.setOnClickListener {             // 显示ListPopupWindow
            rolePopupWindow.show()
        }
    }
    fun initLangs(){
        // 初始化ListPopupWindow
        promptPopupWindow = ListPopupWindow(this)
        // 设置列表项

        val langArray = arrayOf(
            Lang("中文", Locale.CHINESE.language),
            Lang("日本語", Locale.JAPANESE.language),
            Lang("English", Locale.ENGLISH.language),
        )

        val adapter = LangAdaptor(this, langArray)

        promptPopupWindow.setAdapter(adapter)

        val widthInDp = 260 // 你想要设置的宽度，单位dp
        val scale: Float = getResources().displayMetrics.density
        val widthInPx = (widthInDp * scale + 0.5f).toInt()
        promptPopupWindow.width = widthInPx
        promptPopupWindow.height = ListPopupWindow.WRAP_CONTENT
        promptPopupWindow.anchorView = langBtn;
        promptPopupWindow.isModal = true;
        promptPopupWindow.clearListSelection()
        promptPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            promptPopupWindow.dismiss() // 关闭弹出窗口

            PreferenceManager.getInstance().saveLang(langArray[position])
            switchLang(langArray[position].value);
        }

        langBtn.setOnClickListener {             // 显示ListPopupWindow
            promptPopupWindow.show()
        }
    }

    fun initMusicMenu(){
        musicMenuPopupWindow = ListPopupWindow(this)
        // 设置列表项

        val operateArray = arrayOf(
            getString(R.string.music_play),
            getString(R.string.music_pause),
            getString(R.string.music_stop),
            "moonlight",
            "night_splash",
            "rock_and_electro",
            "spaghetti_forks",
            "spook",
            "string_house",
            "zashikiwarashi"
        )


        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, operateArray)

        musicMenuPopupWindow.setAdapter(adapter)

        val widthInDp = 260 // 你想要设置的宽度，单位dp
        val scale: Float = getResources().displayMetrics.density
        val widthInPx = (widthInDp * scale + 0.5f).toInt()
        musicMenuPopupWindow.width = widthInPx
        musicMenuPopupWindow.height = ListPopupWindow.WRAP_CONTENT
        musicMenuPopupWindow.anchorView = musicBtn;
        musicMenuPopupWindow.isModal = true;
        musicMenuPopupWindow.clearListSelection()
        musicMenuPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            musicMenuPopupWindow.dismiss() // 关闭弹出窗口

            Log.d(TAG, "music idx:$position")

            when(position){
                0 ->{
                    playMusic()
                }
                1 ->{
                    pauseMusic()
                }
                2 ->{
                    stopPlaying()
                }
                3 ->{
                    stopPlaying()
                    // 初始化MediaPlayer
                    mediaPlayer = MediaPlayer.create(this, R.raw.moonlight) // 假设MP3文件在res/raw目录下

                    // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
                    mediaPlayer?.setOnCompletionListener { mp ->
                        // 当音乐播放完成时执行的操作
                        stopPlaying()
                    }
                }
                4 ->{
                    stopPlaying()
                    // 初始化MediaPlayer
                    mediaPlayer = MediaPlayer.create(this, R.raw.night_splash) // 假设MP3文件在res/raw目录下

                    // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
                    mediaPlayer?.setOnCompletionListener { mp ->
                        // 当音乐播放完成时执行的操作
                        stopPlaying()
                    }
                }
                5 ->{
                    stopPlaying()
                    // 初始化MediaPlayer
                    mediaPlayer = MediaPlayer.create(this, R.raw.rock_and_electro) // 假设MP3文件在res/raw目录下

                    // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
                    mediaPlayer?.setOnCompletionListener { mp ->
                        // 当音乐播放完成时执行的操作
                        stopPlaying()
                    }
                }
                6 ->{
                    stopPlaying()
                    // 初始化MediaPlayer
                    mediaPlayer = MediaPlayer.create(this, R.raw.spaghetti_forks) // 假设MP3文件在res/raw目录下

                    // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
                    mediaPlayer?.setOnCompletionListener { mp ->
                        // 当音乐播放完成时执行的操作
                        stopPlaying()
                    }
                }
                7 ->{
                    stopPlaying()
                    // 初始化MediaPlayer
                    mediaPlayer = MediaPlayer.create(this, R.raw.spook) // 假设MP3文件在res/raw目录下

                    // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
                    mediaPlayer?.setOnCompletionListener { mp ->
                        // 当音乐播放完成时执行的操作
                        stopPlaying()
                    }
                }
                8 ->{
                    stopPlaying()
                    // 初始化MediaPlayer
                    mediaPlayer = MediaPlayer.create(this, R.raw.string_house) // 假设MP3文件在res/raw目录下

                    // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
                    mediaPlayer?.setOnCompletionListener { mp ->
                        // 当音乐播放完成时执行的操作
                        stopPlaying()
                    }
                }
                9 ->{
                    stopPlaying()
                    // 初始化MediaPlayer
                    mediaPlayer = MediaPlayer.create(this, R.raw.zashikiwarashi) // 假设MP3文件在res/raw目录下

                    // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
                    mediaPlayer?.setOnCompletionListener { mp ->
                        // 当音乐播放完成时执行的操作
                        stopPlaying()
                    }
                }
            }
        }

        musicBtn.setOnClickListener {             // 显示ListPopupWindow
            musicMenuPopupWindow.show()
        }
    }

    override fun updateUiForLang(){
        if(isEmoVisible()){
            emoShowOrHideBtn.text = getString(R.string.emo_on)
        }
        else{
            emoShowOrHideBtn.text = getString(R.string.emo_off)
        }

        appStopBtn.text = getString(R.string.stop_app)
        val isChecked = startOrStopBtn.isChecked
        startOrStopBtn.text = if (isChecked) getString(R.string.stop_robot) else getString(R.string.start_robot)
        llmBtn.text = getString(R.string.llm)
        roleBtn.text = getString(R.string.role)
        toneBtn.text = getString(R.string.tone)
        promptBtn.text = getString(R.string.role_setting)
        musicBtn.text = getString(R.string.music)
        langBtn.text = getString(R.string.lang_setting)
        hearingTitleTV.text = getString(R.string.hearing)
        connStatusTV.text = getString(R.string.conn_status_err)
        promptCloseBtn.text = getString(R.string.close)
        promptSaveBtn.text = getString(R.string.save)

        initMusicMenu()
    }

    enum class Emo(private val gifResourceId: Int) {
        IDLE(R.drawable.emo_idle),  // 确保这些资源 ID 是有效的
        LISTENING(R.drawable.emo_listening),
        THINKING(R.drawable.emo_thinking),
        SPEAKING(R.drawable.emo_speaking),
        CONFUSED(R.drawable.emo_confused),
        SURPRISED(R.drawable.emo_surprised),
        EXCITING(R.drawable.emo_exciting),
        SAD(R.drawable.emo_sad);


        // 在Kotlin中，枚举类的成员可以直接访问，无需getter方法
        // 但如果你确实需要getter方法，也可以添加
        fun getGifResourceId(): Int = gifResourceId

        // 通常情况下，你可以直接通过枚举实例访问`gifResourceId`，无需getter
        // 例如：GifState.WAKEUP.gifResourceId
    }


    fun showEmo(emo: Emo) {
        //emoIV.setImageResource(emo.getGifResourceId())

        if (emoIV != null) {
            Log.d("HomeFragment", "Showing GIF: " + emo.name)
            Glide.with(this)
                .asGif()
                .load(emo.getGifResourceId())
                .into(emoIV!!)
        } else {
            Log.e("HomeFragment", "imageViewGif is null")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //actionBarNavigation(false)
        setContentView(R.layout.activity_ivw)


        activityResultLauncher.launch(
            arrayListOf(Manifest.permission.RECORD_AUDIO).apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    //蓝牙设备权限
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }.toTypedArray()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }

        initView()
        initMicView()

        Log.d(TAG, "start Ivw 1")

        KeywordEngineManager.setListener(this)
        initSpeechReco()
        AudioPlayer.getInstance().setListener(this);

        Handler(mainLooper).postDelayed(
            { tvResult.text = "PIX语音机器人" + "e867a88f2" + "\n" + tvResult.text }
            , 2000)


        // 初始化MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.moonlight) // 假设MP3文件在res/raw目录下

        // 设置MediaPlayer监听器（可选，用于处理播放完成等事件）
        mediaPlayer?.setOnCompletionListener { mp ->
            // 当音乐播放完成时执行的操作
            stopPlaying()
        }
    }

    // 播放音乐
    fun playMusic() {
        if (!mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
        }
    }

    // 暂停音乐
    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.pause()
        }
    }

    // 停止音乐并重置MediaPlayer
    fun stopPlaying() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.seekTo(0) // 重置MediaPlayer到初始状态
            mediaPlayer?.pause()
        }

        // 可以在这里添加释放MediaPlayer的代码，但通常在Activity的onDestroy中做
    }

    private fun initSpeechReco (){
        //val speechSubscriptionKey = "
        // " //key1
        val speechSubscriptionKey =  "dd5a4e4489a242d49902f84a3b809006" //key2
        val speechRegion = "eastasia"

        try {
            speechConfig = SpeechConfig.fromSubscription(
                speechSubscriptionKey,
                speechRegion
            )
            speechConfig!!.setSpeechRecognitionLanguage("zh-CN")
            speechConfig!!.speechSynthesisLanguage = "zh-CN" // 设置合成语言为中文
            speechConfig!!.speechSynthesisVoiceName = "zh-CN-XiaoxiaoNeural" // 设置中文语音
        } catch (ex: java.lang.Exception) {
            println(ex.message)
        }
    }

    // create config
    private var speechConfig: SpeechConfig? = null
    private var continuousListeningStarted = false
    private var audioInput: AudioConfig? = null
    private var reco: SpeechRecognizer? = null
    private val content = ArrayList<String>()

    private val executorService: ExecutorService = Executors.newCachedThreadPool();

    private fun interface OnTaskCompletedListener<T> {
        fun onCompleted(taskResult: Void)
    }


    private var microphoneStream: MicrophoneStream? = null
    private fun createMicrophoneStream(): MicrophoneStream {
        releaseMicrophoneStream()
        microphoneStream = MicrophoneStream()
        return microphoneStream as MicrophoneStream
    }

    private fun releaseMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream!!.close()
            microphoneStream = null
        }
    }

    private fun <T> setOnTaskCompletedListener(task: Future<Void>, listener: OnTaskCompletedListener<T>) {
        executorService?.submit<Any>(Callable<Any?> {
            val result = task.get()
            listener.onCompleted(result)
            null
        })
    }


    private fun startSpeechReco() {
        if(continuousListeningStarted){
            Log.d(TAG, "startSpeechReco continuousListeningStarted is true")
            stopSpeechReco()
        }

        try {
            content.clear()
            audioInput = AudioConfig.fromStreamInput(createMicrophoneStream())
            reco = SpeechRecognizer(speechConfig, audioInput)
            reco!!.recognizing.addEventListener { o: Any?, speechRecognitionResultEventArgs: SpeechRecognitionEventArgs ->
                val result = speechRecognitionResultEventArgs.result.text
                Log.i(TAG, "Intermediate result received: $result")
                content.add(result)
                content.removeAt(content.size - 1)

                runOnUiThread(Runnable {
                    handleIntermediateSpeechRecoResult(result)
                })
            }
            reco!!.recognized.addEventListener { o: Any?, speechRecognitionResultEventArgs: SpeechRecognitionEventArgs ->
                val result = speechRecognitionResultEventArgs.result.text

                if(result.isEmpty()){
                    Log.i(TAG, "Final empty result received: $result" + "reason:" + speechRecognitionResultEventArgs.result.reason.toString())
                }
                else{
                    Log.i(TAG, "Final result received: $result" + "reason:" + speechRecognitionResultEventArgs.result.reason.toString())
                    content.add(result)

                    runOnUiThread(Runnable {
                        showEmo(Emo.THINKING)
                        handleSpeechRecoResult(result)
                    })
                }
            }

            val task = reco!!.startContinuousRecognitionAsync()

            continuousListeningStarted = true

            executorService?.submit<Any>(Callable<Any?> {
                val result = task.get()
                Log.i(TAG, "Continuous recognition started: $task, result:$result")
                null
            })

        } catch (ex: java.lang.Exception) {
            Log.e(TAG, "startSpeechReco err:" + ex.message)
        }
    }


    private fun stopSpeechReco() {
        if (continuousListeningStarted) {
            if (reco != null) {
                Log.i(TAG, "try stop Continuous recognition.")
                val task = reco!!.stopContinuousRecognitionAsync()
                // to do 这个方法不会被调用

                executorService?.submit<Any>(Callable<Any?> {
                    val result = task.get()
                    Log.i(TAG, "Continuous recognition stopped. $task, result:$result")
                    null
                })


                continuousListeningStarted = false
            } else {
                continuousListeningStarted = false
            }
            return
        }
    }

    private fun handleIntermediateSpeechRecoResult(result: String){
        stopRecoResultTimer()
    }

    private fun handleSpeechRecoResult(result: String){
        speechRecoResult += result
        startRecoResultTimer(speechRecoResult)
        startAutoDisconnectTimer()
    }


    private var keywordEnabled = false
    private fun disableKeyword(){
        Log.d(TAG, "disableKeyword")
        keywordEnabled = false
    }
    private fun enableKeyword(){
        Log.d(TAG, "enableKeyword")
        keywordEnabled = true
    }




    // 启动机器人功能
    private fun startRobot(){
        // 设置 TtsServerManager 的监听器为当前类实例
        TtsServerManager.getInstance().setListener(this)

        // 连接 TtsServerManager，可能用于启动语音合成功能
        TtsServerManager.getInstance().connect()

        // 启动一个协程，在 IO 线程中执行以下操作
        lifecycleScope.launch(Dispatchers.IO) {
            startSpeechReco()  // 启动语音识别，让机器人开始“听”用户的声音
        }
    }

    // 停止机器人功能
    private fun stopRobot(){
        // 启动一个协程，在 IO 线程中执行以下操作
        lifecycleScope.launch(Dispatchers.IO) {
            // KeywordEngineManager.stop()  // （此行被注释）停止关键字引擎

            // 断开 TtsServerManager 的连接，可能用于停止语音合成功能
            TtsServerManager.getInstance().disconnect()

            stopSpeechReco()  // 停止语音识别，机器人不再“听”用户的声音
        }
    }


    private fun initStartOrStopBtn(check: Boolean) {
        // 清除按钮的所有监听器，避免重复添加监听器导致重复响应
        startOrStopBtn.clearOnCheckedChangeListeners()

        // 根据传入的布尔值 check 设置按钮的选中状态
        startOrStopBtn.isChecked = check

        // 根据选中状态设置按钮的文本：选中显示“停止机器人”，未选中显示“启动机器人”
        startOrStopBtn.text = if (check) getString(R.string.stop_robot) else getString(R.string.start_robot)

        // 添加按钮状态变化的监听器，当按钮状态发生变化时触发
        startOrStopBtn.addOnCheckedChangeListener { button, isChecked ->
            Log.i(TAG, "initStartOrStopBtn, isChecked:$isChecked")  // 打印当前按钮的选中状态到日志

            // 当按钮被选中时，调用 showEmo 显示机器人的“空闲”状态
            if (isChecked) {
                showEmo(Emo.IDLE)
            }

            // 当按钮被选中时启动机器人，未选中时停止机器人
            if (isChecked) {
                startRobot()  // 启动机器人
                // startOrStopBtn.visibility = View.INVISIBLE  // （此行被注释）设置按钮不可见
                emoLayout.visibility = View.VISIBLE  // 设置 emoLayout 可见
                emoShowOrHideBtn.text = getString(R.string.emo_off)  // 设置 emoShowOrHideBtn 的文本为“emo 关闭”
            } else {
                stopRobot()  // 停止机器人

            }

            // 根据选中状态再次设置按钮文本，确保按钮显示当前状态的操作
            startOrStopBtn.text = if (isChecked) getString(R.string.stop_robot) else getString(R.string.start_robot)
        }
    }


    private fun initAudioRelatedBtns(audioVisible: Boolean) {
        startOrStopBtn.isVisible = audioVisible
//        tvAudioRecord.isVisible = audioVisible
        btnAudioFile.isVisible = !audioVisible
    }

    private fun audioButtonEnable(enable: Boolean) {
//        audioGroup.setChildrenEnabled(enable)
        tvKeyword.isEnabled = enable
    }


    val apiKey = "3c9b56e0-7bf4-45ab-89e8-e8fb255890c3"
    val model = "ep-20240824145049-hgtw4"

    //val model = "ep-20240825110851-pv2pr" //min_avbs

    private var messages: MutableList<ChatMessage> = ArrayList()

    private fun clearMessages(){
        messages = ArrayList()
    }
    private fun filterMessages(){
        val historyMsgSize = 10
        // 检查messages列表的大小，并取最小值以避免索引越界
        val size = minOf(historyMsgSize, messages.size)

        // 如果列表大小小于5，直接返回原列表（或者你可以决定如何处理这种情况）
        val roleName = PreferenceManager.getInstance().getRoleName()
        val role = roleName?.let { PreferenceManager.getInstance().getRole(it) }
        val prompt = role!!.value

        val promptMsg = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(prompt).build()

        if(size == 0){
            messages.add(promptMsg)
        }else if (size < historyMsgSize) {
            messages.removeAt(0)
            messages.add(0, promptMsg)
        } else {
            messages = messages.takeLast(historyMsgSize).toMutableList()
            messages.removeAt(0)
            messages.add(0, promptMsg)
        }
    }

    fun sendTextToChatVolc1(text: String) {
        Thread {
            val service = ArkService.builder().apiKey(apiKey).build()

            Log.i(TAG, ">>>standard request start")

            filterMessages();

            val userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(text).build()
            messages.add(userMessage)

            Log.d(TAG, "msgs:$messages")

            val chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build()

            var cnt:Int = 0

            try {
                service.createChatCompletion(chatCompletionRequest).choices.forEach(Consumer { choice: ChatCompletionChoice ->
                    cnt++
                    val reply = choice.message.content.toString()
                    Log.i(TAG, "VoiceAssistanth回复" + cnt + ":"  + reply)


                    Log.d("VoiceAssistant", "回复: $reply")

                    val sysMsg = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(reply).build()
                    messages.add(sysMsg)


                    runOnUiThread{
                        updateTvResult("语音机器人:$reply")

                        val respWithEmo = RespWithEmo.fromJson(reply)

                        Log.i(TAG, "完成语音识别")

                        if(respWithEmo == null){
                            Log.i(TAG, "回复格式不对")
                            return@runOnUiThread
                        }
                        stopSpeechReco()

                        if(!isSynthesizing){
                            this.respWithEmo = respWithEmo
                            Log.w(TAG, "语音开始合成")
                            isSynthesizing = true
                            //synthesizing("$reply")
                            TtsServerManager.getInstance().send(respWithEmo.resp)
                        }else{
                            Log.w(TAG, "还有语音正在合成中，忽略新的语音合成请求:$reply")
                        }
                    }
                })
            }catch (e: Exception) {
               Log.w(TAG, "msg:$e")
                runOnUiThread{
                    showErrorToast("Err:$e")
                }
            }


            service.shutdownExecutor()
        }.start()
    }

    //async streaming request
    public fun sendTextToChatVolc4(text: String) {

        Thread {
            val service = ArkService.builder().apiKey(apiKey).build()

            Log.i(TAG, "----- streaming request -----")
            val streamMessages: MutableList<ChatMessage> = ArrayList()
            val streamSystemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM)
                .content("你是豆包，是由字节跳动开发的 AI 人工智能助手").build()
            val streamUserMessage = ChatMessage.builder().role(ChatMessageRole.USER)
                .content("常见的十字花科植物有哪些？").build()
            streamMessages.add(streamSystemMessage)
            streamMessages.add(streamUserMessage)
            val streamChatCompletionRequest = ChatCompletionRequest.builder().model(model).messages(streamMessages).build()

            var cnt:Int = 0
            service.streamChatCompletion(streamChatCompletionRequest)
                .doOnError { obj: Throwable -> obj.printStackTrace() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe { choice: ChatCompletionChunk ->
                    if (choice.choices.size > 0) {
                        cnt++
                        Log.i(TAG, "" + cnt + ":"  + choice.choices[0].message.content.toString())
                    }
                }

            Thread.sleep(60000)
            service.shutdownExecutor()
            Log.i(TAG, ">>>streaming request end")
        }.start()
    }


    class Weather(var type: String, var properties: HashMap<String?, Any?>, var required: List<String>)


    private fun updateTvResult(result: String){
        tvResult.text = "${result}\n" + tvResult.text;
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothConnectedRegistered) {
            bluetoothConnectedRegistered = false
        }

        stopSpeechReco()

        // 释放MediaPlayer资源
        mediaPlayer?.release()
        mediaPlayer = null

    }


    override fun onConnected(ttsServerManager: TtsServerManager) {
        Log.i(TAG, "tts server onConnected")

        runOnUiThread{
            showNormalToast(getString(R.string.conn_status_ok))
            showEmo(Emo.LISTENING)
        }

    }

    private  fun handleTtsServerUnavailable(){
        isSynthesizing = false
        showEmo(Emo.IDLE)

        initStartOrStopBtn(false)

        showErrorLongToast(getString(R.string.conn_status_err))
    }

    override fun onDisconnected(
        ttsServerManager: TtsServerManager?,
        code: Int,
        reason: String?,
        remote: Boolean
    ) {
        Log.i(TAG, "语音合成断开连接, code:$code, reason:$reason, remote:$remote")

        runOnUiThread {
            handleTtsServerUnavailable()
        }
    }

    //非主线程
    override fun onResult(ttsServerManager: TtsServerManager, audio: ByteArray?) {
        Log.i(TAG, "完成语音合成 开始播放语音");

        runOnUiThread {
            val emo = this.respWithEmo.emo

            when(emo){
                RespWithEmo.NORMAL ->{
                    showEmo(Emo.SPEAKING)
                    Log.i(TAG, "SPEAKING")
                }
                RespWithEmo.EXCITING ->{
                    showEmo(Emo.EXCITING)
                    Log.i(TAG, "EXCITING")
                }
                RespWithEmo.SURPRISED ->{
                    showEmo(Emo.SURPRISED)
                    Log.i(TAG, "SURPRISED")
                }
                RespWithEmo.SAD ->{
                    showEmo(Emo.SAD)
                    Log.i(TAG, "SAD")
                }
                RespWithEmo.CONFUSED ->{
                    showEmo(Emo.CONFUSED)
                    Log.i(TAG, "CONFUSED")
                }
                else->{
                    showEmo(Emo.SPEAKING)
                    Log.i(TAG, "SPEAKING")
                }
            }

            AudioPlayer.getInstance().play(audio);
        }
    }

    override fun onError(ttsServerManager: TtsServerManager, e: java.lang.Exception?) {
        Log.i(TAG, "语音合成失败, " + e.toString() );
    }


    override fun onStart(manager: KeywordEngineManager) {
        tvResult.text = "语音机器人已启动\n" + tvResult.text;
        enableKeyword()
        audioButtonEnable(false)
    }

    override fun onResult(manager: KeywordEngineManager, result: String) {
        updateTvResult("语音机器人:在呢")

        showEmo(Emo.LISTENING)

        Log.d(TAG, "${result}\n")


        if(keywordEnabled){
            startSpeechReco()
        }

        disableKeyword()
    }

    override fun onError(manager: KeywordEngineManager, code: Int, error: Throwable?) {
        audioButtonEnable(true)
        initStartOrStopBtn( false)
        tvResult.text = "Keyword Engine error:$code, msg=${error?.message}\n" + tvResult.text;
    }

    override fun onStop(manager: KeywordEngineManager) {
        tvResult.text = "语音机器人退出\n" + tvResult.text;
        audioButtonEnable(true)
        initStartOrStopBtn(false)
    }

    override fun onStartRecord(manager: KeywordEngineManager) {
    }

    override fun onPauseRecord(manager: KeywordEngineManager) {

    }

    override fun onResumeRecord(manager: KeywordEngineManager) {

    }

    override fun onRecordProgress(
        manager: KeywordEngineManager,
        data: ByteArray?,
        sampleSize: Int,
        volume: Int
    ) {
        runOnUiThread {
        }
    }

    override fun onStopRecord(manager: KeywordEngineManager, output: File?) {
    }

    override fun onCompletion(audioPlayer: AudioPlayer?) {
        isSynthesizing = false
        Log.i(TAG, "语音播放完成");
        Log.i(TAG, ">>>开始语音识别");
        showEmo(Emo.LISTENING)
        startSpeechReco()
    }

    override fun onError(audioPlayer: AudioPlayer?, e: Exception?) {
        isSynthesizing = false
        Log.i(TAG, "语音播放错误:" + e.toString());
        Log.i(TAG, ">>>开始语音识别");
        showEmo(Emo.LISTENING)
        startSpeechReco()
    }
}