package com.valleapp.comandas.tareas;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.valleapp.comandas.utilidades.HTTPRequest;
import com.valleapp.comandas.utilidades.Instruccion;


import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class TareaManejarInstrucciones extends TimerTask {

    private final Timer parent;
    private final Queue<Instruccion> cola;


    boolean procesado = true;


    Handler handleHttp = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                String res = msg.getData().getString("RESPONSE");
                 if(!res.equals("")) {
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
                        procesado = true;
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            super.handleMessage(msg);

        }
    };

    public TareaManejarInstrucciones(Timer timerManejarInstrucciones, Queue<Instruccion> colaInstrucciones) {
        this.cola= colaInstrucciones;
        this.parent = timerManejarInstrucciones;
    }

    @Override
    public void run() {
        try {

            Log.i("TAREAS_PENDIENTES", String.valueOf(cola.size()));

            if (procesado) {

                synchronized (cola) {
                    Instruccion inst = cola.peek();

                    if (inst != null) {
                        procesado = false;
                        Log.i("Jose_luis", inst.getUrl());
                        new HTTPRequest(inst.getUrl(), inst.getParams(), "", handleHttp);
                    }
                }
            }


            synchronized (parent) {
                parent.wait(3000);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
