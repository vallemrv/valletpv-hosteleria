package com.valleapp.vallecom.Activitys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.valleapp.vallecom.R;
import com.valleapp.vallecom.adaptadores.AdaptadorTicket;
import com.valleapp.vallecom.db.DBCuenta;
import com.valleapp.vallecom.db.DBMesas;
import com.valleapp.vallecom.utilidades.HTTPRequest;
import com.valleapp.vallecom.utilidades.Instruccion;
import com.valleapp.vallecom.utilidades.ServicioCom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Cuenta extends Activity {

    String totalMesa;
    String server = "";
    JSONObject mesa;
    Context cx;
    DBCuenta dbCuenta;
    DBMesas dbmesa;
    ServicioCom myServicio;


    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                myServicio = ((ServicioCom.MyBinder) iBinder).getService();
                myServicio.setExHandler("lineaspedido", handlerHttp);
                dbCuenta = (DBCuenta) myServicio.getDb("lineaspedido");
                dbmesa = (DBMesas) myServicio.getDb("mesas");
                rellenarTicket();
                //Atualizar cuenta.
                ContentValues p = new ContentValues();
                p.put("mesa_id",mesa.getString("ID"));
                new HTTPRequest(server+"/cuenta/get_cuenta",p,"actualizar", handlerHttp);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };


    @SuppressLint("HandlerLeak")
    private final Handler handlerHttp = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op != null && op.equals("actualizar")){
                try {
                    JSONArray datos = new JSONArray(res);
                    if (datos.length() > 0) {
                        dbCuenta.actualizarMesa(datos, mesa.getString("ID"));
                    }else{
                        DBMesas db = (DBMesas) myServicio.getDb("mesas");
                        db.cerrarMesa(mesa.getString("ID"));
                        finish();
                    }
                    rellenarTicket();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
         }
    };

    @SuppressLint("DefaultLocale")
    private void rellenarTicket() {
        try {
            TextView l =  findViewById(R.id.txtTotal);
            ListView lst =  findViewById(R.id.lstCuenta);
            List<JSONObject> lineasTicket = dbCuenta.getAll(mesa.getString("ID"));
            totalMesa = String.format("%.2f", dbCuenta.getTotal(mesa.getString("ID")));
            l.setText(String.format("%s €", totalMesa));
            lst.setAdapter(new AdaptadorTicket(cx, (ArrayList<JSONObject>) lineasTicket));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        TextView lbl = findViewById(R.id.lblMesa);
        this.cx = this;
        try {

            server = getIntent().getExtras().getString("url");
            mesa = new JSONObject(getIntent().getExtras().getString("mesa"));
            lbl.setText("Mesa "+mesa.getString("Nombre"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        if(myServicio == null) {
            Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
            intent.putExtra("url", server);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        }else{
            rellenarTicket();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        myServicio.rmExHandler("lineaspedido");
        unbindService(mConexion);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void clickImprimir(View v){
        if(Double.parseDouble(totalMesa.replace(",","."))>0) {
            try {
                ContentValues p = new ContentValues();
                p.put("idm", mesa.getString("ID"));
                dbmesa.marcarRojo(mesa.getString("ID"));
                myServicio.addColaInstrucciones(new Instruccion(p,"/impresion/preimprimir"));
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickMarcarRojo(View v){
        if(Double.parseDouble(totalMesa.replace(",","."))>0) {
            try {
                ContentValues p = new ContentValues();
                p.put("idm", mesa.getString("ID"));
                myServicio.addColaInstrucciones(new Instruccion(p,"/comandas/marcar_rojo"));
                dbmesa.marcarRojo(mesa.getString("ID"));
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickSalir(View v){
        finish();
    }
}
