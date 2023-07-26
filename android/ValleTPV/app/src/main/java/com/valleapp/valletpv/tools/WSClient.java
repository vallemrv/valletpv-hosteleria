package com.valleapp.valletpv.tools;

import com.valleapp.valletpv.tareas.IController;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import java.net.URI;
import java.nio.ByteBuffer;

public class WSClient extends WebSocketClient {

    private boolean isWebsocketClose = false;
    private boolean exit = false;
    private final IController controller;

    public WSClient(String serverUri, String end_point,  IController controller) throws Exception {
        super(new URI(urlParse(serverUri)+end_point));
        this.controller = controller;
    }

    static String urlParse(String url) {
        if ( url.startsWith("http://") ) {
            url = url.replace("http://", "ws://");
        } else if ( url.startsWith("https://") ) {
            url = url.replace("https://", "wss://");
        }else {
            url = "ws://" + url;
        }
        return url;
    }
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        isWebsocketClose = false;
        System.out.println("Websocket open.....");
        controller.sync_device(new String[]{"mesasabiertas", "lineaspedido", "camareros"}, 500);
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject o = new JSONObject(message);
            controller.updateTables(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error de conexion....");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Websocket close....");
        isWebsocketClose = true;

        if (!exit) {
            try {
                this.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        System.out.println("socket bytebuffer bytes");
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        super.onWebsocketPong(conn, f);
    }

    public void salir() {
        this.exit = true; // Indica que no se debe intentar la reconexión automática
        this.close();
    }

    public boolean isWebsocketClose() {
        return isWebsocketClose;
    }
}
