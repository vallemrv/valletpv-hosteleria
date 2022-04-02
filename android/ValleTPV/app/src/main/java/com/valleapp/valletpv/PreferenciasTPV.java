package com.valleapp.valletpv;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.valleapp.valletpv.tools.JSON;

import org.json.JSONException;
import org.json.JSONObject;


public class PreferenciasTPV extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias_tpv);
        final Context cx = this;
        final EditText txt = (EditText)findViewById(R.id.txtUrl);
        Button btn = (Button)findViewById(R.id.btn_varios_aceptar);
        JSONObject obj = cargarPreferencias();

        if(obj!=null) try {
            txt.setText(obj.getString("URL"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
             String url = txt.getText().toString();
             JSONObject obj = new JSONObject();
             try {

                 obj.put("URL", url);
                 JSON json = new JSON();
                 json.serializar("preferencias.dat",obj,cx);
                 Toast.makeText(getApplicationContext(),"Datos guardados correctamente",Toast.LENGTH_SHORT).show();
                 finish();

             } catch (JSONException e) {
                e.printStackTrace();
             }
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
