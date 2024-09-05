package com.valleapp.valletpv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.cashlogyActivitis.ArqueoCashlogyActivity;
import com.valleapp.valletpvlib.tools.HTTPRequest;
import com.valleapp.valletpvlib.tools.JSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Arqueo extends Activity {

    String server;
    LinearLayout pneGastos;
    LinearLayout pneEfectivo;
    TextView txtCambio;
    TextView txtGastos;
    TextView txtEfectivo;
    Double cambio =0.0, gastos=0.0, efectivo = 0.0;
    List<JSONObject> objGastos = new ArrayList<>();
    List<JSONObject> objEfectivo = new ArrayList<>();
    Context cx;


    @SuppressLint("HandlerLeak")
    private final Handler controller_http = new Handler(Looper.getMainLooper()) {
        @SuppressLint("DefaultLocale")
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            assert op != null;
            if (op.equals("cambio")) {
                try {
                    assert res != null;
                    JSONObject obj = new JSONObject(res);
                    cambio = obj.getDouble("cambio");
                    txtCambio.setText(String.format("%.2f €", cambio));
                    if (!obj.getBoolean("hay_arqueo")) {
                        mostrarMensaje("No hay Ticket para hacer un arqueo...\n " +
                                "Este arqueo remplaza al ultimo");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if(op.equals("arqueo")){
                assert res != null;
                if(res.equals("success")) finish();
                else{
                    mostrarMensaje("No hay Ticket para hacer un arqueo...\n " +
                            "Este arqueo remplaza al ultimo");
                }
            }
        }


    };

    private void mostrarMensaje(String men){
        Toast toast = new Toast(cx);
        @SuppressLint("InflateParams") View toast_view = LayoutInflater.from(cx).inflate(R.layout.texto_toast_simple, null);
        TextView textView = toast_view.findViewById(R.id.txt_label);
        textView.setTextSize(33);
        textView.setText(men);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 80);
        toast.show();
    }

    @SuppressLint("DefaultLocale")
    private void rellenarGastos() {
        gastos=0.0;
        pneGastos.removeAllViews();

        for (JSONObject gasto : objGastos) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.item_gastos, null);
            TextView can = v.findViewById(R.id.cantidad);
            TextView des = v.findViewById(R.id.Descripcion);
            ImageButton rm = v.findViewById(R.id.btn_borrar);
            rm.setTag(gasto);

            try {
            double cantidad = gasto.getDouble("Importe");
            String descrip = gasto.getString("Des");

            if (cantidad > 0 && descrip.length() > 0) {
                can.setText(String.format("%.2f €", cantidad));
                des.setText(descrip);
                gastos += cantidad;
                pneGastos.addView(v, params);
              }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        txtGastos.setText(String.format("%.2f €", gastos));

    }

    @SuppressLint("DefaultLocale")
    private void rellenarEfectivo(){

        efectivo = 0.0;
        pneEfectivo.removeAllViews();

        for(JSONObject e : objEfectivo) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.item_efectivo, null);
            TextView mon =  v.findViewById(R.id.txt_moneda);
            TextView can = v.findViewById(R.id.txt_cantidad);
            TextView tot = v.findViewById(R.id.Total);
            ImageButton rm = v.findViewById(R.id.btn_borrar);
            rm.setTag(e);

            try {
                double moneda = e.getDouble("Moneda");
                int cantidad = e.getInt("Can");
                mon.setText(String.format("%01.2f €", moneda));
                can.setText(String.format("%s", cantidad));
                tot.setText(String.format("%01.2f €", (cantidad * moneda)));
                efectivo += cantidad * moneda;
                pneEfectivo.addView(v, params);
            } catch (JSONException x) {
                x.printStackTrace();
            }
        }
        txtEfectivo.setText(String.format("%.2f €", efectivo));
    }

    public void clickAbrirCaja(View v){
        new HTTPRequest(server+"/impresion/abrircajon",new ContentValues(),"open", controller_http);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arqueo);
        CargarPreferencias();
        pneGastos = findViewById(R.id.pneGastos);
        pneEfectivo = findViewById(R.id.pneEfectivo);
        txtCambio = findViewById(R.id.lblCambio);
        txtEfectivo = findViewById(R.id.lblEfectivo);
        txtGastos = findViewById(R.id.lblGastos);
        this.cx = this;
    }

    public void addEfectivo(View v){
       final Dialog dlg = new Dialog(this);
       dlg.setContentView(R.layout.add_efectivo);
       dlg.setTitle("Agregar efectivo");
       ImageButton s = dlg.findViewById(R.id.btn_salir_monedas);
       ImageButton ok = dlg.findViewById(R.id.btn_guardar_preferencias);
       final TextView m = dlg.findViewById(R.id.txtMoneda);
       final TextView c = dlg.findViewById(R.id.txtCantidad);
          s.setOnClickListener(view -> dlg.cancel());
          ok.setOnClickListener(view -> {
            try {
                Double moneda = Double.parseDouble(m.getText().toString().replace(",", "."));
                int cantidad = Integer.parseInt(c.getText().toString());
                if ((moneda * cantidad) > 0) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("Can", cantidad);
                        obj.put("Moneda", moneda);
                        objEfectivo.add(obj);
                        rellenarEfectivo();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                dlg.cancel();
            }catch(Exception exp){
              exp.printStackTrace();
            }
          });
          dlg.show();
      }

      public void addGastos(View v){
          final Dialog dlg = new Dialog(this);
          dlg.setContentView(R.layout.add_gastos);
          dlg.setTitle("Agregar gasto");
          ImageButton s = dlg.findViewById(R.id.Salir);
          ImageButton ok = dlg.findViewById(R.id.Aceptar);
          final TextView txtDes = dlg.findViewById(R.id.txtDescripcion);
          final TextView imp = dlg.findViewById(R.id.txtImporte);
          s.setOnClickListener(view -> dlg.cancel());
          ok.setOnClickListener(view -> {
              try {
                  Double Importe = Double.parseDouble(imp.getText().toString().replace(",", "."));
                  String des = txtDes.getText().toString();
                  if(Importe>0 &&  des.length()>0){
                          JSONObject obj = new JSONObject();
                          obj.put("Des",des);
                          obj.put("Importe", Importe);
                          objGastos.add(obj);
                          rellenarGastos();

                  }
                  dlg.cancel();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          });
          dlg.show();
      }

      public void EditCambio(View v){
          final Dialog dlg = new Dialog(this);
          dlg.setContentView(R.layout.edit_cambio);
          dlg.setTitle("Editar Cambio");
          ImageButton s = dlg.findViewById(R.id.salirCambio);
          ImageButton ok =  dlg.findViewById(R.id.aceptarCam);
          final TextView txtDes = dlg.findViewById(R.id.cambio);
          s.setOnClickListener(view -> dlg.cancel());
          ok.setOnClickListener(view -> {
            try{
              cambio = Double.parseDouble(txtDes.getText().toString());
              txtCambio.setText(String.format("%s €", cambio));
              dlg.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
          });
         dlg.show();
      }

      public void arquearCaja(View v){
           if (cambio>0) {
               ContentValues p = new ContentValues();
               p.put("cambio", Double.toString(cambio));
               p.put("efectivo", Double.toString(efectivo));
               p.put("gastos", Double.toString(gastos));
               p.put("des_efectivo", objEfectivo.toString());
               p.put("des_gastos", objGastos.toString());
               new HTTPRequest(server + "/arqueos/arquear", p, "arqueo", controller_http);
           }else{
               mostrarMensaje("El cambio debe ser mayor que 0 €");
           }

      }

    public void CargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if (pref == null) {
                Intent intent = new Intent(this, PreferenciasTPV.class);
                startActivity(intent);
            } else {
                server = pref.getString("URL");

                // Verifica si la preferencia indica que se debe usar Cashlogy
                boolean usaCashlogy = pref.getBoolean("usaCashlogy");

                if (usaCashlogy) {
                    // Solicita los datos del servidor primero
                    new HTTPRequest(server + "/arqueos/getcambio", new ContentValues(), "cambio", new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            String res = msg.getData().getString("RESPONSE");
                            if (res != null) {
                                try {
                                    JSONObject obj = new JSONObject(res);
                                    // Recoge los datos del servidor
                                    double cambio = obj.getDouble("cambio");
                                    boolean hayArqueo = obj.getBoolean("hay_arqueo");
                                    double stacke = obj.getDouble("stacke");
                                    double cambio_real = obj.getDouble("cambio_real");

                                    // Inicia ArqueoCashlogyActivity pasando los datos
                                    Intent intent = new Intent(Arqueo.this, ArqueoCashlogyActivity.class);
                                    intent.putExtra("cambio", cambio);
                                    intent.putExtra("hayArqueo", hayArqueo);
                                    intent.putExtra("stacke", stacke);
                                    intent.putExtra("cambio_real", cambio_real);

                                    // Añadir preferencias al Intent
                                    intent.putExtra("URL", server);
                                    intent.putExtra("URL_Cashlogy", pref.getString("URL_Cashlogy"));
                                    intent.putExtra("usaCashlogy", pref.getBoolean("usaCashlogy"));

                                    startActivity(intent);
                                    finish(); // Finaliza la actividad actual
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else {
                    // Si no se usa Cashlogy, continúa con la lógica normal
                    new HTTPRequest(server + "/arqueos/getcambio", new ContentValues(), "cambio", controller_http);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void clickBorrarEfc(View v){
        JSONObject obj = (JSONObject)v.getTag();
        objEfectivo.remove(obj);
        rellenarEfectivo();
    }

    public void clickBorrarGasto(View v){
        JSONObject obj = (JSONObject)v.getTag();
        objGastos.remove(obj);
        rellenarGastos();
    }

}
