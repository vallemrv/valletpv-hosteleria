package com.valleapp.valletpv.tareas;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.valleapp.valletpv.tools.HTTPRequest;
import com.valleapp.valletpv.tools.Instrucciones;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class TareaManejarInstrucciones extends TimerTask {

    private final Timer parent;
    private final Queue<Instrucciones> cola;


    boolean procesado = true;


    Handler handleHttp = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                String res = msg.getData().getString("RESPONSE");
                if(!res.equals("")) {
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
                        procesado = true;
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            super.handleMessage(msg);

        }
    };

    public TareaManejarInstrucciones(Timer timerManejarInstrucciones, Queue<Instrucciones> colaInstrucciones) {
        this.cola= colaInstrucciones; this.parent = timerManejarInstrucciones;
    }

    @Override
    public void run() {
        try {
            Instrucciones inst;
            while ((inst = cola.peek()) == null && !procesado){
                synchronized (parent) {
                    parent.wait(1000);
                }
            }
            synchronized (cola) {
                procesado = false;
                if (inst != null) {
                    new HTTPRequest(inst.getUrl(), inst.getParams(),  "", handleHttp);
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
