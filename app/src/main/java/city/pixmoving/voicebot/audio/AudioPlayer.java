package com.example.robobus_voicebot.audio;

import android.media.MediaPlayer;
import android.util.Log;

import com.iflytek.aikitdemo.MyApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class AudioPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private String TAG = "avbs" + AudioPlayer.class.getName();
    private static AudioPlayer instance;

    private AudioPlayerListener listener;

    public void setListener(AudioPlayerListener listener) {
        this.listener = listener;
    }


    // 私有构造函数，防止外部直接实例化
    private AudioPlayer() {

        // 这里可以设置其他播放器配置，如监听器
    }

    // 获取单例实例
    public static synchronized AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }

    private MediaPlayer mediaPlayer;
    // 播放方法，这里需要传入byte[]并创建MediaSource（这里只是示意）
    public void play(byte[] audio) {
        try {
            Log.d(TAG, "play step1");
            File tempMp3 = File.createTempFile("kurchina", "mp3", MyApp.Companion.getCONTEXT().getExternalFilesDir(null));

            Log.d(TAG, "play step2:" + tempMp3.toString());

            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(audio);
            fos.close();

            FileInputStream fis = new FileInputStream(tempMp3);

            if(mediaPlayer != null){
                mediaPlayer.setOnErrorListener(null);
                mediaPlayer.setOnErrorListener(null);
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);

            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();

            Log.d(TAG, "play step2");
        } catch (IOException ex) {
            Log.d(TAG, "play step3");
            if(listener != null){
                Log.d(TAG, "play step4");
                listener.onError(this, ex);
            }
        }
    }

    // 释放播放器资源（通常在Activity或Fragment的onDestroy中调用）

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(listener != null) {
            listener.onCompletion(this);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        if(listener != null){
            listener.onError(this, new Exception("i:" + i + "i1:" + i1));
            return false;
        }
        return false;
    }
}
