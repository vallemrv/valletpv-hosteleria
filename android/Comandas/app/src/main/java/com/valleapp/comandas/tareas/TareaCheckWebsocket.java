package com.valleapp.comandas.tareas;

import android.content.ContentValues;
import android.os.Handler;

import com.valleapp.comandas.utilidades.HTTPRequest;

import java.util.Timer;
import java.util.TimerTask;


public class TareaCheckWebsocket extends TimerTask {

    private final String server;
    private final Handler handler;
    private final Timer parent;
    private final long timeout;
    String[] tablasUpdatables;

    public TareaCheckWebsocket(String server,
                               Handler handler,
                               Timer parent,
                               long timeout){
        this.server = server;
        this.handler = handler;
        this.parent = parent;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            for(String tb: tablasUpdatables) {
                ContentValues p = new ContentValues();
                p.put("tb", tb);
                new HTTPRequest(server + "/sync/get_tb_up_last", p, "check_updates", handler);
                synchronized (parent) {
                    parent.wait(1000);
                }
            }

            synchronized (parent) {
                parent.wait(timeout);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
