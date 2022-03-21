package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.valleapp.comandas.Util.AdaptadorBuscarPedidos;
import com.valleapp.comandas.Util.HTTPRequest;

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


    @SuppressLint("HandlerLeak")
    private final Handler controller_http = new Handler() {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op.equals("servido")){
                lPedidos.remove(objSel);
                RellenarSug();
            }else if(op.equals("buscar")){
                lPedidos = new ArrayList<>();
                try {
                    if(!res.equals("")) {
                        JSONArray p = new JSONArray(res);
                        for (int i = 0; i < p.length(); i++) {
                            lPedidos.add(p.getJSONObject(i));
                        }

                    }
                RellenarSug();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void RellenarSug() {
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
        List<NameValuePair> p = new ArrayList<>();
        p.add(new BasicNameValuePair("art", objSel.toString()));
        p.add(new BasicNameValuePair("idz", objSel.getString("IDZona")));
        new HTTPRequest(server + "/pedidos/servido", p, "servido", controller_http);
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {


      if(charSequence.length()>0) {
          try {

              List<NameValuePair> p = new ArrayList<>();
              p.add(new BasicNameValuePair("str", charSequence.toString()));
              new HTTPRequest(server + "/pedidos/buscar", p, "buscar", controller_http);

          } catch (Exception e) {
              e.printStackTrace();
          }

      }
    }

    @Override
    public void afterTextChanged(Editable editable) { }

}
