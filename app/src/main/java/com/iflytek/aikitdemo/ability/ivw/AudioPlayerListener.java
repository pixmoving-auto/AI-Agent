package com.iflytek.aikitdemo.ability.ivw;

public interface AudioPlayerListener {
    void onCompletion(AudioPlayer audioPlayer);

    void onError(AudioPlayer audioPlayer, Exception e);



}