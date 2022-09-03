package com.valleapp.vallecom.tareas;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.valleapp.vallecom.utilidades.HTTPRequest;
import com.valleapp.vallecom.utilidades.Instruccion;

import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class TareaManejarInstrucciones extends TimerTask {

    private final Timer parent;
    private final Queue<Instruccion> cola;
    private final String server;
    private final long timeout;
    private final Map<String, Handler>  handlers;
    boolean procesado = true;
    int count = 0;

    Handler handleHttp = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                String op = msg.getData().getString("op");
                if (op != null && op.equals("ERROR")){
                    cola.poll();
                }else {
                    String res = msg.getData().getString("RESPONSE");
                    if (res != null && !res.equals("")) {
                        synchronized (cola) {
                            Instruccion inst = cola.poll();
                            if (inst != null) {
                                Handler handlerInst = inst.getHandler();
                                if (handlerInst != null) {
                                    Message msgInst = handlerInst.obtainMessage();
                                    Bundle bundle = msg.getData();
                                    if (bundle == null) bundle = new Bundle();
                                    bundle.putString("RESPONSE", res);
                                    bundle.putString("op", inst.getOp());
                                    msgInst.setData(bundle);
                                    handlerInst.sendMessage(msgInst);
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                Log.w("Instrucciones", "Error al ejecutar instruccion en el servidor");
            }

            procesado = true;
            count=0;

        }
    };

    public TareaManejarInstrucciones(Timer timerManejarInstrucciones,
                                     Queue<Instruccion> colaInstrucciones,
                                     String server,
                                     long timeout,  Map<String, Handler> handlers) {
        this.cola = colaInstrucciones;
        this.parent = timerManejarInstrucciones;
        this.server = server;
        this.timeout = timeout;
        this.handlers = handlers;

    }


    @SuppressLint("DefaultLocale")
    private void enviarInfo(){
        Handler h = handlers.get("estadows");
        if (h!=null) {
            HTTPRequest http = new HTTPRequest();
            http.sendMessage(h, "op_pendientes", String.format("%d tareas pendientes", cola.size()));
        }
    }

    @Override
    public void run() {
        try {

            if (procesado) {
                synchronized (cola) {
                    Instruccion inst = cola.peek();
                    if (inst != null) {
                        procesado = false;
                        new HTTPRequest(server + inst.getUrl(), inst.getParams(), "", handleHttp);
                    }
                }
            }else{
                count++;
            }

            synchronized (parent) {
                parent.wait(timeout);
                enviarInfo();
                if (count > 20) {
                    count = 0;
                    procesado = true;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
