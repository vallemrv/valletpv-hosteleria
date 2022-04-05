package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.valleapp.comandas.utilidades.HTTPRequest;
import com.valleapp.comandas.utilidades.JSON;
import com.valleapp.comandas.interfaces.IPedidos;
import com.valleapp.comandas.adaptadores.AdaptadorMesas;
import com.valleapp.comandas.pestañas.ListaMesas;
import com.valleapp.comandas.pestañas.Pedidos;
import com.valleapp.comandas.utilidades.ServicioCom;
import com.valleapp.comandas.db.DbMesas;
import com.valleapp.comandas.db.DbZonas;


public class Mesas extends FragmentActivity implements View.OnLongClickListener, IPedidos {

    private AdaptadorMesas adaptadorMesas;
    private ListaMesas listaMesas = null;
    private Pedidos pedidos = null;

    private String server = "";
    int presBack = 0;
    DbMesas dbMesas = new DbMesas(this);
    DbZonas dbZonas = new DbZonas(this);
    JSONObject cam = null;
    JSONObject zn = null;

    final Context cx = this;
    ServicioCom myServicio;



    @SuppressLint("HandlerLeak")
    private final Handler controller_zonas = new Handler() {
        public void handleMessage(Message msg) {
           RellenarZonas();
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler controller_mesas = new Handler() {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if (op.equals("pendientes")){
                getPendientes();
            } else if (op.equals("mensaje")){
                Dialog dg = new Dialog(cx);
                dg.setContentView(R.layout.dialog_mensaje);
                TextView txt = dg.findViewById(R.id.texto_mensaje);
                txt.setTextSize(35);
                txt.setText(res);
                dg.setTitle("Atencion mensaje...");
                dg.show();
            } else if (op.equals("exit")){
                 salir_mesas();
            } else if(op.equals("pedido")) {
                RellenarPedido(res);
            }else if(op.equals("mesasabiertas")) {
                try {
                    dbMesas.updateByZona(new JSONArray(res), zn.getString("ID"));
                    RellenarMesas();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else if (op.equals("servido")){
                Toast toast= Toast.makeText(getApplicationContext(),
                        "Pedido servdio...", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
                toast.show();
            }else{
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
            if(myServicio!=null) myServicio.setHandleZonas(controller_zonas);
            if(myServicio!=null) myServicio.setHandleMesas(controller_mesas);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    private void salir_mesas(){
        if(mConexion!=null && myServicio!=null){
            unbindService(mConexion);
        }
        Intent servicio_com = new Intent(getApplicationContext(), ServicioCom.class);
        stopService(servicio_com);
        mConexion = null;
        super.onBackPressed();
    }

    private void RellenarPedido(String res) {

        try{

            JSONArray lineas = new JSONArray(res);
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


    private void RellenarZonas() {
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
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                zn = (JSONObject)view.getTag();
                                refreshMesas();
                                getPendientes();
                        }
                    });
                    ll.addView(btn, params);
                }

                RellenarMesas();
                getPendientes();
            }

        }catch (Exception e){
          e.printStackTrace();
        }

    }

    private void RellenarMesas() {
        try {

            JSONArray lsmesas = dbMesas.getAll(zn.getString("ID")) ;
            listaMesas.clearTable();

            if(lsmesas.length()>0){

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                DisplayMetrics metrics = getResources().getDisplayMetrics();
                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, Math.round(metrics.density * 120), 0.33f);

                rowparams.setMargins(5,5,5,5);

                TableRow row = new TableRow(cx);
                listaMesas.addView(row, params);

                for (int i = 0; i < lsmesas.length(); i++) {
                    JSONObject  m =  lsmesas.getJSONObject(i);
                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.boton_mesa, null);

                    ImageButton btnCm = v.findViewById(R.id.btnCambiarMesa);
                    ImageButton btnC = v.findViewById(R.id.btnCobrar);
                    ImageButton btnLs = v.findViewById(R.id.btnLsPedidos);

                    String[] rgb = m.getString("RGB").split(",");
                    //LinearLayout pne = v.findViewById(R.id.pneBotones);

                    btnC.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));
                    btnCm.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));
                    btnLs.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));


                    if(!m.getBoolean("abierta")){
                        btnC.setVisibility(View.INVISIBLE); btnCm.setVisibility(View.INVISIBLE);
                        btnLs.setVisibility(View.INVISIBLE);
                        //pne.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));
                    }else{

                        inicializarBtnAux(btnC, btnCm, btnLs, m);
                    }
                    Button btn = v.findViewById(R.id.btnMesa);

                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setText(m.getString("Nombre"));
                    btn.setTextSize(15);
                    btn.setTag(m);
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                JSONObject m = (JSONObject)view.getTag();
                                Intent intent = new Intent(cx, Comanda.class);
                                intent.putExtra("op", "m");
                                intent.putExtra("url", server);
                                intent.putExtra("cam", cam.toString());
                                intent.putExtra("mesa", m.toString());
                                startActivity(intent);
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
         btnCm.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 ((Mesas)cx).clickJuntarMesa(view);
                 return false;
             }
         });

    }

    public void getPendientes(){
        try {
            if (zn != null) {
                List<NameValuePair> p = new ArrayList<>();
                p.add(new BasicNameValuePair("idz", zn.getString("ID")));
                new HTTPRequest(server + "/pedidos/getpendientes", p, "pedido", controller_mesas);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void refreshMesas(){
        try {
            if (zn != null) {
                List<NameValuePair> p = new ArrayList<>();
                p.add(new BasicNameValuePair("idz", zn.getString("ID")));
                new HTTPRequest(server + "/mesas/lsmesasabiertas", p, "mesasabiertas", controller_mesas);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clickServido(View v){
        try {
            JSONObject obj = (JSONObject) v.getTag();
            List<NameValuePair> p = new ArrayList<>();
            p.add(new BasicNameValuePair("art", obj.toString()));
            p.add(new BasicNameValuePair("idz", zn.getString("ID")));
            new HTTPRequest(server + "/pedidos/servido", p, "servido", controller_mesas);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void MostrarListaTicket(View v) {
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
            title.setText(cam.getString("Nombre")+" "+ cam.getString("Apellidos"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onDestroy() {
        if(mConexion!=null && myServicio!=null){
            unbindService(mConexion);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        presBack = 0;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(presBack>=1) {
            salir_mesas();
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
        Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
        intent.putExtra("server", server);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        cargarPreferencias();
        RellenarZonas();
        refreshMesas();
        getPendientes();
        super.onResume();
    }

    @Override
    public void pedir(View v) {
        try{
            JSONObject obj = (JSONObject)v.getTag();
            List<NameValuePair> p = new ArrayList<>();
            p.add(new BasicNameValuePair("idp",obj.getString("IDPedido")));
            p.add(new BasicNameValuePair("id",obj.getString("IDArt")));
            p.add(new BasicNameValuePair("Nombre",obj.getString("Nombre")));
            new HTTPRequest(server+"/impresion/reenviarlinea",p,"", controller_mesas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
