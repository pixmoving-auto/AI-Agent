package city.pixmoving.voicebot.audio;

public interface AudioPlayerListener {
    void onCompletion(AudioPlayer audioPlayer);

    void onError(AudioPlayer audioPlayer, Exception e);



}