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

import com.valleapp.comandas.db.DBCamareros;
import com.valleapp.comandas.db.DBCuenta;
import com.valleapp.comandas.db.DBMesas;
import com.valleapp.comandas.db.DBMesasAbiertas;
import com.valleapp.comandas.db.DBReceptores;
import com.valleapp.comandas.db.DBSecciones;
import com.valleapp.comandas.db.DBSubTeclas;
import com.valleapp.comandas.db.DBSugerencias;
import com.valleapp.comandas.db.DBTeclas;
import com.valleapp.comandas.db.DBZonas;
import com.valleapp.comandas.db.DbTbUpdates;
import com.valleapp.comandas.interfaces.IBaseDatos;
import com.valleapp.comandas.tareas.TareaManejarAutorias;
import com.valleapp.comandas.tareas.TareaManejarInstrucciones;
import com.valleapp.comandas.tareas.TareaUpdateForDevices;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

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

    Timer timerUpdateFast = new Timer();
    Timer timerUpdateLow = new Timer();
    Timer timerManejarInstrucciones = new Timer();
    Timer timerAurotias = new Timer();
    Timer checkWebsocket = new Timer();

    Map<String, Handler> exHandler = new HashMap<>();
    Map<String, IBaseDatos> dbs;

    DbTbUpdates dbTbUpdates;

    Queue<Instruccion> colaInstrucciones = new LinkedList<>();

    String[] tbNameUpdateFast;
    String[] tbNameUpdateLow;

    private JSONObject cam;
    WebSocketClient client;
    boolean isWebsocketClose = false;


    public void crearWebsocket() {
        super.onCreate();
        try {
            client = new WebSocketClient(new URI("ws://"+server.replace("api", "ws")+
                    "/comunicacion/sync_devices")) {

                @Override
                public void onWebsocketPong(WebSocket conn, Framedata f) {
                    super.onWebsocketPong(conn, f);
                }

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    // Devolución de llamada después de que se abre la conexión
                    isWebsocketClose = false;
                    timerUpdateFast.schedule(new TareaUpdateForDevices(tbNameUpdateFast, server, controller_http, timerUpdateFast, 1000), 1000);
                    timerUpdateLow.schedule(new TareaUpdateForDevices(tbNameUpdateLow, server, controller_http, timerUpdateLow, 1000), 5000);
                }


                @Override
                public void onMessage(String message) {
                    try {
                        if (message.contains("sync_actualizar")) {
                            Timer t = new Timer();
                            t.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    delegadoHandleCheckUpdates(message);
                                }
                            }, 2000);

                        } else if (message.contains("mensajes")) {
                            JSONObject res = new JSONObject(message);
                            String id = res.getString("idreceptor");
                            if (id.equals(cam.getString("ID")) || id.equals("all") ){
                                Handler menHandler = exHandler.get("menHandler");

                                if (menHandler != null ) {
                                    ContentValues p = new ContentValues();
                                    p.put("idautorizado", cam.getString("ID"));
                                    new HTTPRequest(server + "/autorizaciones/get_lista_autorizaciones",
                                            p, "autorias", menHandler);
                                }
                            }
                        }
                    }catch (Exception e){}
                }

                @Override
                public void onError(Exception ex) {
                    // devolución de llamada por error de conexión
                    Log.i("Websocket", "Error de conexion .....");
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
                synchronized (dbTbUpdates) {
                    db.rellenarTabla(objs);
                    dbTbUpdates.upTabla(tb_name, last);
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
            synchronized (dbTbUpdates) {
                JSONObject obj = new JSONObject(res);
                String t = obj.getString("nombre");
                if (dbs.containsKey(t) && dbTbUpdates.is_updatable(obj)) {
                    ContentValues p = new ContentValues();
                    p.put("tb", t);
                    new HTTPRequest(server + "/sync/update_for_devices", p, "update_table", controller_http);
                }
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
            timerUpdateFast.cancel();
            timerUpdateLow.cancel();
            timerAurotias.cancel();
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

    public void initTimerAutorias(Handler mainHandler, String idautoria){
       exHandler.put("menHandler", mainHandler);
       timerAurotias.schedule(new TareaManejarAutorias(mainHandler, idautoria, 1000, server+"/autorizaciones/get_lista_autorizaciones"), 2000);
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
            dbs.put("secciones_com", new DBSecciones(getApplicationContext()));
            dbs.put("teclas", new DBTeclas(getApplicationContext()));
            dbs.put("lineaspedido", new DBCuenta(getApplicationContext()));
            dbs.put("subteclas", new DBSubTeclas(getApplicationContext()));
            dbs.put("sugerencias", new DBSugerencias(getApplicationContext()));
            dbs.put("mesasabiertas", new DBMesasAbiertas(dbMesas));
            dbs.put("receptores", new DBReceptores(getApplicationContext()));
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

    public void setCamarero(JSONObject cam) {
        this.cam = cam;
    }

    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}