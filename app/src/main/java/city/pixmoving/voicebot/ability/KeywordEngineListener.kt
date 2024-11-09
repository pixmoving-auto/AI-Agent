package com.example.robobus_voicebot.ability;

import com.example.robobus_voicebot.manager.KeywordEngineManager
import java.io.File;


interface KeywordEngineListener {
    fun onStart(manager: KeywordEngineManager)
    fun onResult(manager: KeywordEngineManager, result: String)
    fun onError(manager: KeywordEngineManager, code: Int, error: Throwable?)
    fun onStop(manager: KeywordEngineManager)

    fun onStartRecord(manager: KeywordEngineManager)
    fun onPauseRecord(manager: KeywordEngineManager)
    fun onResumeRecord(manager: KeywordEngineManager)
    fun onRecordProgress(manager: KeywordEngineManager, data: ByteArray?, sampleSize: Int, volume: Int)
    fun onStopRecord(manager: KeywordEngineManager, output: File?)
}