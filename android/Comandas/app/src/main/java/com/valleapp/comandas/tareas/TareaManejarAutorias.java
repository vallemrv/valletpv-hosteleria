package com.valleapp.comandas.tareas;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.valleapp.comandas.utilidades.HTTPRequest;

import org.json.JSONArray;
import java.util.TimerTask;


public class TareaManejarAutorias extends TimerTask {

    Handler mainHandler;
    String idautorizado;
    int autorizacionesSize = 0;
    int timeOut;
    boolean procesado = true;
    String url;

    public  TareaManejarAutorias(Handler mainHandler, String idautorizado, int timeOut, String url){
        this.timeOut = timeOut; this.idautorizado = idautorizado; this.mainHandler= mainHandler;
        this.url = url;
    }


    final  Handler  handler = new  Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                String res = msg.getData().getString("RESPONSE");
                if(res != null && res != "") {
                    JSONArray l = new JSONArray(res);
                    if (l.length() != autorizacionesSize) {
                        autorizacionesSize = l.length();
                        if (mainHandler != null) mainHandler.handleMessage(msg);
                    }
                }
            }catch (Exception e){
                Log.w("Autorias", "Respuesta erronea del servidor");
            }
            procesado = true;
        }
    };

    @Override
    public void run() {
            if (procesado){
                procesado = false;
                ContentValues p = new ContentValues();
                p.put("idautorizado", idautorizado);
                new HTTPRequest(url, p, "autorias", handler);
            }
            synchronized (this){
                try {
                    wait(timeOut);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }
}
