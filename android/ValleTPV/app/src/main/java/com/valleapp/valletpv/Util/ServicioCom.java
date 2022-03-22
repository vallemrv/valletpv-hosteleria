package com.valleapp.valletpv.Util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;

import com.valleapp.valletpv.db.DbCamareros;
import com.valleapp.valletpv.db.DbCuenta;
import com.valleapp.valletpv.db.DbMesas;
import com.valleapp.valletpv.db.DbSecciones;
import com.valleapp.valletpv.db.DbTeclas;
import com.valleapp.valletpv.db.DbZonas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class ServicioCom extends Service {

    public static boolean pasa = false;

    //Variables para conexion de WS
    boolean reconectar = true;
    WSClient ws_manager;


    final IBinder myBinder = new MyBinder();
    DbCamareros dbCamareros ;
    DbZonas dbZonas;
    DbMesas dbMesas;
    DbSecciones dbSecciones;
    DbTeclas dbTeclas;
    DbCuenta dbCuenta;

    String server = null;

    private Handler controller_mesas;
    private Handler controller_camareros;

    Timer timerUpdate = new Timer();
    Timer timerECO = new Timer();
    boolean eco = true;
    boolean hay_conexion = true;
    String code = "";
    JSONObject info_cobro = null;

    @SuppressLint("HandlerLeak")
    private  final Handler controller_WS = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            String res = msg.getData().getString("RESPONSE");

            try{
                JSONObject aux = new JSONObject(res);

                String op = aux.getString("OP");
                Log.d("cagada", String.format("OP: %s, Response: %s ", op, res));

                switch (op) {
                    case "OPENED":
                        ContentValues p = new ContentValues();
                        p.put("hora", "");
                        new HTTPRequest(server + "/sync/getupdate", p, "sync", controller_http);
                        break;
                    case "CLOSED":
                        if (server != null && !server.equals("") && reconectar) {
                            ws_manager = new WSClient(server + "/ws/comunicacion/comandas", controller_WS);
                        }
                        break;
                    case "MENSAJE":
                        if (controller_mesas != null) {
                            genrateMensajeNotification(aux.getString("msg"));
                        }
                        break;
                    case "UPDATE":
                        String tabla = aux.getString("Tabla");
                        switch (tabla) {
                            case "camareros":
                                new HTTPRequest(server + "/camareros/listado_activos", new ContentValues(), "cam", controller_http);
                                break;
                            case "zonas":
                                new HTTPRequest(server + "/mesas/lszonas", new ContentValues(), "zonas", controller_http);
                                new HTTPRequest(server + "/mesas/lstodaslasmesas", new ContentValues(), "mesas", controller_http);
                                break;
                            case "secciones":
                                new HTTPRequest(server + "/secciones/listado", new ContentValues(), "sec", controller_http);
                                new HTTPRequest(server + "/articulos/lstodos", new ContentValues(), "art", controller_http);
                                break;
                            case "mesasabiertas":
                                new HTTPRequest(server + "/mesas/lsmesasabiertas", new ContentValues(), "m", controller_http);
                                new HTTPRequest(server + "/cuenta/lsaparcadas", new ContentValues(), "cuenta", controller_http);
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + tabla);
                        }

                        break;
                    case "ECO_ECO":
                        String code_res = aux.getString("code");
                        eco = code.equals(code_res);
                        break;

                }



            }catch (JSONException e){
                e.printStackTrace();
                Log.d("cagada", String.format(" Response: %s ",  res));

            }
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler controller_http = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            Log.d("cagada", String.format("OP: %s, Response: %s ", op, res));

            try {
                if(op.length()>0){
                    if (op.equals("cam")){
                        dbCamareros.Vaciar();
                        dbCamareros.RellenarTabla(new JSONArray(res));
                        if (controller_camareros != null) controller_camareros.sendEmptyMessage(0);
                    }
                    else if (op.equals("zonas")){
                        dbZonas.RellenarTabla(new JSONArray(res));
                    }
                    else if (op.equals("mesas")){
                        dbMesas.RellenarTabla(new JSONArray(res));
                    }
                    else if (op.equals("sec")){
                        dbSecciones.RellenarTabla(new JSONArray(res));
                    }
                    else if (op.equals("art")){
                        dbTeclas.RellenarTabla(new JSONArray(res));
                    }
                    else if (op.equals("cuenta")) {
                        dbCuenta.RellenarTabla(new JSONArray(res));
                    }
                    else if (op.equals("hay_conexion")){
                        hay_conexion = true;
                    }
                    else if (op.equals("error")) {
                        if (res.equals("cobrar") && info_cobro != null) {
                            Message obj_msg = controller_mesas.obtainMessage();
                            Bundle bundle = obj_msg.getData();
                            if (bundle == null) bundle = new Bundle();
                            bundle.putString("RESPONSE", info_cobro.toString());
                            bundle.putString("op", "cobrar");
                            obj_msg.setData(bundle);
                            controller_mesas.sendMessage(obj_msg);
                        }
                    }
                    else if (op.equals("cobrar")) {
                        Message obj_msg = controller_mesas.obtainMessage();
                        Bundle bundle = obj_msg.getData();
                        if (bundle == null) bundle = new Bundle();
                        bundle.putString("RESPONSE", res);
                        bundle.putString("op", op);
                        obj_msg.setData(bundle);
                        controller_mesas.sendMessage(obj_msg);
                    }

                    else if (op.equals("sync") && !ServicioCom.pasa){
                        JSONObject aux = new JSONObject(res);
                        JSONArray tb = aux.getJSONArray("Tablas");
                        if(tb.length()>0){
                            for(int i=0;i<tb.length();i++){
                               String tabla = tb.getJSONObject(i).getString("Tabla");
                                switch (tabla) {
                                    case "Camareros":
                                        new HTTPRequest(server + "/camareros/listado_activos", new ContentValues(), "cam", controller_http);
                                        break;
                                    case "Zonas":
                                        new HTTPRequest(server + "/mesas/lszonas", new ContentValues(), "zonas", controller_http);
                                        new HTTPRequest(server + "/mesas/lstodaslasmesas", new ContentValues(), "mesas", controller_http);
                                        break;
                                    case "Secciones":
                                        new HTTPRequest(server + "/secciones/listado", new ContentValues(), "sec", controller_http);
                                        new HTTPRequest(server + "/articulos/lstodos", new ContentValues(), "art", controller_http);
                                        break;
                                    case "MesasAbiertas":
                                        new HTTPRequest(server + "/mesas/lsmesasabiertas", new ContentValues(), "m", controller_http);
                                        new HTTPRequest(server + "/cuenta/lsaparcadas", new ContentValues(), "cuenta", controller_http);
                                        break;

                                }
                            }
                        }


                    } else if (op.equals("m")) {
                       JSONArray datos = new JSONArray(res);
                       dbMesas.update(datos);
                       if (controller_mesas != null) controller_mesas.sendEmptyMessage(0);
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    };

     public ServicioCom() {

    }

    private void IniciarDB() {
      if(dbSecciones==null)  dbSecciones = new DbSecciones(getApplicationContext());
      if(dbTeclas==null)  dbTeclas = new DbTeclas(getApplicationContext());
      if(dbZonas==null)  dbZonas = new DbZonas(getApplicationContext());
      if(dbCamareros==null)  dbCamareros = new DbCamareros(getApplicationContext());
      if(dbMesas==null)  dbMesas = new DbMesas(getApplicationContext());
      if(dbCuenta==null)  dbCuenta = new DbCuenta(getApplicationContext());
    }

    private void genrateMensajeNotification(String msg) {
        Message obj_msg = this.controller_mesas.obtainMessage();
        Bundle bundle = obj_msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("mensaje", msg);
        bundle.putString("op", "mensaje");
        obj_msg.setData(bundle);
        this.controller_mesas.sendMessage(obj_msg);
    }


    public void Reconectar(){
        Log.d("cagada", Boolean.toString(ws_manager.opened));
        if(ws_manager != null){
            ws_manager.close();
        }
    }

    public void AbrirCajon() {
        if(server!=null) new HTTPRequest(server + "/impresion/abrircajon", new ContentValues(), "abrir_cajon", controller_http);
    }



    public void getTicket(Handler mostrarLsTicket, String IDTicket) {
        ContentValues p = new ContentValues();
        p.put("id", IDTicket);
        new HTTPRequest(server + "/cuenta/lslineas", p, "ticket", mostrarLsTicket);
    }

    public void getLsTicket(Handler hLsTicket) {
        new HTTPRequest(server+"/cuenta/lsticket", new ContentValues(),"lsticket", hLsTicket);
    }

    public void imprimirTicket(String idTicket) {
        ContentValues p = new ContentValues();
        p.put("id", idTicket);
        p.put("abrircajon", "False");
        p.put("receptor_activo", "True");
        new HTTPRequest(server + "/impresion/imprimir_ticket", p, "", controller_http);
    }

    public void rmMesa(ContentValues p) {
        new HTTPRequest(server+"/cuenta/rm", p ,"", controller_http);
    }

    public void nuevoPedido(ContentValues obj)
    {
        new HTTPRequest(server+"/cuenta/add", obj ,"", controller_http);
    }

    public void cobrarCuenta(ContentValues obj, JSONObject info)
    {
        info_cobro = info;
        new HTTPRequest(server+"/cuenta/cobrar", obj ,"cobrar", controller_http);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String url = intent.getStringExtra("url");
        if (url != null) server = url;
        IniciarDB();
        return START_STICKY;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        timerUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                if (server != null && !server.equals("")) {
                    ws_manager = new WSClient(server + "/ws/comunicacion/comandas", controller_WS);
                }
            }
        }, 200);

        timerECO.schedule(new TimerTask() {
            @Override
            public void run() {
                if (eco || !hay_conexion) {
                    eco = false;
                    hay_conexion = false;
                    Date d = new Date();
                    code = DateFormat.format("yyyyMMddhhmmss", d.getTime()).toString();
                    ContentValues p = new ContentValues();
                    p.put("code", code);
                    new HTTPRequest(server + "/sync/get_eco", p, "hay_conexion", controller_http);
                }else {
                    if(ws_manager != null && ws_manager.opened){
                        ws_manager.close();
                       eco = true;
                    }
                }
            }
        }, 1000, 30000);

    }

    @Override
    public void onDestroy() {
        reconectar = false;
        if(ws_manager != null && ws_manager.opened){
            ws_manager.close();
            timerECO.cancel();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;

    }


    public void setHandleMesas(Handler handler) {
        this.controller_mesas = handler;
        this.controller_mesas.sendEmptyMessage(0);
    }

    public void setHandleCamareros(Handler handler) {
        this.controller_camareros = handler;
    }

    public void PreImprimir(final ContentValues p) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                new HTTPRequest(server + "/impresion/preimprimir", p, "", controller_http);
            }
        }, 500);
         }

    public void opMesas(ContentValues p, String url) {
        new HTTPRequest(url, p, "", controller_http);
    }

    public void rmLinea(ContentValues p) {
        new HTTPRequest(server+"/cuenta/rmlinea", p ,"", controller_http);
    }

    public void set_lista_autorizados(Handler controller, String lista, String camarero) {
        ContentValues p = new ContentValues();
        p.put("lista", lista);
        p.put("camarero", camarero);
        new HTTPRequest(server + "/camareros/sel_camareros", p, "autorizar", controller);

    }

    public void crear_pass(Handler controller, String cam, String pass) {
        ContentValues p = new ContentValues();
        p.put("cam", cam);
        p.put("password", pass);
        new HTTPRequest(server + "/camareros/crear_password", p, "password", controller);

    }

    public void getLsSettings(Handler controller) {
        new HTTPRequest(server + "/receptores/get_lista", new ContentValues(), "get_lista", controller);

    }

    public void set_lista_settings(Handler controller, String lista) {
        ContentValues p = new ContentValues();
        p.put("lista", lista);
        new HTTPRequest(server + "/receptores/set_settings", p, "set_lista", controller);
    }

    public void get_cuenta(Handler controller, String mesa_id) {
        ContentValues p = new ContentValues();
        p.put("mesa_id", mesa_id);
        new HTTPRequest(server + "/cuenta/get_cuenta", p, "", controller);
    }


    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}