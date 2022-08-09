package com.valleapp.vallecom.Activitys;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.valleapp.vallecom.R;
import com.valleapp.vallecom.adaptadores.AdaptadorMesas;
import com.valleapp.vallecom.db.DBCuenta;
import com.valleapp.vallecom.db.DBMesas;
import com.valleapp.vallecom.db.DBReceptores;
import com.valleapp.vallecom.db.DBZonas;
import com.valleapp.vallecom.interfaces.IPedidos;
import com.valleapp.vallecom.pestañas.ListaMesas;
import com.valleapp.vallecom.pestañas.Pedidos;
import com.valleapp.vallecom.utilidades.ActivityBase;
import com.valleapp.vallecom.utilidades.Instruccion;
import com.valleapp.vallecom.utilidades.JSON;
import com.valleapp.vallecom.utilidades.ServicioCom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class Mesas extends ActivityBase implements View.OnLongClickListener, IPedidos {

    private ListaMesas listaMesas = null;
    private Pedidos pedidos = null;

    int presBack = 0;

    DBMesas dbMesas;
    DBZonas dbZonas;
    DBCuenta dbCuenta;
    DBReceptores dbReceptores;

    JSONObject cam = null;
    JSONObject zn = null;

    JSONArray peticiones = new JSONArray();
    ArrayList<JSONObject> receptores;
    boolean viendo_mensajes = false;
    LinearLayout botonesReceptores ;

    private int sel_receptor = 0;
    private boolean all_cam = false;


    private final Handler handlerMesas = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            rellenarZonas();
        }
    };

    private final Handler handlerEstadoWS = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            try {
                rellenarEstadoWS(op, res);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    private final Handler handlerPedidos = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            rellenarPedido(sel_receptor, all_cam);
        }
    };

    private final Handler handlerMensajes = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            try {

                if (op.equals("men_once")){

                    JSONObject o = new JSONObject(res);
                    String idautorizado = o.getString("idautorizado");
                    String self_id = cam.getString("ID");
                    if (idautorizado.equals(self_id)) {
                        peticiones.put(o);
                    }
                }else {
                    if (res!=null)  peticiones = new JSONArray(res);
                }
                if(!viendo_mensajes && peticiones.length() > 0) {
                    MediaPlayer m = MediaPlayer.create(cx, R.raw.mail);
                    m.start();
                }else{
                    mostrarAutorias(null);
                }
                manejarAurotias();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private final Handler handlerOperaciones = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            if (op == null) op = "";
            switch (op) {
                case "exit":
                    finish();
                    break;
                case "servido":
                    mostrarToast("Articulos servidos");
                    break;
                case "reenviar":
                    mostrarToast("Peticion enviadaaa");
                    break;
                default:
                    findViewById(R.id.loading).setVisibility(View.GONE);
            }
        }
    };


    private void manejarAurotias() {
           View v = findViewById(R.id.show_autorias);
           int numPeticiones = peticiones.length();
            if(numPeticiones>0) {
                v.setVisibility(View.VISIBLE);
                TextView t = findViewById(R.id.txt_num_autorias);
                t.setText(String.valueOf(numPeticiones));
            }else{
                v.setVisibility(View.GONE);
            }

    }

    private void rellenarEstadoWS(String op, String res) {
        if (op.equals("estadows")){
            TextView txt = findViewById(R.id.txtInf_ws);
            txt.setText(res);
        }else if (op.equals("op_pendientes")){
            TextView txt = findViewById(R.id.txtInf_tareas);
            txt.setText(res);
        }

    }

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if (myServicio != null){
                dbMesas = (DBMesas) myServicio.getDb("mesas");
                dbZonas = (DBZonas) myServicio.getDb("zonas");
                dbCuenta = (DBCuenta) myServicio.getDb("lineaspedido");
                dbReceptores = (DBReceptores) myServicio.getDb("receptores");
                myServicio.setExHandler("mesasabiertas", handlerMesas );
                myServicio.setExHandler("zonas", handlerMesas );
                myServicio.setExHandler("mesas", handlerMesas );
                myServicio.setExHandler("mensajes", handlerMensajes);
                myServicio.setExHandler("lineaspedido", handlerPedidos);
                myServicio.setExHandler("estadows", handlerEstadoWS);
                myServicio.setCamarero(cam); //debe de ir despues de setExHandler....
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    private void rellenarPedido(int IDReceptor, boolean all) {
        try{

            IDReceptor = receptores.get(IDReceptor).getInt("ID");
            String sql_where = "camarero=" + cam.getString("ID")+ " and ";

            if (all) sql_where = "";

            JSONArray lineas = dbCuenta.filterByPedidos( sql_where+ "  receptor=" + IDReceptor + " and servido=0", "IDArt, IDPedido, receptor");

            LinearLayout ll = findViewById(R.id.listaPedidoComanda);
            ll.removeAllViews();

            DisplayMetrics metrics = getResources().getDisplayMetrics();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(5,5,5,5);

            int IDPedido = -1;
            View grupo = null;
            LinearLayout listaGr = null;
            LinearLayout.LayoutParams params_linea = null;

            for (int i = 0; i < lineas.length(); i++) {

                JSONObject art = lineas.getJSONObject(i);

                if (IDPedido == -1 || IDPedido != art.getInt("IDPedido")){
                    IDPedido = art.getInt("IDPedido");
                    LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    grupo  = inflater.inflate(R.layout.grupos_pedidos, null);
                    listaGr = grupo.findViewById(R.id.lista_pedidos_grupo);
                    TextView m = grupo.findViewById(R.id.texto_mesa);
                    m.setText("Mesa: "+art.getString("nomMesa"));
                    ll.addView(grupo, params);
                    ImageButton btn = grupo.findViewById(R.id.borrar_pedido);
                    btn.setTag(IDPedido);

                    btn.setOnClickListener(view -> {
                        JSONArray finalLineas = dbCuenta.filterByPedidos("IDPedido="+view.getTag().toString());
                        for (int i1 = 0; i1 < finalLineas.length(); i1++) {
                            try {
                                servirPedido(finalLineas.getJSONObject(i1));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        selectReceptor(sel_receptor, all_cam);
                    });

                    params_linea = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (metrics.density * 40f));


                }

                LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                View v  = inflater.inflate(R.layout.linea_pedido_externo, null);

                TextView c = v.findViewById(R.id.lblCantidad);
                TextView n = v.findViewById(R.id.lblNombre);
                ImageButton b = v.findViewById(R.id.btnBorrarPedido);
                b.setTag(art);

                c.setText(String.format("%s", art.getString("Can")));
                n.setText(String.format("%s", art.getString("Descripcion")));
                listaGr.addView(v, params_linea);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void rellenarReceptores() {
        try {
            receptores = dbReceptores.getAll();
            botonesReceptores = findViewById(R.id.pneReceptores);
            botonesReceptores.removeAllViews();

            if(receptores.size()>0){

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

                params.setMargins(5,0,5,0);
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                float dp = 5f;
                float fpixels = metrics.density * dp;
                int pixels = (int) (fpixels + 0.5f);
                int i = 0;
                for (JSONObject r: receptores) {

                    Button btn = new Button(cx);
                    btn.setId(i);
                    btn.setSingleLine(false);

                    btn.setTextSize(pixels);
                    btn.setTag(r);
                    btn.setText(r.getString("nombre").trim().replace(" ", "\n"));
                    btn.setBackgroundResource(R.drawable.bg_pink);
                    btn.setOnLongClickListener(this);
                    btn.setOnClickListener(view -> {
                        selectReceptor(view.getId(), false);
                    });
                    btn.setOnLongClickListener(view -> {
                        selectReceptor(view.getId(), true);
                        return true;
                    });
                    botonesReceptores.addView(btn, params);
                    i++;
                }
                selectReceptor(0, false);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void selectReceptor(int id, boolean all){
        sel_receptor = id;
        all_cam = all;
        for(int l = 0; l < receptores.size(); l++){
            View f = botonesReceptores.findViewById(l);
            f.setBackgroundResource(R.drawable.bg_pink);
        }
        View f = botonesReceptores.findViewById(id);
        if (all) f.setBackgroundResource(R.drawable.bg_red);
        else f.setBackgroundResource(R.drawable.bg_blue_light);
        rellenarPedido(id, all);
    }

    private void rellenarZonas() {
        try {
            JSONArray lszonas = dbZonas.getAll();
            LinearLayout ll = findViewById(R.id.pneZonas);
            ll.removeAllViews();

            if(lszonas.length()>0){


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

                params.setMargins(5,0,5,0);
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                float dp = 5f;
                float fpixels = metrics.density * dp;
                int pixels = (int) (fpixels + 0.5f);

                for (int i = 0; i < lszonas.length(); i++) {
                    JSONObject  z =  lszonas.getJSONObject(i);

                    if(zn==null && i==0) zn=z;

                    Button btn = new Button(cx);
                    btn.setId(i);
                    btn.setSingleLine(false);

                    btn.setTextSize(pixels);
                    btn.setTag(z);
                    btn.setText(z.getString("Nombre").trim().replace(" ", "\n"));
                    String[] rgb = z.getString("RGB").split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));
                    btn.setOnLongClickListener(this);
                    btn.setOnClickListener(view -> {
                            zn = (JSONObject)view.getTag();
                            rellenarMesas();
                    });
                    ll.addView(btn, params);
                }
                rellenarMesas();
            }

        }catch (Exception e){
          e.printStackTrace();
        }

    }

    private void rellenarMesas() {
        try {

            String idz = zn.getString("ID");
            JSONArray lsmesas = dbMesas.getAll(idz) ;
            listaMesas.clearTable();

            if(lsmesas.length()>0){

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        0, Math.round(metrics.density * 150));

                rowparams.setMargins(5,5,5,5);

                TableRow row = new TableRow(cx);


                listaMesas.addView(row, params);
                for (int i = 0; i < lsmesas.length(); i++) {

                    JSONObject  m =  lsmesas.getJSONObject(i);
                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.boton_mesa, null);

                    ImageButton btnCm = v.findViewById(R.id.btnCambiarMesa);
                    ImageButton btnC = v.findViewById(R.id.btnCobrar);
                    ImageButton btnLs = v.findViewById(R.id.btnLsPedidos);
                    LinearLayout panel = v.findViewById(R.id.pneBtnMesa);

                    if(m.getString("abierta").equals("0")){
                        panel.setVisibility(View.GONE);
                    }else{
                        inicializarBtnAux(btnC, btnCm, btnLs, m);
                    }


                    Button btn = v.findViewById(R.id.btnMesa);
                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setText(m.getString("Nombre"));
                    btn.setTextSize(15);
                    btn.setTag(m);

                    String[] rgb = m.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));

                    btn.setOnClickListener(view -> {
                        try {
                            JSONObject m1 = (JSONObject) view.getTag();
                            m1.put("Tarifa", zn.getString("Tarifa"));
                            Intent intent = new Intent(cx, HacerComandas.class);
                            intent.putExtra("op", "m");
                            intent.putExtra("cam", cam.toString());
                            intent.putExtra("mesa", m1.toString());
                            intent.putExtra("url", server);
                            startActivity(intent);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });

                    row.addView(v, rowparams);
                    if (((i+1) % 3) == 0) {
                        row = new TableRow(cx);
                        listaMesas.addView(row, params);
                    }

                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void inicializarBtnAux(ImageButton btnC, ImageButton btnCm, ImageButton btnLs, final JSONObject m) {
         btnC.setTag(m);btnCm.setTag(m);btnLs.setTag(m);
         btnCm.setOnLongClickListener(view -> {
             clickJuntarMesa(view);
             return false;
         });

    }

    private void servirPedido(JSONObject obj){
        try{
            ContentValues p = new ContentValues();
            p.put("art", obj.toString());
            p.put("idz", zn.getString("ID"));
            myServicio.addColaInstrucciones(new Instruccion(p, "/pedidos/servido"));
            dbCuenta.artServido(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void clickServido(View v){
        JSONObject obj = (JSONObject) v.getTag();
        servirPedido(obj);
        selectReceptor(sel_receptor, all_cam);
    }

    public void mostrarListaTicket(View v) {
        Intent intent = new Intent(cx, Cuenta.class);
        intent.putExtra("url", server);
        intent.putExtra("mesa", v.getTag().toString());
        startActivity(intent);
     }

    public void clickCambiarMesa(View v){
        Intent intent = new Intent(cx, OpMesas.class);
        intent.putExtra("url", server);
        intent.putExtra("mesa", v.getTag().toString());
        intent.putExtra("op", "cambiar");
        startActivity(intent);
    }

    public void clickJuntarMesa(View v){
        Intent intent = new Intent(cx, OpMesas.class);
        intent.putExtra("url", server);
        intent.putExtra("mesa", v.getTag().toString());
        intent.putExtra("op", "juntar");
        startActivity(intent);
    }

    public void clickMostrarPedidos(View v){
        Intent intent = new Intent(cx, MostrarPedidos.class);
        intent.putExtra("url", server);
        intent.putExtra("mesa", v.getTag().toString());
        startActivity(intent);
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if(!pref.isNull("zn")) {
                zn = new JSONObject(pref.getString("zn"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void buscarPedidos(View v){
        Intent intent = new Intent(cx, BuscarPedidos.class);
        intent.putExtra("url", server);
        startActivity(intent);
    }

    public void mostrarAutorias(View v){
        viendo_mensajes = true;
        Intent i = new Intent(cx, Autorias.class);
        i.putExtra("url", server);
        i.putExtra("peticiones", peticiones.toString());
        peticiones = new JSONArray();
        startActivityForResult(i, 400);
    }

    public void  mostrarSendMensajes(View v){
        try {
            Intent i = new Intent(cx, SendMensajes.class);
            i.putExtra("camarero", cam.getString("ID"));
            startActivity(i);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 400) {
            if(resultCode == RESULT_OK){
                try{
                    assert data != null;
                    JSONArray p = new JSONArray(data.getStringExtra("mensajes"));
                    for(int i = 0; i < p.length(); i++) {
                        peticiones.put(p.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        pedidos = new Pedidos();
        listaMesas = new ListaMesas();
        AdaptadorMesas adaptadorMesas = new AdaptadorMesas(getSupportFragmentManager(), listaMesas, pedidos);

        ViewPager vpPager = findViewById(R.id.pager);

        vpPager.setAdapter(adaptadorMesas);

        try {
            server = getIntent().getExtras().getString("url");
            cam = new JSONObject(getIntent().getExtras().getString("cam"));
            TextView title = findViewById(R.id.lblTitulo);
            title.setText(cam.getString("Nombre"));
            findViewById(R.id.show_autorias).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(presBack>=1) {
            super.onBackPressed();
        }else{
            mostrarToast("Pulsa otra vez para salir", Gravity.BOTTOM, 0, 80);
            presBack++;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if(pref.isNull("zn")) {
                pref.put("zn", view.getTag().toString());
            }else{
              if(pref.getString("zn").equals(view.getTag().toString())){
                  pref.remove("zn");
              }else pref.put("zn", view.getTag().toString());
            }
            json.serializar("preferencias.dat", pref, cx);
            mostrarToast("Asocioacion realizada");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onResume() {
        presBack = 0;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                handlerOperaciones.sendEmptyMessage(0);
            }
        }, 3000);
        cargarPreferencias();
        if(myServicio == null) {
            Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        }else {
            viendo_mensajes = false;
            rellenarZonas();
            rellenarReceptores();
            manejarAurotias();
        }
        super.onResume();
    }

    @Override
    public void pedir(View v) {
        try{
            JSONObject obj = (JSONObject)v.getTag();
            ContentValues p = new ContentValues();
            p.put("idp",obj.getString("IDPedido"));
            p.put("id",obj.getString("IDArt"));
            p.put("Descripcion",obj.getString("Descripcion"));
            myServicio.addColaInstrucciones(new Instruccion(p, "/impresion/reenviarlinea", handlerOperaciones, "reenviar"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
