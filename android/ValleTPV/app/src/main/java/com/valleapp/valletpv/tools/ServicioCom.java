package com.valleapp.valletpv.tools;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.valleapp.valletpv.db.DBSubTeclas;
import com.valleapp.valletpv.interfaces.IBaseDatos;
import com.valleapp.valletpv.db.DbCamareros;
import com.valleapp.valletpv.db.DbCuenta;
import com.valleapp.valletpv.db.DbMesas;
import com.valleapp.valletpv.db.DbMesasAbiertas;
import com.valleapp.valletpv.db.DbSecciones;
import com.valleapp.valletpv.db.DbTbUpdates;
import com.valleapp.valletpv.db.DbTeclas;
import com.valleapp.valletpv.db.DbZonas;
import com.valleapp.valletpv.tareas.TareaManejarInstrucciones;
import com.valleapp.valletpv.tareas.TareaUpdateForDevices;
import com.valleapp.valletpv.tareas.TareaUpdateFromDevices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class ServicioCom extends Service {


    final IBinder myBinder = new MyBinder();

    String server = null;

    Timer timerUpdateFast = new Timer();
    Timer timerUpdateLow = new Timer();
    Timer timerUpdateFromDevices = new Timer();
    Timer timerManejarInstrucciones = new Timer();

    Map<String, Handler> exHandler = new HashMap<>();
    Map<String, IBaseDatos> dbs;

    DbTbUpdates dbTbUpdates;

    Queue<RowsUpdatables> colaUpdate = new LinkedList<>();
    Queue<Instrucciones> colaInstrucciones = new LinkedList<>();

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
        Log.d("TEST", "Servicio startcommand");
        String url = intent.getStringExtra("url");
        if (url != null){
            server = url;
            IniciarDB();
            timerUpdateFromDevices.schedule(new TareaUpdateFromDevices(dbs, timerUpdateFromDevices, colaUpdate, server, dbTbUpdates), 5000, 1);
            timerManejarInstrucciones.schedule(new TareaManejarInstrucciones(timerManejarInstrucciones, colaInstrucciones, 1000), 2000, 1);
            //timerUpdateFast.schedule(new TareaUpdateForDevices(tbNameUpdateFast, server, controller_http, timerUpdateFast, 2000), 2000, 1);
            //timerUpdateLow.schedule(new TareaUpdateForDevices(tbNameUpdateLow, server, controller_http, timerUpdateLow, 20000), 2000, 1);
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
        timerUpdateFromDevices.cancel();
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
    public Handler getExHandler(String nombre){
        try {
            return exHandler.get(nombre);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return  null;
    }

    public void addTbCola(RowsUpdatables r){
        colaUpdate.add(r);
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
                    "secciones",
                    "subteclas"
            };

        }
        if (dbs == null){
            DbMesas dbMesas = new DbMesas(getApplicationContext());
            dbs = new HashMap<>();
            dbs.put("camareros", new DbCamareros(getApplicationContext()));
            dbs.put("mesas", dbMesas);
            dbs.put("zonas", new DbZonas(getApplicationContext()));
            dbs.put("secciones", new DbSecciones(getApplicationContext()));
            dbs.put("teclas", new DbTeclas(getApplicationContext()));
            dbs.put("lineaspedido", new DbCuenta(getApplicationContext()));
            dbs.put("mesasabiertas", new DbMesasAbiertas(dbMesas));
            dbs.put("subteclas", new DBSubTeclas(getApplicationContext()));
        }


        if(dbTbUpdates==null)  dbTbUpdates = new DbTbUpdates(getApplicationContext());
    }

    public IBaseDatos getDb(String nombre){
        return dbs.get(nombre);
    }

    public void abrirCajon() {
        if(server!=null) new HTTPRequest(server + "/impresion/abrircajon", new ContentValues(), "abrir_cajon", controller_http);
    }

    public void getLineasTicket(Handler mostrarLsTicket, String IDTicket) {
        ContentValues p = new ContentValues();
        p.put("id", IDTicket);
        new HTTPRequest(server + "/cuenta/lslineas", p, "get_lineas_ticket", mostrarLsTicket);
    }

    public void getListaTickets(Handler hLsTicket) {
        new HTTPRequest(server+"/cuenta/lsticket", new ContentValues(),"get_lista_ticket", hLsTicket);
    }

    public void imprimirTicket(String idTicket) {
        ContentValues p = new ContentValues();
        p.put("id", idTicket);
        p.put("abrircajon", "False");
        p.put("receptor_activo", "True");
        new HTTPRequest(server + "/impresion/imprimir_ticket", p, "", controller_http);
    }

    public void getSettings(Handler controller) {
        new HTTPRequest(server + "/receptores/get_lista", new ContentValues(), "get_lista_receptores", controller);
    }

    public void setSettings(String lista) {
        ContentValues p = new ContentValues();
        p.put("lista", lista);
        new HTTPRequest(server + "/receptores/set_settings", p, "set_settings", controller_http);
    }

    public void rmMesa(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server+"/cuenta/rm"));
        }
    }

    public void opMesas(ContentValues params, String op) {
        synchronized (colaInstrucciones){
            String url =  op == "juntarmesas" ? "/cuenta/juntarmesas" : "/cuenta/cambiarmesas";
            colaInstrucciones.add(new Instrucciones(params, server + url));
        }
    }

    public void rmLinea(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server+"/cuenta/rmlinea"));
        }
    }

    public void nuevoPedido(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server+"/cuenta/add"));
        }
    }

    public void cobrarCuenta(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server+"/cuenta/cobrar"));
        }
    }

    public void preImprimir(final ContentValues p) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                new HTTPRequest(server + "/impresion/preimprimir", p, "", controller_http);
            }
            }, 500);
         }


    public void get_cuenta(Handler controller, String mesa_id) {
        ContentValues p = new ContentValues();
        p.put("mesa_id", mesa_id);
        new HTTPRequest(server + "/cuenta/get_cuenta", p, "", controller);
    }

    public void addCamNuevo(String n, String a) {
        ContentValues p = new ContentValues();
        p.put("nombre", n);
        p.put("apellido", a);
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(p, server+"/camareros/camarero_add"));
        }
    }

    public void pedirAutorizacion(ContentValues p) {
        new HTTPRequest(server + "/autorizaciones/pedir_autorizacion", p, "", null);
    }

    public void sendMensaje(ContentValues p) {
        new HTTPRequest(server + "/autorizaciones/send_informacion", p, "", null);
    }

    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}