package com.valleapp.vallecom.utilidades;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.valleapp.vallecom.db.DBCamareros;
import com.valleapp.vallecom.db.DBCuenta;
import com.valleapp.vallecom.db.DBMesas;
import com.valleapp.vallecom.db.DBMesasAbiertas;
import com.valleapp.vallecom.db.DBReceptores;
import com.valleapp.vallecom.db.DBSecciones;
import com.valleapp.vallecom.db.DBSubTeclas;
import com.valleapp.vallecom.db.DBSugerencias;
import com.valleapp.vallecom.db.DBTeclas;
import com.valleapp.vallecom.db.DBZonas;
import com.valleapp.vallecom.db.DBTbUpdates;
import com.valleapp.vallecom.interfaces.IBaseDatos;
import com.valleapp.vallecom.interfaces.IBaseSocket;
import com.valleapp.vallecom.tareas.TareaManejarInstrucciones;
import com.valleapp.vallecom.tareas.TareaUpdateForDevices;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.net.URI;
import java.nio.ByteBuffer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class ServicioCom extends Service {


    final IBinder myBinder = new MyBinder();

    String server = null;

    Timer timerUpdate = new Timer();
    Timer timerManejarInstrucciones = new Timer();
    Timer checkWebsocket = new Timer();

    Map<String, Handler> exHandler = new HashMap<>();
    Map<String, IBaseDatos> dbs;

    DBTbUpdates DBTbUpdates;

    Queue<Instruccion> colaInstrucciones = new LinkedList<>();

    String[] tbNameUpdate;

    private JSONObject cam;
    WebSocketClient client;
    boolean isWebsocketClose = false;

    public boolean checkServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {

            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                Log.i("checkservice", serviceClass.getName()+", "+service.service.getClassName());
                stopService(new Intent(getApplicationContext(), serviceClass));
                return true;
            }
        }
        return false;
    }

    public void crearWebsocket() {
        super.onCreate();
        try {
            client = new WebSocketClient(new URI("ws://"+server.replace("api", "ws")+
                    "/comunicacion/devices")) {

                @Override
                public void onWebsocketPong(WebSocket conn, Framedata f) {
                    super.onWebsocketPong(conn, f);
                }

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    isWebsocketClose = false;
                    //comprobamos los datos cada vez que se conecte.
                    actualizarCamareros();
                    comprobarMesasAbiertas();
                    comprobarMensajes();
                    comprobarCuentas();
                 }


                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject o = new JSONObject(message);
                        updateTablesByWS(o);
                        DBCamareros db_cam = (DBCamareros) getDb("camareros");
                        if (cam != null && !db_cam.is_autorizado(cam)){
                            System.exit(0);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    // devolución de llamada por error de conexión
                    Log.i("Websockets", "Error de conexion .....");
                    isWebsocketClose = true;
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    // Devolución de llamada de conexión cerrada, si remoto es verdadero, significa que fue cortado por el servidor
                    Log.i("Websocket", "Websocket close....");
                    isWebsocketClose = true;
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    // El mensaje de flujo de bytes devuelto
                    Log.i("Websocket","socket bytebuffer bytes");
                }
            };

            client.connect();
        } catch (Exception ignored) {

        }
    }

    private void updateTablesByWS(JSONObject o){
        try {
            String tb = o.getString("tb");
            String op = o.getString("op");
            IBaseSocket db = (IBaseSocket) getDb(tb);
            if (db != null) {
                if (op.equals("insert")) db.insert(o.getJSONObject("obj"));
                if (op.equals("md")) db.update(o.getJSONObject("obj"));
                if (op.equals("rm")) db.rm(o.getJSONObject("obj"));
                Handler h = exHandler.get(tb);
                if (h != null){
                    Log.d("SEND_HANDLER", tb);
                    h.sendEmptyMessage(0);
                }
            } else if (op.equals("men")) {
                Handler h = exHandler.get(tb);
                if (h != null) {
                    new HTTPRequest().sendMessage(h, "men_once", o.getJSONObject("obj").toString());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private  void comprobarMesasAbiertas(){
        ContentValues p = new ContentValues();
        p.put("tb", "mesasabiertas");
        new HTTPRequest(server + "/sync/update_for_devices", p, "update_table", controller_http);
    }

    private void comprobarMensajes() {
        try {
            Handler h = exHandler.get("mensajes");
            if(cam != null && h != null) {
                ContentValues p = new ContentValues();
                p.put("idautorizado", cam.getString("ID"));
                new HTTPRequest(server + "/autorizaciones/get_lista_autorizaciones", p, "men_lista", h);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void comprobarCuentas(){
        try {
            ContentValues p = new ContentValues();
            DBCuenta dbCuenta = (DBCuenta) getDb("lineaspedido");
            JSONArray lineas = dbCuenta.execSql("SELECT * FROM cuenta ");
            p.put("lineas", lineas.toString());
            new HTTPRequest(server + "/pedidos/comparar_lineaspedido", p,
                    "update_socket", controller_http);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarCamareros(){
        ContentValues p = new ContentValues();
        p.put("tb", "camareros");
        new HTTPRequest(server + "/sync/update_for_devices", p, "update_table", controller_http);
    }

    private final Handler controller_http = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if (res != null) {
                try {
                    switch (op) {
                        case "check_updates":
                            delegadoHandleCheckUpdates(res);
                            break;
                        case "update_table":
                            delegadoHandleUpdateTable(res);
                            break;
                        case "update_socket":
                            JSONArray objs = new JSONArray(res);
                            for(int i= 0; i < objs.length(); i++) {
                                updateTablesByWS(objs.getJSONObject(i));
                            }
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            if (db!= null) {
                synchronized (DBTbUpdates) {
                    db.rellenarTabla(objs);
                    DBTbUpdates.upTabla(tb_name, last);
                    if (exHandler.containsKey(tb_name)) Objects.requireNonNull(exHandler.get(tb_name)).sendEmptyMessage(0);
                    if (tb_name.equalsIgnoreCase("camareros") && cam != null) {
                        DBCamareros dbCam = (DBCamareros) db;
                        JSONArray ls = dbCam.filter("ID=" + cam.getString("ID") + " AND autorizado = '1'");
                        if (ls.length() == 0) {
                            System.exit(0);
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void delegadoHandleCheckUpdates(String res) {
        try {
            synchronized (DBTbUpdates) {
                JSONObject obj = new JSONObject(res);
                String t = obj.getString("nombre");
                if (dbs.containsKey(t) && DBTbUpdates.is_updatable(obj)) {
                    ContentValues p = new ContentValues();
                    p.put("tb", t);
                    new HTTPRequest(server + "/sync/update_for_devices", p, "update_table", controller_http);
                }else{
                    if(t.equals("mesas")) Objects.requireNonNull(exHandler.get("mesas")).sendEmptyMessage(0);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkServiceRunning(ServicioCom.class);
        if (intent == null) return START_NOT_STICKY;
        String url = intent.getStringExtra("url");
        if (url != null){
            server = url;
            IniciarDB();
            crearWebsocket();
            timerManejarInstrucciones.schedule(new TareaManejarInstrucciones(timerManejarInstrucciones, colaInstrucciones, server, 1000), 2000, 1);
            checkWebsocket.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isWebsocketClose && client!= null){
                        client.reconnect();
                    }
                }
            }, 2000, 5000);
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
        try {
            timerUpdate.cancel();
            client.close();
        }catch (Exception ignored){

        }
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


    public void addColaInstrucciones(Instruccion inst) {
        synchronized (colaInstrucciones) {
            colaInstrucciones.add(inst);
        }
    }

    private void IniciarDB() {
        if (tbNameUpdate == null){
            tbNameUpdate = new String[]{
                    "zonas",
                    "mesas",
                    "teclas",
                    "subteclas",
                    "seccionescom",
                    "sugerencias",
                    "receptores"
            };

        }
        if (dbs == null){
            DBMesas dbMesas = new DBMesas(getApplicationContext());
            dbs = new HashMap<>();
            dbs.put("mesas", dbMesas);
            dbs.put("camareros", new DBCamareros(getApplicationContext()));
            dbs.put("zonas", new DBZonas(getApplicationContext()));
            dbs.put("seccionescom", new DBSecciones(getApplicationContext()));
            dbs.put("teclas", new DBTeclas(getApplicationContext()));
            dbs.put("lineaspedido", new DBCuenta(getApplicationContext()));
            dbs.put("subteclas", new DBSubTeclas(getApplicationContext()));
            dbs.put("sugerencias", new DBSugerencias(getApplicationContext()));
            dbs.put("mesasabiertas", new DBMesasAbiertas(dbMesas));
            dbs.put("receptores", new DBReceptores(getApplicationContext()));
            for (IBaseDatos db: dbs.values()){
                db.inicializar();
            }
        }

        if(DBTbUpdates ==null)  DBTbUpdates = new DBTbUpdates(getApplicationContext());
    }

    public IBaseDatos getDb(String nombre){
        return dbs.get(nombre);
    }

    public void setCamarero(JSONObject cam) {
        this.cam = cam;
        comprobarMensajes();
        timerUpdate.schedule(new TareaUpdateForDevices(this.tbNameUpdate, server, controller_http, timerUpdate, 2000), 1000);
    }

    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}