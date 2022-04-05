package com.valleapp.comandas;

import static com.valleapp.comandas.R.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.valleapp.comandas.db.DBCamareros;
import com.valleapp.comandas.utilidades.HTTPRequest;
import com.valleapp.comandas.utilidades.Instruccion;
import com.valleapp.comandas.utilidades.JSON;
import com.valleapp.comandas.utilidades.ServicioCom;



public class Camareros extends Activity {


    private String server = "";
    ArrayList<JSONObject> lscam = null;
    final Context cx = this;

    private ServicioCom myServicio;
    private DBCamareros dbCamareros;

    private ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if(myServicio!=null) {
                myServicio.setExHandler("camareros", handlerHttp);
                dbCamareros = (DBCamareros) myServicio.getDb("camareros");
                descargarCamarerosActivos();
                mostrarListado();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler handlerHttp = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            if (op == "listado") {
                try {
                    String res = msg.getData().getString("RESPONSE");
                    dbCamareros.rellenarTabla(new JSONArray(res));
                    mostrarListado();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_camareros);
    }

    @Override
    protected void onResume() {
        cargarPreferencias();
        Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
        intent.putExtra("url", server);
        startService(intent);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        Intent intent = new Intent(cx, ServicioCom.class);
        stopService(intent);
        super.onDestroy();
    }


    @SuppressLint("SetTextI18n")
    public void mostrarListado(){
        try {
            lscam = dbCamareros.getAutorizados(true);

            if(lscam.size()>0){
                TableLayout ll = findViewById(id.pneCamareros);
                ll.removeAllViews();
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT );

                DisplayMetrics metrics = getResources().getDisplayMetrics();


                TableRow row = new TableRow(cx);
                ll.addView(row, params);

                int i = 0;
                for (JSONObject  cam: lscam) {
                    Button btnCamarero = new Button(cx);
                    btnCamarero.setBackgroundResource(drawable.bg_pink);
                    btnCamarero.setWidth(20);
                    btnCamarero.setTag(cam);
                    btnCamarero.setSingleLine(false);
                    btnCamarero.setTextSize(15);
                    btnCamarero.setText(cam.getString("Nombre")
                            .trim().replace(" ", "\n"));

                    btnCamarero.setOnClickListener(view -> {
                        try {
                            final JSONObject camSeleccionado = (JSONObject) view.getTag();
                            final String cam_pass = camSeleccionado.getString("Pass");
                            if (cam_pass.equals("") || cam_pass.equals("null")){
                                final Dialog createPass = new Dialog(cx);
                                createPass.setTitle("Crear una contraseña");
                                createPass.setContentView(layout.dialog_create_pass);
                                final TextView pass = createPass.findViewById(id.create_pass_text);
                                final TextView pass_rep = createPass.findViewById(id.repetir_pass_text);
                                createPass.findViewById(id.btnCrearPass)
                                        .setOnClickListener(v -> {
                                            try {
                                                if (!pass_rep.getText().toString().equals(pass.getText().toString())){
                                                    Toast.makeText(getApplicationContext(),"Las contraseñas no coincidén...", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    ContentValues p = new ContentValues();
                                                    p.put("cam", camSeleccionado.toString());
                                                    p.put("password", pass.getText().toString());
                                                    myServicio.addColaInstrucciones(new Instruccion(p, "/camareros/crear_password"));
                                                    createPass.dismiss();
                                                    entrarEnMesas(camSeleccionado.toString());
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        });
                                createPass.show();

                            }else{
                                final Dialog enterPass = new Dialog(cx);
                                enterPass.setContentView(layout.dialog_enter_pass);
                                enterPass.setTitle("Contraseña");
                                final TextView pass = enterPass.findViewById(id.enter_pass_text);

                                enterPass.findViewById(id.enter_pass_boton)
                                        .setOnClickListener(v -> {
                                            try {
                                                if(pass.getText().toString().equals(cam_pass)){
                                                    entrarEnMesas(camSeleccionado.toString());
                                                }else{
                                                    Toast.makeText(getApplicationContext(),
                                                            "Usuario no autorizado...",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            enterPass.dismiss();
                                        });
                                enterPass.show();

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });

                    TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            Math.round(metrics.density * 100));

                    rowparams.setMargins(5,5,5,5);
                    row.addView(btnCamarero, rowparams);

                    if (((i+1) % 3) == 0) {
                        row = new TableRow(cx);
                        ll.addView(row, params);
                    }
                    i++;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void reloadCamareros(View v){
        Toast toast= Toast.makeText(getApplicationContext(),
                "Refrescando camareros", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
        descargarCamarerosActivos();

    }

    protected void entrarEnMesas(String cam){
        Intent intent = new Intent(cx, Mesas.class);
        intent.putExtra("cam", cam);
        intent.putExtra("url", server);
        startActivity(intent);
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void descargarCamarerosActivos(){
        new HTTPRequest(server+"/camareros/listado",
                new ContentValues(),
                "listado", handlerHttp);
    }
}
