package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.valleapp.comandas.adaptadores.AdaptadorSugerencias;
import com.valleapp.comandas.db.DBSugerencias;
import com.valleapp.comandas.utilidades.HTTPRequest;


public class Sugerencias extends Activity implements TextWatcher {

    String server;
    JSONObject art;
    Context cx;
    TextView txtSug;
    DBSugerencias dbSugerencias = new DBSugerencias(this);
    JSONArray lsBusqueda = new JSONArray();

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
                rellenarSug(lsBusqueda);
        }
    };

    private void rellenarSug(JSONArray p) {
        try {

            List<JSONObject> lsug = new ArrayList<>();

            for(int i=0; i < p.length(); i++){
                lsug.add(p.getJSONObject(i));
            }

            ListView lst = findViewById(R.id.lstSugerencias);
            lst.setAdapter(new AdaptadorSugerencias(cx, lsug));


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugerencias);
        txtSug = findViewById(R.id.editText);
        txtSug.addTextChangedListener(this);

        cx = this;
        server = getIntent().getExtras().getString("url");

        try {

            art = new JSONObject(getIntent().getExtras().getString("art"));
            TextView l = findViewById(R.id.lblTitulo);
            String titulo = "Sugerencia para " + art.getString("Nombre") ;
            l.setText(titulo);
            rellenarSug(dbSugerencias.filter("IDTecla ="+ art.getString("ID")));

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public void clickSugerencia(View v){
        aceptarSug(v.getTag().toString());
    }

    private void aceptarSug(String sug){
        Intent it = getIntent();
        it.putExtra("art", art.toString());
        it.putExtra("sug", sug);
        setResult(RESULT_OK, it);
        finish();
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {


      if(charSequence.length()>0) {
          try {

              if (charSequence.toString().contains("\n")) {
                  ContentValues p = new ContentValues();
                  String sug = charSequence.toString().replace("\n", "");
                  p.put("sug", sug);
                  p.put("idArt", art.getString("ID"));
                  new HTTPRequest(server + "/sugerencias/add", p, "", null);
                  txtSug.setVisibility(View.GONE);
                  aceptarSug(sug);
              } else if (!charSequence.toString().contains("\n")) {
                  String cWhere = "IDTecla = "+art.getString("ID")+" AND sugerencia LIKE '%"+ charSequence.toString() +"%'";
                  new Thread(() -> {
                      try {
                          Thread.sleep(1000);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                      lsBusqueda = dbSugerencias.filter(cWhere);
                      handler.sendEmptyMessage(0);
                  }).start();

              }

          } catch (JSONException e) {
              e.printStackTrace();
          }

      }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

}
