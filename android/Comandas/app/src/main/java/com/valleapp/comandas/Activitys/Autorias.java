package com.valleapp.comandas.Activitys;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.valleapp.comandas.R;
import com.valleapp.comandas.adaptadores.AdaptadorAutorias;
import com.valleapp.comandas.utilidades.ActivityBase;
import com.valleapp.comandas.utilidades.HTTPRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Autorias extends ActivityBase {

    List<JSONObject> lsObj;
    String server;

    public  void mostrarLista(){
        try {

            ListView ls = findViewById(R.id.lista_peticiones);
            ls.setAdapter(new AdaptadorAutorias(this, lsObj));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendAutorizacion(View v){
        try {
            JSONObject o = (JSONObject) v.getTag();
            ContentValues p = new ContentValues();
            p.put("aceptada", "1");
            p.put("idpeticion", o.getString("idpeticion"));
            new HTTPRequest(server + "/autorizaciones/gestionar_peticion", p, "", null);
            lsObj.remove(o);
            mostrarLista();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendCancelacion(View v){
        try {
            JSONObject o = (JSONObject) v.getTag();
            ContentValues p = new ContentValues();
            p.put("aceptada", "0");
            p.put("idpeticion", o.getString("idpeticion"));
            new HTTPRequest(server + "/autorizaciones/gestionar_peticion", p, "", null);
            lsObj.remove(o);
            mostrarLista();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void salirAutorias(View v){
        Intent it = getIntent();
        it.putExtra("mensajes", lsObj.toString());
        setResult(RESULT_OK, it);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autorias);
        server = getIntent().getExtras().getString("url");
        String str = getIntent().getExtras().getString("peticiones");
        try {
            JSONArray obj = new JSONArray(str);
            lsObj = new ArrayList<>();
            for (int i = 0; i< obj.length(); i++){
                lsObj.add(obj.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mostrarLista();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
