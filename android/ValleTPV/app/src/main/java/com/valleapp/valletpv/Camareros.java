/**
 * @Author: Manuel Rodriguez <valle>
 * @Date:   2018-12-29T11:26:27+01:00
 * @Email:  valle.mrv@gmail.com
 * @Last modified by:   valle
 * @Last modified time: 2018-12-29T14:52:12+01:00
 * @License: Apache License v2.0
 */
package com.valleapp.valletpv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;

import android.view.View;

import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.valleapp.valletpv.Util.JSON;
import com.valleapp.valletpv.Util.ServicioCom;
import com.valleapp.valletpv.db.DbCamareros;
import com.valleapp.valletpv.dlg.DlgSelCamareros;



public class Camareros extends Activity {

    private String server = "";
    final Context cx = this;

    JSONArray lscam = null;
    JSONObject cam_sel = null;

    JSONArray lsautorizados = new JSONArray();
    JSONArray lsnoautorizados = new JSONArray();

    DbCamareros dbCamareros = new DbCamareros(cx);
    ServicioCom myServicio;


    @SuppressLint("HandlerLeak")
    private final Handler controller_camareros = new Handler() {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            if (op == null) {
                lscam = dbCamareros.getAll();
                RellenarCamareros();
            }else if(op.equals("error")){
                try {
                    String res = msg.getData().getString("RESPONSE");
                    if (res.equals("autorizar") && cam_sel != null){
                        Intent intent = new Intent(cx, Mesas.class);
                        intent.putExtra("url", server);
                        intent.putExtra("cam", cam_sel.toString());
                        intent.putExtra("lsautorizados", lsautorizados.toString());
                        intent.putExtra("lsnoautorizados", lsnoautorizados.toString());
                        startActivity(intent);
                    }
                } catch (Exception e){ e.printStackTrace(); }
            } else {
                try {
                    String res = msg.getData().getString("RESPONSE");
                    if (op.equals("password")) {
                            JSONObject obj = new JSONObject(res);
                            Boolean autorizado = obj.getBoolean("autorizado");
                            if (autorizado) {
                                final JSONObject cam = new JSONObject(obj.getString("cam"));
                                entrar_en_mesas(cam);
                            } else {
                                Toast.makeText(getApplicationContext(), "Usuario no autorizado...", Toast.LENGTH_SHORT).show();
                            }

                    } else  if (op.equals("autorizar")){
                        JSONObject obj = new JSONObject(res);
                        final JSONObject cam = new JSONObject(obj.getString("cam"));
                        Intent intent = new Intent(cx, Mesas.class);
                        intent.putExtra("url", server);
                        intent.putExtra("cam", cam.toString());
                        intent.putExtra("lsautorizados", lsautorizados.toString());
                        intent.putExtra("lsnoautorizados", lsnoautorizados.toString());
                        startActivity(intent);
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    };

    private void RellenarCamareros() {
       try {
           if(lscam.length()>0){

               TableLayout ll = findViewById(R.id.pneCamareros);
               ll.removeAllViews();

               DisplayMetrics metrics = getResources().getDisplayMetrics();


               TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                          TableLayout.LayoutParams.MATCH_PARENT,
                          TableLayout.LayoutParams.WRAP_CONTENT);


               TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                          TableLayout.LayoutParams.MATCH_PARENT,
                          Math.round(metrics.density * 120));

               rowparams.setMargins(5,5,5,5);
               ll.setStretchAllColumns(true);

               TableRow row = new TableRow(cx);

               ll.addView(row, params);


               for (int i = 0; i < lscam.length(); i++) {
                   JSONObject  cam =  lscam.getJSONObject(i);
                   Button btn = new Button(cx);
                   btn.setId(i);
                   btn.setSingleLine(false);
                   String[] nom = cam.getString("Nombre").split(" ");

                   btn.setText(nom.length > 1 ? nom[0]+"\n"+nom[1] : cam.getString("Nombre") +"\n");
                   btn.setBackgroundResource(R.drawable.blancoxml);
                   btn.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           try {
                              final JSONObject cam =  lscam.getJSONObject(view.getId());
                              final String cam_pass = cam.getString("Pass");

                              if (cam_pass.equals("") || cam_pass.equals("null")){
                                  final Dialog createPass = new Dialog(cx);
                                  createPass.setTitle("Crear una contraseña");
                                  createPass.setContentView(R.layout.dialog_create_pass);
                                  final TextView pass = createPass.findViewById(R.id.create_pass_text);
                                  final TextView pass_rep = createPass.findViewById(R.id.repetir_pass_text);
                                  createPass.findViewById(R.id.btnCrearPass)
                                      .setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              try {
                                                  if (!pass_rep.getText().toString().equals(pass.getText().toString())){
                                                      Toast.makeText(getApplicationContext(),"Las contraseñas no coincidén...", Toast.LENGTH_SHORT).show();
                                                  }else {
                                                      if(myServicio!=null) myServicio.crear_pass(controller_camareros,
                                                              cam.toString(),
                                                              pass.getText().toString());
                                                      createPass.dismiss();
                                                  }
                                              } catch (Exception e) {
                                                  e.printStackTrace();
                                              }}
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
                                                              entrar_en_mesas(cam);
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

                      if (((i+1) % 6) == 0) {
                          row = new TableRow(cx);
                          ll.addView(row, params);
                      }
                }
           }

       }catch (Exception e){
          e.printStackTrace();
       }
    }

    protected void entrar_en_mesas(final JSONObject camarero) throws JSONException {

        cam_sel = camarero;
        final DlgSelCamareros sel_cam = new DlgSelCamareros(cx);
        sel_cam.setNoautorizados(lscam);
        sel_cam.setTitle("Elegir camareros");
        sel_cam.show();
        sel_cam.get_btn_ok().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lsautorizados = sel_cam.getAutorizados();
                lsnoautorizados = sel_cam.getNoautorizados();
                sel_cam.cancel();
                myServicio.set_lista_autorizados(controller_camareros, lsautorizados.toString(),
                                                 cam_sel.toString());
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valle_tpv);
        Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
        intent.putExtra("url", server);
        startService(intent);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if(pref==null){
                Intent intent = new Intent(this, PreferenciasTPV.class);
                startActivity(intent);
            }else{
              server = pref.getString("URL");
              lscam = dbCamareros.getAll();
              RellenarCamareros();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if(myServicio!=null) myServicio.setHandleCamareros(controller_camareros);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        cargarPreferencias();
        if (server != null && !server.equals("")) {
            Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
            intent.putExtra("url", server);
            startService(intent);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(cx, ServicioCom.class);
        stopService(intent);
        if(mConexion!=null && myServicio!=null) unbindService(mConexion);
        super.onDestroy();
    }
}
