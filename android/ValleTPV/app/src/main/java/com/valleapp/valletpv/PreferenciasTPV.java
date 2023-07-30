package com.valleapp.valletpv;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.valleapp.valletpv.tools.HTTPRequest;
import com.valleapp.valletpv.tools.JSON;
import com.valleapp.valletpv.tools.ServerConfig;

import org.json.JSONException;
import org.json.JSONObject;


public class PreferenciasTPV extends AppCompatActivity {

    final Context cx=this;
    String url = "";
    String code = "";
    String UID = "";
    CardView ll_codigo = null;

    final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if (op.equals("new")) {
                try {
                    JSONObject obj = new JSONObject(res);
                    code = obj.getString("codigo");
                    UID = obj.getString("UID");
                    ll_codigo.setVisibility(LinearLayout.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if (op.equals("ERROR")){
                Toast.makeText(cx, res, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias_tpv);

        final EditText txt = findViewById(R.id.txtUrl);
        final EditText txt_codigo = findViewById(R.id.txt_codigo);
        ll_codigo = findViewById(R.id.card_view);
        Button btn = findViewById(R.id.btn_aceptar_monedas);
        Button btn_codigo = findViewById(R.id.btn_validar_codigo);
        JSONObject obj = cargarPreferencias();

        if(obj!=null) {
            try {
                txt.setText(obj.getString("URL"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        btn_codigo.setOnClickListener(view -> {
            String codigo = txt_codigo.getText().toString();
            if (codigo.equals(codigo)) {
               JSONObject obj1 = new JSONObject();
                try {
                    obj1.put("code", code);
                    obj1.put("UID", UID);
                    obj1.put("url", url);
                    JSON json = new JSON();
                    json.serializar("preferencias.dat", obj1, cx);
                    Toast.makeText(cx, "Código válido", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else {
                Toast.makeText(cx, "Código no válido", Toast.LENGTH_SHORT).show();
                txt_codigo.setText("");
            }

        });

        btn.setOnClickListener(view -> {
            url = txt.getText().toString();
            ServerConfig  server = new ServerConfig(url);
            String endPoint = server.getUrl("/dispositivos/new/");
            if (endPoint == null){
                Toast.makeText(cx, "URL no válida", Toast.LENGTH_SHORT).show();
                return;
            }
            new HTTPRequest(endPoint, server.getParams(), "new", handler);
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
