package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.comandas.db.DBCuenta;
import com.valleapp.comandas.db.DBMesas;
import com.valleapp.comandas.db.DBZonas;
import com.valleapp.comandas.utilidades.Instruccion;
import com.valleapp.comandas.utilidades.ServicioCom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OpMesas extends Activity {

    DBZonas dbZonas;
    DBMesas dbMesas;
    DBCuenta dbCuenta;
    JSONObject mesa ;
    String op ;
    JSONArray lsmesas = null;
    JSONArray lszonas = null;
    JSONObject zn = null;
    JSONObject art = null;
    private String url;
    Context cx;
    private String server;
    private ServicioCom servicioCom;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            servicioCom = ((ServicioCom.MyBinder)iBinder).getService();
            if (servicioCom != null){
                dbZonas = (DBZonas) servicioCom.getDb("zonas");
                dbMesas = (DBMesas) servicioCom.getDb("mesas");
                dbCuenta = (DBCuenta) servicioCom.getDb("lineaspedido");
                rellenarZonas();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            servicioCom = null;
        }
    };



    private void rellenarZonas() {
        try {

            lszonas = dbZonas.getAll();

            if(lszonas.length()>0){

                LinearLayout ll = findViewById(R.id.pneZonas);
                ll.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

                params.setMargins(5,0,5,0);
                for (int i = 0; i < lszonas.length(); i++) {
                    JSONObject  z =  lszonas.getJSONObject(i);

                    if(zn==null && i==0) zn=z;

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.btn_art, null);


                    Button btn = v.findViewById(R.id.boton_art);
                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setTextSize(11);
                    btn.setText(z.getString("Nombre").trim().replace(" ", "\n"));
                    String[] rgb = z.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));

                    btn.setOnClickListener(view -> {
                        try {
                            zn = lszonas.getJSONObject(view.getId());
                            rellenarMesas();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                    ll.addView(v, params);


                }

                rellenarMesas();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void rellenarMesas() {
        try {


            lsmesas = dbMesas.getAll(zn.getString("ID"));

            if(lsmesas.length()>0){

                TableLayout ll = findViewById(R.id.pneMesas);
                ll.removeAllViews();
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                DisplayMetrics metrics = getResources().getDisplayMetrics();
                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, Math.round(metrics.density * 120));

                rowparams.setMargins(5,5,5,5);

                TableRow row = new TableRow(cx);
                ll.addView(row, params);

                for (int i = 0; i < lsmesas.length(); i++) {
                    JSONObject m_show = lsmesas.getJSONObject(i);

                    LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.btn_art, null);


                    Button btn = v.findViewById(R.id.boton_art);

                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setText(m_show.getString("Nombre"));
                    btn.setTag(m_show);
                    btn.setTextSize(15);
                    String[] rgb = m_show.getString("RGB").trim().split(",");
                    if (m_show.getInt("ID") != mesa.getInt("ID")) {
                        btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0].trim()),
                                Integer.parseInt(rgb[1].trim()),
                                Integer.parseInt(rgb[2].trim())));
                        btn.setOnClickListener(view -> {
                            try {
                                JSONObject m = (JSONObject) view.getTag();
                                ContentValues p = new ContentValues();

                                if (art == null) {
                                    p.put("idp", mesa.getString("ID"));
                                    p.put("ids", m.getString("ID"));
                                    finalizar(m);
                                } else {
                                    String idm = m.getString("ID");
                                    String idLinea = art.getString("ID");
                                    p.put("idm", idm);
                                    p.put("idLinea", idLinea);
                                    dbCuenta.moverLinea(m, art);
                                }
                                if (servicioCom != null) {
                                    servicioCom.addColaInstrucciones(new Instruccion(p, url));
                                    finish();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                    }else{
                        btn.setBackgroundResource(R.drawable.bg_pink);
                        btn.setOnClickListener(view ->{
                            Toast.makeText(cx,"Esta es la misma mesa..", Toast.LENGTH_SHORT).show();
                        });
                    }

                    row.addView(v, rowparams);

                    if (((i + 1) % 3) == 0) {
                        row = new TableRow(cx);
                        ll.addView(row, params);
                    }
                }


            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void finalizar(JSONObject m) throws JSONException {
        if(op.equals("cambiar")){
            if(m.getString("abierta").equals("0")){
                dbMesas.abrirMesa(m.getString("ID"), mesa.getString("num"));
                dbMesas.cerrarMesa(mesa.getString("ID"));
                dbCuenta.cambiarCuenta(mesa.getString("ID"), m.getString("ID"));
            }else{
                dbCuenta.cambiarCuenta(mesa.getString("ID"), "-100");
                dbCuenta.cambiarCuenta(m.getString("ID"), mesa.getString("ID"));
                dbCuenta.cambiarCuenta("-100", m.getString("ID"));
            }
        }else {
            if(m.getString("abierta").equals("1")){
                dbMesas.cerrarMesa(m.getString("ID"));
                dbCuenta.cambiarCuenta(m.getString("ID"), mesa.getString("ID"));
            }else{
                dbMesas.abrirMesa(m.getString("ID"), mesa.getString("num"));
            }
        }

        Toast.makeText(getApplicationContext(), "Realizando un cambio en las mesas.....", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op_mesas);
        server = getIntent().getExtras().getString("url");
        cx = this;
        op = getIntent().getExtras().getString("op");
        try {
            TextView l = findViewById(R.id.lblTitulo);
            String titulo = "";
            mesa = new JSONObject(getIntent().getExtras().getString("mesa"));
            if(op.equals("art")){
               titulo = "Cambiar articulo "+mesa.getString("Nombre");
               art = new JSONObject(getIntent().getExtras().getString("art"));
               url = server + "/cuenta/mvlinea";
            }else{
               url = server + (op.equals("cambiar") ? "/cuenta/cambiarmesas" :"/cuenta/juntarmesas");
               titulo =  (op.equals("cambiar") ? "Cambiar mesa " : "Juntar mesa ") + mesa.getString("Nombre");
            }
           l.setText(titulo);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
        intent.putExtra("url", server);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        super.onDestroy();
    }
}
