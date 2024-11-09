package com.example.robobus_voicebot.manager;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.robobus_voicebot.ability.TtsRequest;
import com.example.robobus_voicebot.ability.TtsServerListener;
import com.example.robobus_voicebot.ability.TtsWebsocketDemo;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;


import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;


public class TtsServerManager{
    private String TAG = "avbs " + TtsServerManager.class.getName();

    public static final String API_URL = "wss://openspeech.bytedance.com/api/v1/tts/ws_binary";
    public static final String appid = "7657408905";
    public static final String accessToken = "";

    // 监听器引用
    private TtsServerListener listener;
    private WebSocketClient wsClient;

    // 私有化构造函数
    private TtsServerManager() {
        super();
       // this.wsClient = new WebSocketClient(URI.create(API_URL), Collections.singletonMap("Authorization", "Bearer; " + accessToken));


    }

    // 定义一个静态内部类，该类持有一个TtsServerManager的实例
    private static class SingletonHolder {
        private static final TtsServerManager INSTANCE = new TtsServerManager();
    }

    // 提供一个公共的静态方法返回实例
    public static TtsServerManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 设置监听器
    public void setListener(TtsServerListener listener) {
        this.listener = listener;
    }

    // 非阻塞的方法

    public void connect() {
        // 假设连接成功
        //Log.i(TAG, "Connecting to server: " + serverAddress + ":" + port);

        wsClient = new WebSocketClient(URI.create(API_URL), Collections.singletonMap("Authorization", "Bearer; " + accessToken)) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i(TAG, "opened connection");

                if (listener != null) {
                    listener.onConnected(TtsServerManager.this);
                }
            }

            @Override
            public void onMessage(String message) {
                Log.i(TAG, "received message: " + message);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                Log.i(TAG, ">>>>>onMessage start" + msgCnt++);

                int headerSize = bytes.get(0) & 0x0f;
                int messageType = (bytes.get(1) & 0xff) >> 4;
                int messageTypeSpecificFlags = bytes.get(1) & 0x0f;
                bytes.position(headerSize * 4);
                byte[] fourByte = new byte[4];
                if (messageType == 11) {
                    cnt++;
                    // Audio-only server response
                    Log.i(TAG, "onMessage received audio-only response:" + cnt);
                    if (messageTypeSpecificFlags == 0) {
                        // Ack without audio data
                    } else {
                        bytes.get(fourByte, 0, 4);
                        int sequenceNumber = new BigInteger(fourByte).intValue();


                        bytes.get(fourByte, 0, 4);
                        int payloadSize = new BigInteger(fourByte).intValue();
                        byte[] payload = new byte[payloadSize];
                        Log.i(TAG, "sequenceNumber:" + sequenceNumber + ", payloadSize:" + payloadSize);

                        bytes.get(payload, 0, payloadSize);

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        try {
                            buffer.write(payload);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (sequenceNumber < 0) {
                            // received the last segment
                            handleResult(true, buffer.toByteArray());
                        }
                    }
                } else if (messageType == 15) {
                    // Error message from server
                    bytes.get(fourByte, 0, 4);
                    int code = new BigInteger(fourByte).intValue();
                    bytes.get(fourByte, 0, 4);
                    int messageSize = new BigInteger(fourByte).intValue();
                    byte[] messageBytes = new byte[messageSize];
                    bytes.get(messageBytes, 0, messageSize);
                    String message = new String(messageBytes, StandardCharsets.UTF_8);
                    handleError(new TtsWebsocketDemo.TtsException(code, message));
                } else {
                    Log.w(TAG, "onMessage Received unknown response message type: {}:" + messageType);
                }

                Log.i(TAG, ">>>>>onMessage end" + msgCnt);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                // 通知监听器连接已断开
                if (listener != null) {
                    listener.onDisconnected(TtsServerManager.this, code, reason, remote);
                }
                // 在这里实现实际的断开连接逻辑
                Log.i(TAG, "Disconnecting from server");
            }

            @Override
            public void onError(Exception e) {
                handleError(e);
            }
        };

        wsClient.connect();
    }


    // 假设的disconnect方法，调用onDisconnected()
    public void disconnect() {
        if(wsClient != null && wsClient.isOpen()){
            wsClient. close();
        }
    }

    public boolean isConnected(){
        if(wsClient != null && wsClient.isOpen()){
            return wsClient. isOpen();
        }

        return false;
    }

    public void send(String text){
        if(!this.isConnected()){
            Log.i(TAG, "语音服务未启动，无法合成语言");
            return ;
        }

        // set your appid and access_token
//自定义音色
//        TtsRequest ttsRequest = TtsRequest.builder()
//                .app(TtsRequest.App.builder()
//                        .appid(appid)
//                        .cluster("volcano_mega")
//                        .build())
//                .user(TtsRequest.User.builder()
//                        .uid("uid")
//                        .build())
//                .audio(TtsRequest.Audio.builder()
//                        .encoding("mp3")
//                        .voiceType("S_33KAHMm11")
//                        .build())
//                .request(TtsRequest.Request.builder()
//                        .reqID(UUID.randomUUID().toString())
//                        .operation("query")
//                        .text(text)
//                        .build())
//                .build();

        TtsRequest ttsRequest = TtsRequest.builder()
                .app(TtsRequest.App.builder()
                        .appid(appid)
                        .cluster("volcano_tts")
                        .build())
                .user(TtsRequest.User.builder()
                        .uid("uid")
                        .build())
                .audio(TtsRequest.Audio.builder()
                        .encoding("mp3")
                        .voiceType(PreferenceManager.Companion.getInstance().getTone())
                        .build())
                .request(TtsRequest.Request.builder()
                        .reqID(UUID.randomUUID().toString())
                        .operation("query")
                        .text(text)
                        .build())
                .build();

        String json = JSON.toJSONString(ttsRequest);
        Log.i(TAG, "request: {}:" +json);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        byte[] header = {0x11, 0x10, 0x10, 0x00};
        ByteBuffer requestByte = ByteBuffer.allocate(8 + jsonBytes.length);
        requestByte.put(header).putInt(jsonBytes.length).put(jsonBytes);

        wsClient.send(requestByte.array());

        Log.i(TAG, "send text.");
    }

    void handleResult(boolean isSucceed, byte[] audio){
        if (listener != null) {
            listener.onResult(this, audio);
        }
    }

    void handleError(Exception e) {
        wsClient.close(CloseFrame.NORMAL, e.toString());

        Log.e(TAG, "An error occurred", e);

        // 通知监听器有错误发生
        if (listener != null) {
            listener.onError(this, e);
        }
    }

    private int cnt;
    private int msgCnt;
}


