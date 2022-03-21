package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.valleapp.comandas.Util.HTTPRequest;
import com.valleapp.comandas.Util.AdaptadorSugerencias;


public class Sugerencias extends Activity implements TextWatcher {

    String server;
    JSONObject art;
    Context cx;
    TextView txtSug;

    @SuppressLint("HandlerLeak")
    private final Handler controller_http = new Handler() {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op.equals("add")){
                if(res.trim().equals("success")){
                   Intent it = getIntent();
                   it.putExtra("art", art.toString());
                   it.putExtra("sug", txtSug.getText().toString().replace("\n",""));
                   setResult(RESULT_OK, it);
                   finish();
               }
            }else if(op.equals("sug")){
                RellenarSug(res);
            }
        }
    };

    private void RellenarSug(String res) {
        try {

            List<JSONObject> lPedidos = new ArrayList<JSONObject>();


            if(!res.equals("")) {

                JSONArray p = new JSONArray(res);


                for(int i=0; i < p.length(); i++){
                    lPedidos.add(p.getJSONObject(i));
                }


            }

            ListView lst = findViewById(R.id.lstSugerencias);
            lst.setAdapter(new AdaptadorSugerencias(cx, lPedidos));


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
            List<NameValuePair> p = new ArrayList<NameValuePair>();
            p.add(new BasicNameValuePair("id", art.getString("ID")));
            new HTTPRequest(server+"/sugerencias/ls",p,"sug", controller_http);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public void clickSugerencia(View v){
        Intent it = getIntent();
        it.putExtra("art", art.toString());
        it.putExtra("sug", v.getTag().toString());
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
                   List<NameValuePair> p = new ArrayList<>();
                  p.add(new BasicNameValuePair("sug", charSequence.toString().replace("\n", "")));
                  p.add(new BasicNameValuePair("idArt", art.getString("ID")));
                  new HTTPRequest(server + "/sugerencias/add", p, "add", controller_http);
                  txtSug.setVisibility(View.GONE);
              } else if (!charSequence.toString().contains("\n")) {
                  List<NameValuePair> p = new ArrayList<NameValuePair>();
                  p.add(new BasicNameValuePair("id", art.getString("ID")));
                  p.add(new BasicNameValuePair("str", charSequence.toString()));
                  new HTTPRequest(server + "/sugerencias/ls", p, "sug", controller_http);
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
