package city.pixmoving.voicebot.ability;


import city.pixmoving.voicebot.manager.TtsServerManager;

public interface TtsServerListener {
    //会在非UI线程上被调用
    void onConnected(TtsServerManager ttsServerManager);

    //连接正常断开
    void onDisconnected(TtsServerManager ttsServerManager, int code, String reason, boolean remote);
    void onResult(TtsServerManager ttsServerManager, byte[] audio);
    //会在非UI线程上被调用
    void onError(TtsServerManager ttsServerManager, Exception e);
}