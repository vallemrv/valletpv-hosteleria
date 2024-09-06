package com.valleapp.valletpv.tareas;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;


import com.valleapp.valletpv.tools.Instrucciones;
import com.valleapp.valletpvlib.comunicacion.HTTPRequest;

import java.util.Queue;
import java.util.TimerTask;

public class TareaManejarInstrucciones extends TimerTask {

    private final Queue<Instrucciones> cola;
    private final long timeout;
    boolean procesado = true;
    int count = 0;

    Handler handleHttp = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                String op = msg.getData().getString("op");
                if (op != null && op.equals("ERROR")) {
                    cola.poll();
                }else {
                    String res = msg.getData().getString("RESPONSE");
                    if (res != null && !res.equals("")) {
                        synchronized (cola) {
                            Instrucciones inst = cola.poll();
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
            procesado = true;count=0;
            super.handleMessage(msg);

        }
    };

    public TareaManejarInstrucciones(Queue<Instrucciones> colaInstrucciones, long timeout) {
        this.cola= colaInstrucciones;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {

            if (procesado) {
                synchronized (cola) {
                    Instrucciones inst = cola.peek();
                    if (inst != null) {
                        new HTTPRequest(inst.getUrl(), inst.getParams(), "", handleHttp);
                        procesado = false;
                    }
                }
            }else{
                count ++;
            }

            if (count > 20) {
                count = 0;
                procesado = true;
            }

            Thread.sleep(timeout);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
