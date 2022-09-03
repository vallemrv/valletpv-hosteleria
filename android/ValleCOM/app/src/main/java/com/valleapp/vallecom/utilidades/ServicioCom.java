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

    final Queue<Instruccion> colaInstrucciones = new LinkedList<>();

    String[] tbNameUpdate;

    private JSONObject cam;
    WebSocketClient client;
    boolean isWebsocketClose = false;

    public void checkServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                Log.i("checkservice", serviceClass.getName()+", "+service.service.getClassName());
                stopService(new Intent(getApplicationContext(), serviceClass));
            }
        }
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
                    sync_device(new String[]{"mesasabiertas", "lineaspedido"});
                    comprobarCamareros();
                    comprobarMensajes();
                    syncDb();
                }


                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject o = new JSONObject(message);
                        updateTables(o);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    // devolución de llamada por error de conexión
                    Log.i("WEBSOCKET_INFO", "Error de conexion .....");
                    isWebsocketClose = true;
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    // Devolución de llamada de conexión cerrada, si remoto es verdadero, significa que fue cortado por el servidor
                    Log.i("WEBSOCKET_INFO", "Websocket close....");
                    isWebsocketClose = true;
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    // El mensaje de flujo de bytes devuelto
                    Log.i("WEBSOCKET_INFO","socket bytebuffer bytes");
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sync_device(String[] tbs) {

        try {
            for(String tb: tbs) {
                ContentValues p = new ContentValues();
                IBaseDatos db =  getDb(tb);
                p.put("tb", tb);
                p.put("reg", db.filter(null).toString());
                new HTTPRequest(server + "/sync/sync_devices", p, "update_socket", controller_http);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void comprobarCamareros(){
        new HTTPRequest(server+"/camareros/listado",
                new ContentValues(),
                "camareros", controller_http);
    }

    private void updateTables(JSONObject o){
        try {
            String tb = o.getString("tb");
            String op = o.getString("op");
            IBaseSocket db = (IBaseSocket) getDb(tb);
            if (db != null) {
                JSONArray objs = new JSONArray();
                try {
                    JSONObject obj = o.getJSONObject("obj");
                    objs.put(obj);
                }catch (JSONException ignored){
                    objs = o.getJSONArray("obj");
                }

                for(int i=0; i<objs.length();i++) {
                    JSONObject obj = objs.getJSONObject(i);
                    Log.d("UPDATETEBLES", "tb_name: "+tb+", op: "+op+", reg: "+obj.toString());
                    if (op.equals("insert")) db.insert(obj);
                    if (op.equals("md")) db.update(obj);
                    if (op.equals("rm")) db.rm(obj);
                }
                Handler h = exHandler.get(tb);
                if (h != null){
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


    private final Handler controller_http = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if (res != null) {
                try {
                    if ("update_socket".equals(op)) {
                        JSONArray objs = new JSONArray(res);
                        for (int i = 0; i < objs.length(); i++) {
                            updateTables(objs.getJSONObject(i));
                        }
                    }else if (op.equals("camareros")) {
                        DBCamareros db = (DBCamareros) getDb("camareros");
                        db.rellenarTabla(new JSONArray(res));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkServiceRunning(ServicioCom.class);
        if (intent == null) return START_NOT_STICKY;
        String url = intent.getStringExtra("url");
        if (url != null){
            server = url;
            IniciarDB();
            crearWebsocket();
            timerManejarInstrucciones.schedule(
                    new TareaManejarInstrucciones(timerManejarInstrucciones,
                                                  colaInstrucciones,
                                                  server, 1000,
                                                  exHandler), 2000, 1);
            checkWebsocket.schedule(new TimerTask() {
                @Override
                public void run() {
                    DBCamareros db_cam = (DBCamareros) getDb("camareros");
                    if (cam != null && !db_cam.is_autorizado(cam)){
                        System.exit(0);
                    }
                   if (isWebsocketClose && client!= null){
                        client.reconnect();
                        Handler h = exHandler.get("estadows");
                        if(h!=null) {
                            HTTPRequest http = new HTTPRequest();
                            http.sendMessage(h, "estadows", "WS Desconectado");
                        }
                    }else{
                        Handler h = exHandler.get("estadows");
                        if(h != null) {
                            HTTPRequest http = new HTTPRequest();
                            http.sendMessage(h, "estadows", "WS Conectado");
                        }
                    }
                }
            }, 2000, 1500);
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
        }catch (Exception e){
            e.printStackTrace();
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
        if (exHandler.containsKey(handlerName)) {
          exHandler.remove(exHandler.get(handlerName));
        }
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
                    "receptores",
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
            dbs.put("mesasabiertas", new DBMesasAbiertas(getApplicationContext()));
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
    }

    private void syncDb() {
        timerUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                sync_device(tbNameUpdate);
            }
        },2000);
    }

    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}