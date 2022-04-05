package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.valleapp.comandas.db.DBCuenta;
import com.valleapp.comandas.db.DBMesas;
import com.valleapp.comandas.db.DBZonas;
import com.valleapp.comandas.utilidades.Instruccion;
import com.valleapp.comandas.utilidades.JSON;
import com.valleapp.comandas.interfaces.IPedidos;
import com.valleapp.comandas.adaptadores.AdaptadorMesas;
import com.valleapp.comandas.pestañas.ListaMesas;
import com.valleapp.comandas.pestañas.Pedidos;
import com.valleapp.comandas.utilidades.ServicioCom;


public class Mesas extends FragmentActivity implements View.OnLongClickListener, IPedidos {

    private AdaptadorMesas adaptadorMesas;
    private ListaMesas listaMesas = null;
    private Pedidos pedidos = null;

    private String server = "";
    int presBack = 0;

    DBMesas dbMesas;
    DBZonas dbZonas;
    DBCuenta dbCuenta;

    JSONObject cam = null;
    JSONObject zn = null;

    final Context cx = this;

    ServicioCom myServicio;


    private final Handler handlerMesas = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
           rellenarZonas();
        }
    };


    private final Handler handlerOperaciones = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if (op.equals("exit")){
                 finish();
            } else if(op.equals("pedidos")) {
                try {
                    dbCuenta.atualizarDatos(new JSONArray(res));
                    rellenarPedido();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if (op.equals("servido")){
                Toast toast= Toast.makeText(getApplicationContext(),
                        "Pedido servdio...", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
                toast.show();
            }else if (op.equals("reenviar")){
                Toast toast= Toast.makeText(getApplicationContext(),
                        "Peticion enviadaaaa", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
                toast.show();
            }
        }
    };


    private ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if (myServicio != null){
                myServicio.setExHandler("mesasabiertas", handlerMesas );
                dbMesas = (DBMesas) myServicio.getDb("mesas");
                dbZonas = (DBZonas) myServicio.getDb("zonas");
                dbCuenta = (DBCuenta) myServicio.getDb("lineaspedido");
                rellenarZonas();

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };


    private void rellenarPedido() {

        try{

            JSONArray lineas = dbCuenta.filter("IDZona = "+ zn.getString("ID"));

            pedidos.vaciarPanel();

            if(lineas.length()>0){


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(5,0,5,0);

                 for (int i = 0; i < lineas.length(); i++) {
                   JSONObject  art =  lineas.getJSONObject(i);
                   pedidos.addLinea(art,params,this,this);
                 }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

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
                            getPendientes();
                    });
                    ll.addView(btn, params);
                }

                rellenarMesas();
                getPendientes();
            }

        }catch (Exception e){
          e.printStackTrace();
        }

    }

    private void rellenarMesas() {
        try {

            JSONArray lsmesas = dbMesas.getAll(zn.getString("ID")) ;
            listaMesas.clearTable();
            if(lsmesas.length()>0){

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        0, Math.round(metrics.density * 120));

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

                    //m.put("Tarifa", m.getString("Tarifa"));

                    Button btn = v.findViewById(R.id.btnMesa);
                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setText(m.getString("Nombre"));
                    btn.setTextSize(15);
                    btn.setTag(m);


                    String[] rgb = m.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));

                    btn.setOnClickListener(view -> {
                            JSONObject m1 = (JSONObject)view.getTag();
                            Intent intent = new Intent(cx, HacerComandas.class);
                            intent.putExtra("op", "m");
                            intent.putExtra("cam", cam.toString());
                            intent.putExtra("mesa", m1.toString());
                            startActivity(intent);
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

    public void getPendientes(){
        try {
            if (zn != null) {
                ContentValues p = new ContentValues();
                p.put("idz", zn.getString("ID"));
                myServicio.addColaInstrucciones(new Instruccion(p,
                        server+"/pedidos/getpendientes",
                        handlerOperaciones, "pedidos"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void clickServido(View v){
        try {
            JSONObject obj = (JSONObject) v.getTag();
            ContentValues p = new ContentValues();
            p.put("art", obj.toString());
            p.put("idz", zn.getString("ID"));
            myServicio.addColaInstrucciones(new Instruccion(p,
                    server+"/pedidos/servido",
                    handlerMesas, "servido"));
        }catch (Exception e){
            e.printStackTrace();
        }
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


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        pedidos = new Pedidos();
        listaMesas = new ListaMesas();
        adaptadorMesas = new AdaptadorMesas(getSupportFragmentManager(), listaMesas, pedidos);


        ViewPager vpPager = findViewById(R.id.pager);

        vpPager.setAdapter(adaptadorMesas);

        try {
            server = getIntent().getExtras().getString("url");
            cam = new JSONObject(getIntent().getExtras().getString("cam"));
            TextView title = findViewById(R.id.lblTitulo);
            title.setText(cam.getString("Nombre"));
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
            Toast.makeText(getApplicationContext(),"Pulsa otra vez para salir", Toast.LENGTH_SHORT).show();
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

            Toast toast= Toast.makeText(getApplicationContext(),
                    "Asocicion realizada", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
            toast.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onResume() {
        presBack = 0;
        Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        cargarPreferencias();
        if (myServicio != null){
            rellenarZonas();
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
            p.put("Nombre",obj.getString("Nombre"));
            myServicio.addColaInstrucciones(new Instruccion(p, server+"/impresion/reenviarlinea", handlerOperaciones, "reenviar"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
