package com.valleapp.valletpv;

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

import com.valleapp.valletpv.Util.ServicioCom;
import com.valleapp.valletpv.db.DbCuenta;
import com.valleapp.valletpv.db.DbMesas;
import com.valleapp.valletpv.db.DbZonas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OpMesas extends Activity {

    DbMesas dbMesas=new DbMesas(this);
    DbZonas dbZonas= new DbZonas(this);
    DbCuenta dbCuenta = new DbCuenta(this);

    ServicioCom servicioCom;

    String server="";
    JSONObject mesa ;
    String op ;
    JSONArray lsmesas = null;
    JSONArray lszonas = null;
    JSONObject zn = null;
    Context cx;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            servicioCom = ((ServicioCom.MyBinder)iBinder).getService();
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

                LinearLayout ll = (LinearLayout)findViewById(R.id.pneZonas);
                ll.removeAllViews();

                DisplayMetrics metrics = getResources().getDisplayMetrics();


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        Math.round(metrics.density * 100),LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(5,0,5,0);
                for (int i = 0; i < lszonas.length(); i++) {
                    JSONObject  z =  lszonas.getJSONObject(i);

                    if(zn==null && i==0) zn=z;

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.btn_art, null);


                    Button btn = (Button)v.findViewById(R.id.boton_art);
                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setTextSize(11);
                    btn.setText(z.getString("Nombre").trim().replace(" ", "\n"));
                    String[] rgb = z.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(view -> {
                        try {
                            zn = lszonas.getJSONObject((Integer)view.getId());
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

            lsmesas = dbMesas.getAllMenosUna(zn.getString("ID"), mesa.getString("ID"));

            if(lsmesas.length()>0){

                TableLayout ll = (TableLayout)findViewById(R.id.pneMesas);
                ll.removeAllViews();
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);


                DisplayMetrics metrics = getResources().getDisplayMetrics();


                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        Math.round(metrics.density * 160),
                        Math.round(metrics.density * 160));

                rowparams.setMargins(5,5,5,5);

                TableRow row = new TableRow(cx);
                ll.addView(row, params);

                for (int i = 0; i < lsmesas.length(); i++) {
                    JSONObject  m =  lsmesas.getJSONObject(i);

                        LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                                (Context.LAYOUT_INFLATER_SERVICE);

                        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.btn_art, null);


                        Button btn = (Button) v.findViewById(R.id.boton_art);

                        btn.setId(i);
                        btn.setSingleLine(false);
                        btn.setText(m.getString("Nombre"));
                        btn.setTag(m);
                        btn.setTextSize(15);
                        String[] rgb = m.getString("RGB").trim().split(",");
                        btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                        btn.setOnClickListener(view -> {
                            try {
                                JSONObject m1 = (JSONObject)view.getTag();
                                ContentValues p = new ContentValues();
                                p.put("idp", mesa.getString("ID"));
                                p.put("ids", m1.getString("ID"));
                                String url = op.equals("cambiar") ? "cambiarmesas" :"juntarmesas";
                                if(servicioCom!=null){
                                    servicioCom.opMesas(p, url);
                                    finalizar(m1);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        });
                        row.addView(v, rowparams);

                        if (((i + 1) % 5) == 0) {
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
                dbMesas.abrirMesa(m.getString("ID"));
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
                dbMesas.abrirMesa(m.getString("ID"));
            }
        }

        Toast.makeText(getApplicationContext(), "Realizando un cambio en las mesas.....", Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op_mesas);
        cx = this;
        server = getIntent().getExtras().getString("url");
        op = getIntent().getExtras().getString("op");
        try {
            mesa = new JSONObject(getIntent().getExtras().getString("mesa"));
            TextView l = (TextView)findViewById(R.id.lblTitulo);
            String titulo = op.equals("cambiar") ? "Cambiar mesa "+ mesa.getString("Nombre") : "Juntar mesa "+ mesa.getString("Nombre") ;
            l.setText(titulo);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        rellenarZonas();
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
