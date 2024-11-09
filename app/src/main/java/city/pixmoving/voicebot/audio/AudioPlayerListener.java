package com.example.robobus_voicebot.audio;

public interface AudioPlayerListener {
    void onCompletion(AudioPlayer audioPlayer);

    void onError(AudioPlayer audioPlayer, Exception e);



}