package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.valleapp.comandas.Util.HTTPRequest;
import com.valleapp.comandas.Util.JSON;
import com.valleapp.comandas.Util.ServicioCom;


public class Camareros extends Activity {


    private String server = "";
    JSONArray lscam = null;
    final Context cx = this;


    @SuppressLint("HandlerLeak")
    private final Handler controller_camareros = new Handler(){
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op.equals("password")){
                try{
                    JSONObject obj = new JSONObject(res);
                    Boolean autorizado = obj.getBoolean("autorizado");
                    if (autorizado){
                        entrar_en_mesas(obj.getString("cam"));
                    }else {
                        Toast.makeText(getApplicationContext(),"Usuario no autorizado...", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }


            } else if(op.equals("listado")){
              try {
                lscam = new JSONArray(res);
                if(lscam.length()>0){
                    TableLayout ll = findViewById(R.id.pneCamareros);
                    ll.removeAllViews();
                    TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );

                    DisplayMetrics metrics = getResources().getDisplayMetrics();

                    TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            Math.round(metrics.density * 100));

                    rowparams.setMargins(5,5,5,5);
                    TableRow row = new TableRow(cx);
                    ll.addView(row, params);

                    for (int i = 0; i < lscam.length(); i++) {
                        JSONObject  cam =  lscam.getJSONObject(i);
                        Button btn = new Button(cx);
                        btn.setId(i);
                        btn.setSingleLine(false);
                        btn.setTextSize(15);
                        btn.setText(cam.getString("Nombre")
                                .trim().replace(" ", "\n") + "\n" + cam.getString("Apellidos")
                                .trim().replace(" ", "\n"));

                        btn.setBackgroundColor(Color.rgb(70, 80, 90));
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                                try {
                                    final JSONObject cam =  lscam.getJSONObject(view.getId());
                                    final String cam_pass = cam.getString("Pass");
                                    if (cam_pass.equals("") || cam_pass.equals("null")){
                                        final Dialog createPass = new Dialog(cx);
                                        createPass.setTitle("Crear una contraseña");
                                        createPass.setContentView(R.layout.dialog_create_pass);
                                        final TextView pass = createPass.findViewById(R.id.create_pass_text);
                                        final TextView pass_rep = createPass.findViewById(R.id.repetir_pass_text);
                                        ((Button) createPass.findViewById(R.id.btnCrearPass))
                                                .setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        try {
                                                            if (!pass_rep.getText().toString().equals(pass.getText().toString())){
                                                                Toast.makeText(getApplicationContext(),"Las contraseñas no coincidén...", Toast.LENGTH_SHORT).show();
                                                            }else {
                                                                ArrayList<NameValuePair> p = new ArrayList<>();
                                                                p.add(new BasicNameValuePair("cam", lscam.getString(view.getId())));
                                                                p.add(new BasicNameValuePair("password", pass.getText().toString()));
                                                                new HTTPRequest(server + "/camareros/crear_password",
                                                                        p, "password", controller_camareros);
                                                                createPass.dismiss();
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                        createPass.show();

                                    }else{
                                        final Dialog enterPass = new Dialog(cx);
                                        enterPass.setContentView(R.layout.dialog_enter_pass);
                                        enterPass.setTitle("Contraseña");
                                        final TextView pass = enterPass.findViewById(R.id.enter_pass_text);

                                        enterPass.findViewById(R.id.enter_pass_boton)
                                                .setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        try {
                                                            if(pass.getText().toString().equals(cam_pass)){
                                                                entrar_en_mesas(cam.toString());
                                                            }else{
                                                                Toast.makeText(getApplicationContext(),
                                                                        "Usuario no autorizado...",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        enterPass.dismiss();
                                                    }
                                                });
                                        enterPass.show();

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        row.addView(btn, rowparams);

                        if (((i+1) % 3) == 0) {
                            row = new TableRow(cx);
                            ll.addView(row, params);
                        }
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
          }

        }
    };

    public void reloadCamareros(View v){
        Toast toast= Toast.makeText(getApplicationContext(),
                "Refrescando camareros", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
        new HTTPRequest(server+"/camareros/listado",
                new ArrayList<NameValuePair>(),
                "listado", controller_camareros);
    }

    protected void entrar_en_mesas(String camarero_id){
        Intent datos = new Intent(getApplicationContext(),ServicioCom.class);
        datos.putExtra("server", server);
        datos.putExtra("camarero_id", camarero_id);
        startService(datos);
        Intent intent = new Intent(cx, Mesas.class);
        intent.putExtra("cam", camarero_id);
        intent.putExtra("url", server);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camareros);
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if(pref==null){
                Intent intent = new Intent(this, Preferencias.class);
                startActivity(intent);
            }else{
                server = pref.getString("URL");
                new HTTPRequest(server+"/camareros/listado",
                                 new ArrayList<NameValuePair>(),
                                "listado", controller_camareros);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        cargarPreferencias();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
