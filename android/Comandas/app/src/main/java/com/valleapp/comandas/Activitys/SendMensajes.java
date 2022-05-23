package com.valleapp.comandas.Activitys;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.valleapp.comandas.R;
import com.valleapp.comandas.adaptadores.AdaptadorAutorias;
import com.valleapp.comandas.adaptadores.AdaptadorMensajes;
import com.valleapp.comandas.db.DBCuenta;
import com.valleapp.comandas.db.DBMesas;
import com.valleapp.comandas.db.DBReceptores;
import com.valleapp.comandas.db.DBZonas;
import com.valleapp.comandas.utilidades.ActivityBase;
import com.valleapp.comandas.utilidades.HTTPRequest;
import com.valleapp.comandas.utilidades.Instruccion;
import com.valleapp.comandas.utilidades.ServicioCom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SendMensajes extends ActivityBase {

    DBReceptores dbReceptores;
    String camarero;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if (myServicio != null){
                try{
                    dbReceptores = (DBReceptores) myServicio.getDb("receptores");
                    mostrarLista();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    public void onClickItemSimple(View v){
        try {
            TextView t = findViewById(R.id.txt_mensaje);
            if (!t.getText().toString().trim().equals("")) {
                ContentValues p = new ContentValues();
                JSONObject o = new JSONObject();
                JSONArray array = new JSONArray();
                array.put(o);
                o.put("camarero", camarero);
                o.put("mensaje", t.getText().toString());
                o.put("receptor", v.getTag().toString());
                p.put("rows", array.toString());
                p.put("tb", "historialmensajes");
                myServicio.addColaInstrucciones(new Instruccion(p, "/sync/update_from_devices"));
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public  void mostrarLista(){
        try {
            ListView ls = findViewById(R.id.lista_receptores);
            ArrayList<JSONObject> lista  = dbReceptores.getAll();
            ls.setAdapter(new AdaptadorMensajes(this, lista));
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);
        camarero = getIntent().getExtras().getString("camarero");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myServicio == null) {
            Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        }else{
            mostrarLista();
        }

    }

    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        super.onDestroy();
    }
}
