package com.valleapp.valletpv.tareas;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.valleapp.valletpv.interfaces.IBaseDatos;
import com.valleapp.valletpv.tools.HTTPRequest;
import com.valleapp.valletpv.tools.RowsUpdatables;
import com.valleapp.valletpv.db.DbTbUpdates;

import org.json.JSONObject;

import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class TareaUpdateFromDevices extends TimerTask {

    private final Timer parent;
    private final Queue<RowsUpdatables> colaUpdate;
    private final Map<String, IBaseDatos> dbs;
    private final String server;

    DbTbUpdates upTable;

    boolean procesado = true;


    public TareaUpdateFromDevices(Map<String, IBaseDatos> dbs, Timer parent, Queue<RowsUpdatables> colaTb, String server, DbTbUpdates upTb){
        this.dbs = dbs;
        this.parent = parent;
        this.colaUpdate = colaTb;
        this.server = server;
        upTable = upTb;
    }

    Handler handleModificados = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                String res = msg.getData().getString("RESPONSE");
                if(!res.equals("")) {
                    JSONObject o = new JSONObject(res);
                    String tb = o.getString("tb");
                    String last = o.getString("last");
                    synchronized (upTable) {
                        upTable.setLast(tb, last);
                        colaUpdate.poll();
                        procesado = true;
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            super.handleMessage(msg);

        }
    };

    @Override
    public void run() {
        try {
            RowsUpdatables row;
            while ((row = colaUpdate.peek()) == null && !procesado){
                synchronized (parent) {
                    parent.wait(1000);
                }
            }
            synchronized (dbs) {
                procesado = false;
                if (row != null) {

                    ContentValues p = new ContentValues();
                    p.put("tb", row.getTb_name());
                    p.put("rows", row.getRows().toString());
                    new HTTPRequest(server + "/sync/update_from_devices", p, "", handleModificados);
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
