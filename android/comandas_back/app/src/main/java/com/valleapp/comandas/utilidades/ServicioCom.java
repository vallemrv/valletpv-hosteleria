package com.valleapp.comandas.utilidades;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import com.valleapp.comandas.db.DbMesas;
import com.valleapp.comandas.db.DbSecciones;
import com.valleapp.comandas.db.DbSubTeclas;
import com.valleapp.comandas.db.DbTeclas;
import com.valleapp.comandas.db.DbZonas;

public class ServicioCom extends IntentService {

    final IBinder myBinder = new MyBinder();
    DbTeclas dbTeclas;
    DbMesas dbMesas;
    DbSubTeclas dbSubTeclas;
    DbZonas dbZonas;
    DbSecciones dbSecciones;

    String server = null;
    String camarero_id = null;
    Boolean reconectar = true;
    Queue<List<NameValuePair>> cola = new LinkedList<>();
    List<NameValuePair> pedido;
    boolean enviado = true;


    private Handler controller_zonas;
    private Handler controller_mesas;

    Timer timerUpdate = new Timer();
    Timer timer = new Timer();
   

    WSClient ws_manager;

    @SuppressLint("HandlerLeak")
    private  final Handler controller_WS = new Handler(){
        public void handleMessage(Message msg){
            String res = msg.getData().getString("RESPONSE");
            try{
                JSONObject aux = new JSONObject(res);
                String op = aux.getString("OP");
                if (op.equals("OPENED")){
                    if (server != null) {
                        List<NameValuePair> p = new ArrayList<>();
                        p.add(new BasicNameValuePair("hora", ""));
                        new HTTPRequest(server + "/sync/getupdate", p, "sync", controller_http);
                        List<NameValuePair> id_autoizado = new ArrayList<NameValuePair>();
                        id_autoizado.add(new BasicNameValuePair("id", camarero_id));
                        new HTTPRequest(server + "/camareros/es_autorizado", id_autoizado, "autorizado", controller_http);
                    }
                }
                if (op.equals("CLOSED")){
                    if (server != null && reconectar) {
                       ws_manager = new WSClient(server + "/ws/comunicacion/comandas", controller_WS);
                    }
                }
                if (op.equals("MENSAJE")){
                    if (controller_mesas != null) {
                        genrateMensajeNotification(aux.getString("msg"));
                    }
                }
                if (op.equals("CAMBIO_TURNO")){
                    List<NameValuePair> id_autoizado = new ArrayList<NameValuePair>();
                    id_autoizado.add(new BasicNameValuePair("id", camarero_id));
                    new HTTPRequest(server + "/camareros/es_autorizado", id_autoizado, "autorizado", controller_http);
                }
                if (op.equals("UPDATE")){
                    String tabla = aux.getString("Tabla");
                    if(tabla.equals("zonas")){
                        new HTTPRequest(server + "/mesas/lszonas", new ArrayList<NameValuePair>() , "zonas", controller_http);
                        new HTTPRequest(server + "/mesas/lstodaslasmesas", new ArrayList<NameValuePair>() , "mesas", controller_http);
                    }

                    if(tabla.equals("subteclas")){
                        new HTTPRequest(server+"/comandas/lssubteclas",new ArrayList<NameValuePair>(),"sub", controller_http);
                    }

                    if(tabla.equals("teclascom")){
                        new HTTPRequest(server+"/comandas/lsAll",new ArrayList<NameValuePair>(),"art", controller_http);
                        new HTTPRequest(server+"/comandas/lssecciones",new ArrayList<NameValuePair>(),"sec", controller_http);
                    }

                    if(tabla.equals("mesasabiertas")){
                        getPendientes();
                        new HTTPRequest(server + "/mesas/lsmesasabiertas", new ArrayList<NameValuePair>() , "m", controller_http);
                    }

                    if(tabla.equals("pendientes")){
                        getPendientes();
                    }
                }


            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler controller_http = new Handler() {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            Log.d("servicio", res);
            try {
                if(op.length()>0){
                    if (op.equals("autorizado")){
                        JSONObject aux = new JSONObject(res);
                        Boolean es_autorizado = aux.getBoolean("autorizado");
                        if (!es_autorizado){
                            exit_app();
                        }
                    }
                    else if (op.equals("zonas")) dbZonas.RellenarTabla(new JSONArray(res));
                    else if (op.equals("mesas")) dbMesas.RellenarTabla(new JSONArray(res));
                    else if (op.equals("sub")) dbSubTeclas.RellenarTabla(new JSONArray(res));
                    else if (op.equals("art")) dbTeclas.RellenarTabla(new JSONArray(res));
                    else if (op.equals("sec")) dbSecciones.RellenarTabla(new JSONArray(res));
                    else if (op.equals("sync")){
                        JSONObject aux = new JSONObject(res);
                        JSONArray tb = aux.getJSONArray("Tablas");
                        if(tb.length()>0){
                            for(int i=0;i<tb.length();i++){
                               String tabla = tb.getJSONObject(i).getString("Tabla");

                               if(tabla.equals("Zonas")){
                                   new HTTPRequest(server + "/mesas/lszonas", new ArrayList<NameValuePair>() , "zonas", controller_http);
                                   new HTTPRequest(server + "/mesas/lstodaslasmesas", new ArrayList<NameValuePair>() , "mesas", controller_http);
                               }

                               if(tabla.equals("SubTeclas")){
                                   new HTTPRequest(server+"/comandas/lssubteclas",new ArrayList<NameValuePair>(),"sub", controller_http);
                               }

                                if(tabla.equals("TeclasCom")){
                                    new HTTPRequest(server+"/comandas/lsAll",new ArrayList<NameValuePair>(),"art", controller_http);
                                    new HTTPRequest(server+"/comandas/lssecciones",new ArrayList<NameValuePair>(),"sec", controller_http);
                                }

                               if(tabla.equals("MesasAbiertas")){
                                   new HTTPRequest(server + "/mesas/lsmesasabiertas", new ArrayList<NameValuePair>() , "m", controller_http);
                               }
                            }
                        }


                    } else if (op.equals("m")) {
                       JSONArray datos = new JSONArray(res);
                       dbMesas.update(datos);
                       if(controller_zonas !=null) controller_zonas.sendEmptyMessage(0);
                    }else if (op.equals("pedir")){
                        if(res.trim().equals("success")) {
                            enviado = true;
                        }else{
                            enviado = true;
                            cola.add(pedido);
                        }
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };

    private void IniciarDB() {
      if(dbTeclas==null)  dbTeclas = new DbTeclas(getApplicationContext());
      if(dbZonas==null)  dbZonas = new DbZonas(getApplicationContext());
      if(dbMesas==null)  dbMesas = new DbMesas(getApplicationContext());
      if(dbSubTeclas==null)  dbSubTeclas = new DbSubTeclas(getApplicationContext());
      if(dbSecciones==null)  dbSecciones = new DbSecciones(getApplicationContext());
    }

    private void genrateMensajeNotification(String msg) {
        Message obj_msg = this.controller_mesas.obtainMessage();
        Bundle bundle = obj_msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("RESPONSE", msg);
        bundle.putString("op", "mensaje");
        obj_msg.setData(bundle);
        this.controller_mesas.sendMessage(obj_msg);
    }

    private  void getPendientes(){
        Message obj_msg = controller_mesas.obtainMessage();
        Bundle bundle = obj_msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("op", "pendientes");
        bundle.putString("RESPONSE", "");
        obj_msg.setData(bundle);
        controller_mesas.sendMessage(obj_msg);
    }


    private void exit_app(){
        if (ws_manager != null && ws_manager.opened){
            reconectar = false;
            ws_manager.close();
        }
        Message obj_msg = this.controller_mesas.obtainMessage();
        Bundle bundle = obj_msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("op", "exit");
        bundle.putString("RESPONSE", "");
        obj_msg.setData(bundle);
        this.controller_mesas.sendMessage(obj_msg);
    }


    public ServicioCom() {
        super("ValleCOM");
    }



    public void setHandleZonas(Handler controller) {
        this.controller_zonas = controller;
        this.controller_zonas.sendEmptyMessage(0);
    }

    public void setHandleMesas(Handler controller) {
        this.controller_mesas = controller;
    }


    public void encolar(List<NameValuePair> p) {
        cola.add(p);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        server = intent.getStringExtra("server");
        camarero_id = intent.getStringExtra("camarero_id");
        IniciarDB();
        return START_STICKY;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        IniciarDB();
        timerUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                if (server != null) {
                    ws_manager = new WSClient(server + "/ws/comunicacion/comandas", controller_WS);
                }

            }
        }, 200);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
              if((enviado && cola.size()>0)) {
                  pedido = cola.remove();
                  enviado = false;
                  new HTTPRequest(server + "/comandas/pedir", pedido, "pedir", controller_http);
              }
            }
        },0,500);
    }

    @Override
    public void onDestroy() {
        reconectar = false;
        if(timer!=null) timer.cancel();
        if(ws_manager != null && ws_manager.opened){
            ws_manager.close();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}
