package com.valleapp.comandas.utilidades;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import com.valleapp.comandas.db.DBBase;
import com.valleapp.comandas.db.DBCamareros;
import com.valleapp.comandas.db.DBCuenta;
import com.valleapp.comandas.db.DBMesas;
import com.valleapp.comandas.db.DBSugerencias;
import com.valleapp.comandas.db.DBMesasAbiertas;
import com.valleapp.comandas.db.DBSecciones;
import com.valleapp.comandas.db.DBSubTeclas;
import com.valleapp.comandas.db.DbTbUpdates;
import com.valleapp.comandas.db.DBTeclas;
import com.valleapp.comandas.db.DBZonas;
import com.valleapp.comandas.interfaces.IBaseDatos;
import com.valleapp.comandas.tareas.TareaManejarAutorias;
import com.valleapp.comandas.tareas.TareaManejarInstrucciones;
import com.valleapp.comandas.tareas.TareaUpdateForDevices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;


public class ServicioCom extends Service {


    final IBinder myBinder = new MyBinder();

    String server = null;

    Timer timerUpdateFast = new Timer();
    Timer timerUpdateLow = new Timer();
    Timer timerManejarInstrucciones = new Timer();
    Timer timerAurotias = new Timer();

    Map<String, Handler> exHandler = new HashMap<>();
    Map<String, IBaseDatos> dbs;

    DbTbUpdates dbTbUpdates;

    Queue<Instruccion> colaInstrucciones = new LinkedList<>();

    String[] tbNameUpdateFast;
    String[] tbNameUpdateLow;



    private final Handler controller_http = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            try{
                switch (op){
                    case "check_updates":
                        delegadoHandleCheckUpdates(res);
                        break;
                    case "update_table":
                        delegadoHandleUpdateTable(res);
                        break;
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    private void delegadoHandleUpdateTable(String res) {
        try{
            JSONObject obj = new JSONObject(res);
            String tb_name = obj.getString("nombre");
            String last = obj.getString("last");
            JSONArray objs = obj.getJSONArray("objs");
            IBaseDatos db = dbs.get(tb_name);
            Log.i("ServicioCom", tb_name);
            synchronized (db){
                db.rellenarTabla(objs);
            }
            dbTbUpdates.upTabla(tb_name, last);
            if (exHandler.containsKey(tb_name)) exHandler.get(tb_name).sendEmptyMessage(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void delegadoHandleCheckUpdates(String res) {
        try {
            JSONObject obj = new JSONObject(res);
            if(dbTbUpdates.is_updatable(obj)){
                ContentValues p = new ContentValues();
                p.put("tb", obj.getString("nombre"));
                new HTTPRequest(server +"/sync/update_for_devices", p,"update_table", controller_http);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String url = intent.getStringExtra("url");
        if (url != null){
            server = url;
            IniciarDB();
            timerManejarInstrucciones.schedule(new TareaManejarInstrucciones(timerManejarInstrucciones, colaInstrucciones), 2000, 1);
            timerUpdateFast.schedule(new TareaUpdateForDevices(tbNameUpdateFast, server, controller_http, timerUpdateFast, 5000), 2000, 1);
            timerUpdateLow.schedule(new TareaUpdateForDevices(tbNameUpdateLow, server, controller_http, timerUpdateLow, 20000), 2000, 1);
            return START_STICKY;
        }
        return START_NOT_STICKY;

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        timerUpdateFast.cancel();
        timerUpdateLow.cancel();
        timerAurotias.cancel();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;
    }

    public void setExHandler(String nombre, Handler handler) {
        exHandler.put(nombre, handler);
    }


    public void rmExHandler(String handlerName) {
        if (exHandler.containsKey(handlerName))   exHandler.remove(exHandler.get(handlerName));
    }

    public void initTimerAutorias(Handler mainHandler, String idautoria, String url){
        timerAurotias.schedule(new TareaManejarAutorias(mainHandler, idautoria, 2000, url), 2000, 1);
    }

    public void addColaInstrucciones(Instruccion inst) {
        synchronized (colaInstrucciones) {
            colaInstrucciones.add(inst);
        }
    }

    private void IniciarDB() {
        if (tbNameUpdateFast == null){
            tbNameUpdateFast = new String[]{
                    "camareros",
                    "mesasabiertas",
                    "lineaspedido"
            };
            tbNameUpdateLow = new String[]{
                    "zonas",
                    "mesas",
                    "teclas",
                    "subteclas",
                    "secciones_com",
                    "sugerencias"
            };

        }
        if (dbs == null){
            DBMesas dbMesas = new DBMesas(getApplicationContext());
            dbs = new HashMap<>();
            dbs.put("mesas", dbMesas);
            dbs.put("camareros", new DBCamareros(getApplicationContext()));
            dbs.put("zonas", new DBZonas(getApplicationContext()));
            dbs.put("secciones_com", new DBSecciones(getApplicationContext()));
            dbs.put("teclas", new DBTeclas(getApplicationContext()));
            dbs.put("lineaspedido", new DBCuenta(getApplicationContext()));
            dbs.put("subteclas", new DBSubTeclas(getApplicationContext()));
            dbs.put("sugerencias", new DBSugerencias(getApplicationContext()));
            dbs.put("mesasabiertas", new DBMesasAbiertas(dbMesas));
        }


        if(dbTbUpdates==null){
            dbTbUpdates = new DbTbUpdates(getApplicationContext());
            dbTbUpdates.inicializar();
        }

        for(IBaseDatos db: dbs.values()){
            db.inicializar();
        }
    }

    public IBaseDatos getDb(String nombre){
        return dbs.get(nombre);
    }

    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}