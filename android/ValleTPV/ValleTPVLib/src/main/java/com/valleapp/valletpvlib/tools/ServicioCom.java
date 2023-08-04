package com.valleapp.valletpvlib.tools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.core.app.NotificationCompat;


import com.valleapp.valletpvlib.db.DBCamareros;
import com.valleapp.valletpvlib.db.DBCuenta;
import com.valleapp.valletpvlib.db.DBMesas;
import com.valleapp.valletpvlib.db.DBMesasAbiertas;
import com.valleapp.valletpvlib.db.DBSecciones;
import com.valleapp.valletpvlib.db.DBSubTeclas;
import com.valleapp.valletpvlib.db.DBTeclas;
import com.valleapp.valletpvlib.db.DBZonas;
import com.valleapp.valletpvlib.interfaces.IBaseDatos;
import com.valleapp.valletpvlib.interfaces.IBaseSocket;
import com.valleapp.valletpvlib.interfaces.IController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class ServicioCom extends Service implements IController {


    private static final int NOTIFICATION_ID = 10001;
    private static final String CHANNEL_ID = "com.valleapp.valletpv";
    final IBinder myBinder = new MyBinder();
    private JSONObject zn;
    private JSONObject mesa_abierta;

    Timer timerUpdateLow = new Timer();
    Timer timerManejarInstrucciones = new Timer();


    Map<String, Handler> exHandler = new HashMap<>();
    Map<String, IBaseDatos> dbs;

    final Queue<Instrucciones> colaInstrucciones = new LinkedList<>();
    String[] tbNameUpdateLow;

    WSClient client;
    ServerConfig server;

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

    public void sync_device(String[] tbs, long timeout) {

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



    public void updateTables(JSONObject o){
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

    private Notification getNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ValleTPV",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ValleTPV")
                .setContentText("Listo para recibir pedidos");

        return builder.build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Comenzar el servicio en primer plano.
        startForeground(NOTIFICATION_ID, this.getNotification());
        String server_config = intent.getStringExtra("server_config");
        if (server_config == null) {
            server = ServerConfig.loadJSON(server_config);
        }

        // Si el sistema mata este servicio, una vez que haya suficientes recursos, lo reiniciará.
        return START_STICKY;
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
        ContentValues p = server.getParams();
        p.put("id", IDTicket);
        new HTTPRequest(server.getUrl("/cuenta/lslineas") , p,
                "get_lineas_ticket", mostrarLsTicket);
    }

    public void getListaTickets(Handler hLsTicket) {
        new HTTPRequest(server.getUrl("/cuenta/lsticket"), server.getParams(),
                "get_lista_ticket", hLsTicket);
    }

    public void imprimirTicket(String idTicket) {
        ContentValues p = server.getParams();
        p.put("id", idTicket);
        p.put("abrircajon", "False");
        p.put("receptor_activo", "True");
        new HTTPRequest(server.getUrl ("/impresion/imprimir_ticket"), p, "", controller_http);
    }

    public void imprimirFactura(String idTicket) {
        ContentValues p = server.getParams();
        p.put("id", idTicket);
        p.put("abrircajon", "False");
        p.put("receptor_activo", "True");
        new HTTPRequest(server.getUrl("/impresion/imprimir_factura"), p, "", controller_http);
    }

    public void getSettings(Handler controller) {
        new HTTPRequest(server.getUrl(""), server.getParams(),
                "get_lista_receptores", controller);
    }

    public void setSettings(String lista) {
        ContentValues p = server.getParams();
        p.put("lista", lista);
        new HTTPRequest(server.getUrl( "/receptores/set_settings"), p,
                "set_settings", controller_http);
    }

    public void rmMesa(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server.getUrl("/cuenta/rm")));
        }
    }

    public void opMesas(ContentValues params, String op) {
        synchronized (colaInstrucciones){
            String url = Objects.equals(op, "juntarmesas") ? "/cuenta/juntarmesas" : "/cuenta/cambiarmesas";
            colaInstrucciones.add(new Instrucciones(params, server.getUrl(url)));
        }
    }

    public void rmLinea(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server.getUrl("/cuenta/rmlinea")));
        }
    }

    public void nuevoPedido(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server.getUrl("/cuenta/add")));
        }
    }

    public void cobrarCuenta(ContentValues params) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(params, server.getUrl("/cuenta/cobrar")));
        }
    }

    public void preImprimir(final ContentValues p) {
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(p, server.getUrl("/impresion/preimprimir")));
        }
    }

    public void get_cuenta(Handler controller, String mesa_id) {
        ContentValues p = server.getParams();
        p.put("mesa_id", mesa_id);
        new HTTPRequest(server.getUrl("/cuenta/get_cuenta"), p, "", controller);
    }

    public void addCamNuevo(String n, String a) {
        ContentValues p =server.getParams();
        p.put("nombre", n);
        p.put("apellido", a);
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(p, server.getUrl("/camareros/camarero_add")));
        }
    }

    public void autorizarCam(JSONObject obj){
        ContentValues p = server.getParams();
        JSONArray o = new JSONArray();
        o.put(obj);
        p.put("rows", o.toString());
        p.put("tb", "camareros");
        synchronized (colaInstrucciones){
            colaInstrucciones.add(new Instrucciones(p, server.getUrl("/sync/update_from_devices")));
        }
    }

    public void pedirAutorizacion(ContentValues p) {
       new HTTPRequest(server.getUrl("/autorizaciones/pedir_autorizacion"),
               p, "", null);
    }

    public void sendMensaje(ContentValues p) {
        new HTTPRequest(server.getUrl("/autorizaciones/send_informacion"),
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