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
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSelCam;
import com.valleapp.valletpv.db.DBCamareros;
import com.valleapp.valletpv.dlg.DlgAddNuevoCamarero;
import com.valleapp.valletpvlib.tools.JSON;
import com.valleapp.valletpv.tools.ServicioCom;

import org.json.JSONException;
import org.json.JSONObject;

public class ValleTPV extends Activity {

    final Context cx = this;

    private String server = "";

    ServicioCom myServicio;
    DBCamareros dbCamareros;

    ListView lsnoautorizados;
    ListView lstautorizados;

    private String urlCashlogy = "";
    private boolean usarCashlogy = false;


    private final Handler handleHttp = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            Log.e("CASHLOGY", bundle.toString());
            if (bundle.containsKey("CashlogyMsg")) {
                String toastMessage = bundle.getString("CashlogyMsg");
                if (toastMessage != null) {
                    Toast.makeText(ValleTPV.this, toastMessage, Toast.LENGTH_LONG).show();
                }
            }else {
                rellenarListas();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seleccionar_camareros);

        ImageButton s = findViewById(R.id.salir);
        lstautorizados = findViewById(R.id.lstautorizados);
        lsnoautorizados = findViewById(R.id.lstnoautorizados);

        s.setOnClickListener(view -> finish());

        ImageButton btnok = findViewById(R.id.aceptar);
        btnok.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), Camareros.class);
            startActivity(i);
        });

        lsnoautorizados.setOnItemClickListener((adapterView, view, i, l) -> {
            JSONObject obj = (JSONObject)view.getTag();
            try {
                dbCamareros.setAutorizado(obj.getInt("ID"), true);
                obj.put("autorizado", "1");
                myServicio.autorizarCam(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            rellenarListas();
        });

         findViewById(R.id.btn_add_nuevo_camarero).setOnClickListener(view -> {
             DlgAddNuevoCamarero dlg = new DlgAddNuevoCamarero(cx, myServicio);
             dlg.show();
         });


        lstautorizados.setOnItemClickListener((adapterView, view, i, l) -> {
            JSONObject obj = (JSONObject)view.getTag();
            try {
                obj.put("autorizado", "0");
                dbCamareros.setAutorizado(obj.getInt("ID"), false);
                myServicio.autorizarCam(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            rellenarListas();

        });
    }

    private void rellenarListas(){
        lstautorizados.setAdapter(new AdaptadorSelCam(cx, dbCamareros.getAutorizados(true)));
        lsnoautorizados.setAdapter(new AdaptadorSelCam(cx, dbCamareros.getAutorizados(false)));
    }

    @Override
    protected void onResume() {
        cargarPreferencias();
        if (myServicio != null) {
            myServicio.setExHandler("camareros", handleHttp);
            rellenarListas();
        }
        if (server != null && !server.equals("") && myServicio == null) {
            Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
            intent.putExtra("url", server);
            intent.putExtra("url_cashlogy", urlCashlogy);  // Agregar URL de Cashlogy
            intent.putExtra("usar_cashlogy", usarCashlogy); // Agregar estado del CheckBox
            startService(intent);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        }
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        Intent intent = new Intent(cx, ServicioCom.class);
        stopService(intent);
        super.onDestroy();
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if (pref == null) {
                Intent intent = new Intent(this, PreferenciasTPV.class);
                startActivity(intent);
            } else {
                server = pref.getString("URL");
                urlCashlogy = pref.optString("URL_Cashlogy", ""); // Leer URL_Cashlogy
                usarCashlogy = pref.optBoolean("usaCashlogy", false); // Leer usarCashlogy
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if(myServicio!=null){
                myServicio.setUiHandlerCashlogy(handleHttp);
                myServicio.setExHandler("camareros", handleHttp);
                dbCamareros = (DBCamareros) myServicio.getDb("camareros");
                rellenarListas();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };



}