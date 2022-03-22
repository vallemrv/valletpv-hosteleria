package com.valleapp.valletpv.Util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WSClient {

    private WebSocketClient mWebSocketClient;
    private final Handler controller;
    public boolean opened = false;

    public void close(){
        mWebSocketClient.close();
    }

    private void sendRespuesta(String msg){
        Message obj_msg = this.controller.obtainMessage();
        Bundle bundle = obj_msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("RESPONSE", msg);
        obj_msg.setData(bundle);
        this.controller.sendMessage(obj_msg);
    }



    public WSClient(String url,  final Handler controller) {
        URI uri;
        this.controller = controller;
        try {
            uri = new URI("ws://"+url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                sendRespuesta("{'OP':'OPENED'}");
                opened = true;
                Log.d("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                sendRespuesta(s);
                Log.d("Websocket", s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                sendRespuesta("{'OP':'CLOSED'}");
                Log.d("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                sendRespuesta("{'OP':'ERROR', 'mensaje':'" + e.getMessage() + "'}");
                Log.d("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();
    }
}
