package com.valleapp.comandas.Activitys;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.valleapp.comandas.R;
import com.valleapp.comandas.adaptadores.AdaptadorBuscarPedidos;
import com.valleapp.comandas.db.DBCuenta;
import com.valleapp.comandas.utilidades.HTTPRequest;
import com.valleapp.comandas.utilidades.Instruccion;
import com.valleapp.comandas.utilidades.ServicioCom;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class BuscarPedidos extends Activity implements TextWatcher {

    String server;
    Context cx;
    TextView txtBuscar;
    JSONObject objSel;
    List<JSONObject> lPedidos = new ArrayList<>();
    ServicioCom myServicio;
    DBCuenta dbCuenta;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                myServicio = ((ServicioCom.MyBinder) iBinder).getService();
                dbCuenta = (DBCuenta) myServicio.getDb("lineaspedido");
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
    private final Handler handelerBusqueda = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            rellenarLista();
        }
    };

    private void rellenarLista() {
        ListView lst = findViewById(R.id.lstPedidosPendientes);
        lst.setAdapter(new AdaptadorBuscarPedidos(cx, lPedidos));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_pedidos);
        txtBuscar = findViewById(R.id.textBuscador);
        txtBuscar.addTextChangedListener(this);

        cx = this;
        server = getIntent().getExtras().getString("url");

    }

    public void clickServido(View v) throws JSONException {
        objSel = (JSONObject) v.getTag();
        ContentValues p = new ContentValues();
        p.put("art", objSel.toString());
        p.put("idz", objSel.getString("IDZona"));
        myServicio.addColaInstrucciones(new Instruccion(p, "/pedidos/servido"));
        dbCuenta.artServido(objSel);
        lPedidos.remove(objSel);
        rellenarLista();
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

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {


      if(charSequence.length()>0) {
          try {
              new Thread(() ->{
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  String str = charSequence.toString();
                  lPedidos = dbCuenta.filterList("Descripcion LIKE '%"+ str +"%' AND servido = 0");
                  handelerBusqueda.sendEmptyMessage(0);
              }).start();

          } catch (Exception e) {
              e.printStackTrace();
          }

      }
    }

    @Override
    public void afterTextChanged(Editable editable) { }

}
