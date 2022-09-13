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

import com.valleapp.valletpv.db.DBCamareros;
import com.valleapp.valletpv.db.DBCuenta;
import com.valleapp.valletpv.db.DBMesas;
import com.valleapp.valletpv.db.DBMesasAbiertas;
import com.valleapp.valletpv.db.DBSecciones;
import com.valleapp.valletpv.db.DBSubTeclas;
import com.valleapp.valletpv.db.DBTeclas;
import com.valleapp.valletpv.db.DBZonas;
import com.valleapp.valletpv.interfaces.IBaseDatos;
import com.valleapp.valletpv.interfaces.IBaseSocket;
import com.valleapp.valletpv.tareas.TareaManejarInstrucciones;

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
    private JSONObject zn;
    private JSONObject mesa_abierta;

    String server = null;

    Timer timerUpdateLow = new Timer();
    Timer timerManejarInstrucciones = new Timer();
    Timer checkWebsocket = new Timer();


    Map<String, Handler> exHandler = new HashMap<>();
    Map<String, IBaseDatos> dbs;

    final Queue<Instrucciones> colaInstrucciones = new LinkedList<>();
    String[] tbNameUpdateLow;

    WebSocketClient client;
    boolean isWebsocketClose = false;

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
                    // Devolución de llamada después de que se abre la conexión
                    isWebsocketClose = false;
                    Log.i("WEBSOCKET_INFO", "Websocket open.....");
                    sync_device(new String[]{"mesasabiertas", "lineaspedido", "camareros"}, 500);
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
                    Log.i("Websocket", "Error de conexion....");
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


    private void sync_device(String[] tbs, long timeout) {

        try {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run(){
                    for(String tb: tbs) {
                        ContentValues p = new ContentValues();
                        IBaseDatos db =  getDb(tb);
                        p.put("tb", tb);
                        p.put("reg", db.filter(null).toString());
                        new HTTPRequest(server + "/sync/sync_devices", p, "update_socket", controller_http);
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 50);


        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    if (op.equals("insert")) db.insert(obj);
                    if (op.equals("md")) db.update(obj);
                    if (op.equals("rm")) db.rm(obj);
                }

                Handler h = getExHandler(tb);
                if (h != null){
                    if (tb.equals("lineaspedido") && !op.equals("rm") && mesa_abierta != null){
                        String obj_idmesa = o.getJSONObject("obj").getString("IDMesa");
                        String id_mesa_abierta = mesa_abierta.getString("ID");
                        if (!obj_idmesa.equals(id_mesa_abierta)) return;

                    }
                    h.sendEmptyMessage(0);
                }
            }

        }catch (Exception e){
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
            timerUpdateLow.schedule(new TimerTask() {
                @Override
                public void run() {
                    sync_device(tbNameUpdateLow, 1000);
                }
            },1000, 290000);
            timerManejarInstrucciones.schedule(
                    new TareaManejarInstrucciones(colaInstrucciones, 1000), 2000, 1);
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
        timerUpdateLow.cancel();
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

    private void IniciarDB() {
        if (tbNameUpdateLow == null){
            tbNameUpdateLow = new String[]{
                    "camareros",
                    "zonas",
                    "mesas",
                    "teclas",
                    "secciones",
                    "subteclas"
            };
        }
        if (dbs == null){
            DBMesas dbMesas = new DBMesas(getApplicationContext());
            dbs = new HashMap<>();
            dbs.put("camareros", new DBCamareros(getApplicationContext()));
            dbs.put("mesas", dbMesas);
            dbs.put("zonas", new DBZonas(getApplicationContext()));
            dbs.put("secciones", new DBSecciones(getApplicationContext()));
            dbs.put("teclas", new DBTeclas(getApplicationContext()));
            dbs.put("lineaspedido", new DBCuenta(getApplicationContext()));
            dbs.put("mesasabiertas", new DBMesasAbiertas(getApplicationContext()));
            dbs.put("subteclas", new DBSubTeclas(getApplicationContext()));
            for (IBaseDatos db: dbs.values()){
                db.inicializar();
            }
        }

    }

    public IBaseDatos getDb(String nombre){
        return dbs.get(nombre);
    }

    public void abrirCajon() {
        if(server!=null) new HTTPRequest(server + "/impresion/abrircajon",
                new ContentValues(), "abrir_cajon", controller_http);
    }

    public void getLineasTicket(Handler mostrarLsTicket, String IDTicket) {
        ContentValues p = new ContentValues();
        p.put("id", IDTicket);
        new HTTPRequest(server + "/cuenta/lslineas", p,
                "get_lineas_ticket", mostrarLsTicket);
    }

    public void getListaTickets(Handler hLsTicket) {
        new HTTPRequest(server+"/cuenta/lsticket", new ContentValues(),
                "get_lista_ticket", hLsTicket);
    }

    public void imprimirTicket(String idTicket) {
        ContentValues p = new ContentValues();
        p.put("id", idTicket);
        p.put("abrircajon", "False");
        p.put("receptor_activo", "True");
        new HTTPRequest(server + "/impresion/imprimir_ticket", p, "", controller_http);
    }

    public void imprimirFactura(String idTicket) {
        ContentValues p = new ContentValues();
        p.put("id", idTicket);
        p.put("abrircajon", "False");
        p.put("receptor_activo", "True");
        new HTTPRequest(server + "/impresion/imprimir_factura", p, "", controller_http);
    }

    public void getSettings(Handler controller) {
        new HTTPRequest(server + "/receptores/get_lista", new ContentValues(),
                "get_lista_receptores", controller);
    }

    public void setSettings(String lista) {
        ContentValues p = new ContentValues();
        p.put("lista", lista);
        new HTTPRequest(server + "/receptores/set_settings", p,
                "set_settings", controller_http);
    }

    public void rmMesa(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server+"/cuenta/rm"));
        }
    }

    public void opMesas(ContentValues params, String op) {
        synchronized (colaInstrucciones){
            String url = Objects.equals(op, "juntarmesas") ? "/cuenta/juntarmesas" : "/cuenta/cambiarmesas";
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
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(p, server+"/impresion/preimprimir"));
        }
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

    public void autorizarCam(JSONObject obj){
        ContentValues p = new ContentValues();
        JSONArray o = new JSONArray();
        o.put(obj);
        p.put("rows", o.toString());
        p.put("tb", "camareros");
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(p, server+"/sync/update_from_devices"));
        }
    }

    public void pedirAutorizacion(ContentValues p) {
       new HTTPRequest(server + "/autorizaciones/pedir_autorizacion",
               p, "", null);
    }

    public void sendMensaje(ContentValues p) {
        new HTTPRequest(server + "/autorizaciones/send_informacion",
                p, "", null);
    }

    public JSONObject getZona() {
        return zn;
    }

    public void setZona(JSONObject zn){
        this.zn = zn;
    }

    public void setMesa_abierta(JSONObject m){
        this.mesa_abierta = m;
    }

    public class MyBinder extends Binder{
       public ServicioCom getService() {
            return ServicioCom.this;
       }
    }

}