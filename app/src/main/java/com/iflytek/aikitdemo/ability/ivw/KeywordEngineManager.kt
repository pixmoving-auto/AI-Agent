package com.iflytek.aikitdemo.ability.ivw;

import android.util.Log
import com.iflytek.aikitdemo.MyApp
import com.iflytek.aikitdemo.ability.AbilityCallback
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset


object KeywordEngineManager : AbilityCallback {
    private val TAG = "avbs" + KeywordEngineManager.javaClass.name

    // 假设有一些内部状态或资源
    private var isInitialized = false
    private var isStarted = false


    private var listener: KeywordEngineListener? = null


    fun setListener(listener: KeywordEngineListener?) {
        this.listener = listener
    }


    private fun createKeywordFile(keywordList: String): String {
        val file = File(MyApp.CONTEXT.externalCacheDir, "keyword.txt")
        if (file.exists()) {
            file.delete()
        }
        val binFile = File("${MyApp.CONTEXT.externalCacheDir}/process", "key_word.bin")
        if (binFile.exists()) {
            binFile.delete()
        }
        kotlin.runCatching {
            val keyword = keywordList.trim()
                .replace("；", ";")
                .replace(";", ";\n")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
            val bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(file), Charset.forName("utf-8")))
//            val bufferedWriter = BufferedWriter(FileWriter(file))
            bufferedWriter.write(keyword)
            bufferedWriter.close()
        }.onFailure {
            Log.w(TAG, "唤醒词写入失败")
        }
        return file.absolutePath
    }


    // 启动方法, 非阻塞
    fun start(keywordList: String, threshold: Int) {
        if (!isInitialized) {
            throw IllegalStateException("KeywordEngineManager is not initialized")
        }
        if (!isStarted) {
            Log.d(TAG, "Starting KeywordEngineManager...")

            val filePath = createKeywordFile(keywordList)
            val keywordSize = keywordList.trim().split(";").count()
//            ivwHelper?.startAudioRecord(filePath, keywordSize, threshold)
            isStarted = true
        }
    }

    //异步回调
    override fun onAbilityBegin() {
        listener?.onStart(KeywordEngineManager)
        isStarted = true;
    }

    //异步回调
    override fun onAbilityResult(result: String) {
       listener?.onResult(KeywordEngineManager,result)
    }

    //异步回调
    override fun onAbilityError(code: Int, error: Throwable?) {
        listener?.onError(KeywordEngineManager, code, error)
        isStarted = false;
    }

    //异步回调
    override fun onAbilityEnd() {
        listener?.onStop(KeywordEngineManager)
        isStarted = false;
    }
}