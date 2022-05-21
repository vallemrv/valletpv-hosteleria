/*
  @Author: Manuel Rodriguez <valle>
 * @Date:   2018-12-29T11:26:27+01:00
 * @Email:  valle.mrv@gmail.com
 * @Last modified by:   valle
 * @Last modified time: 2018-12-29T14:52:12+01:00
 * @License: Apache License v2.0
 */
package com.valleapp.valletpv;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.valleapp.valletpv.db.DBCamareros;
import com.valleapp.valletpv.tools.ServicioCom;
import com.valleapp.valletpv.tools.ToastShowInfoCuenta;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Camareros extends Activity {


    final Context cx = this;

    JSONArray lscam = null;
    JSONObject cam_sel = null;
    ServicioCom myServicio;

    DBCamareros dbCamareros;

    int presBack = 0;

   ToastShowInfoCuenta toastShowInfoCuenta;

    private final Handler handleHttp = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            if (op == null) {
                rellenarCamareros();
            }else{
                if (op == "show_info_cobro"){
                    Bundle datos = msg.getData();
                    Double entrega = datos.getDouble("entrega");
                    Double cambio = datos.getDouble("cambio");
                    if (cambio > 0) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.toast_info_cambio, findViewById(R.id.toast_info_cobro_container));
                        if (toastShowInfoCuenta != null) toastShowInfoCuenta.cancel();
                        toastShowInfoCuenta = new ToastShowInfoCuenta();
                        toastShowInfoCuenta.show(entrega, cambio, 10000, getApplicationContext(), layout);
                    }
                }
            }
        }
    };

    private void rellenarCamareros() {
       try {
           lscam = dbCamareros.filter("autorizado=1");
           if(lscam.length()>0){

               TableLayout ll = findViewById(R.id.pneCamareros);
               ll.removeAllViews();

               DisplayMetrics metrics = getResources().getDisplayMetrics();


               TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                          TableLayout.LayoutParams.MATCH_PARENT,
                          TableLayout.LayoutParams.WRAP_CONTENT);


               TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                          TableLayout.LayoutParams.MATCH_PARENT,
                          Math.round(metrics.density * 120));

               rowparams.setMargins(5,5,5,5);
               ll.setStretchAllColumns(true);

               TableRow row = new TableRow(cx);

               ll.addView(row, params);


               for (int i = 0; i < lscam.length(); i++) {
                   JSONObject  cam =  lscam.getJSONObject(i);
                   Button btn = new Button(cx);
                   btn.setId(i);
                   btn.setSingleLine(false);
                   String[] nom = cam.getString("Nombre").split(" ");

                   btn.setText(nom.length > 1 ? nom[0]+"\n"+nom[1] : cam.getString("Nombre") +"\n");
                   btn.setBackgroundResource(R.drawable.fondo_btn_xml);
                   btn.setOnClickListener(view -> {
                       try {
                           final JSONObject obj =  lscam.getJSONObject(view.getId());
                           entrar_en_mesas((obj));
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                      });
                      row.addView(btn, rowparams);

                      if (((i+1) % 6) == 0) {
                          row = new TableRow(cx);
                          ll.addView(row, params);
                      }
               }
           }

       }catch (Exception e){
          e.printStackTrace();
       }
    }

    protected void entrar_en_mesas(final JSONObject camarero) throws JSONException {
        presBack = 0;
        cam_sel = camarero;
        Intent intent = new Intent(cx, Mesas.class);
        intent.putExtra("cam", cam_sel.toString());
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camareros);
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
    protected void onResume() {
        presBack = 0;
        Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        if (myServicio != null){
            rellenarCamareros();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        super.onDestroy();
    }

    private ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if(myServicio!=null){
                myServicio.setExHandler("camareros", handleHttp);
                dbCamareros = (DBCamareros) myServicio.getDb("camareros");
                rellenarCamareros();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };


}
