package com.valleapp.valletpv;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.valleapp.valletpv.db.DbTbUpdates;
import com.valleapp.valletpv.tools.JSON;

import org.json.JSONException;
import org.json.JSONObject;


public class PreferenciasTPV extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias_tpv);
        final Context cx = this;
        final EditText txt = findViewById(R.id.txtUrl);
        Button btn = findViewById(R.id.btn_aceptar_monedas);
        JSONObject obj = cargarPreferencias();

        if(obj!=null) try {
            txt.setText(obj.getString("URL"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btn.setOnClickListener(view -> {
        String url = txt.getText().toString();
        JSONObject obj1 = new JSONObject();
        try {

            obj1.put("URL", url);
            JSON json = new JSON();
            json.serializar("preferencias.dat", obj1,cx);
            Toast.makeText(getApplicationContext(),"Datos guardados correctamente",Toast.LENGTH_SHORT).show();
            DbTbUpdates db = new DbTbUpdates(cx);
            db.vaciar();
            finish();

        } catch (JSONException e) {
           e.printStackTrace();
        }
        });
    }



    private JSONObject cargarPreferencias() {
        JSON json = new JSON();
        try {
            return json.deserializar("preferencias.dat", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



}
