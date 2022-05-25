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

import com.valleapp.valletpv.tools.HTTPRequest;
import com.valleapp.valletpv.tools.JSON;

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
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if (op.equals("cambio")) {
                try {
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
        View toast_view = LayoutInflater.from(cx).inflate(R.layout.texto_toast_simple, null);
        TextView textView = toast_view.findViewById(R.id.txt_label);
        textView.setTextSize(33);
        textView.setText(men);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 80);
        toast.show();
    }

    private void rellenarGastos() {
        gastos=0.0;
        pneGastos.removeAllViews();

        for (JSONObject gasto : objGastos) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.item_gastos, null);
            TextView can = v.findViewById(R.id.cantidad);
            TextView des = v.findViewById(R.id.Descripcion);
            ImageButton rm = v.findViewById(R.id.btn_borrar);
            rm.setTag(gasto);

            try {
            Double cantidad = gasto.getDouble("Importe");
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

    private void rellenarEfectivo(){

        efectivo = 0.0;
        pneEfectivo.removeAllViews();

        for(JSONObject e : objEfectivo) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.item_efectivo, null);
            TextView mon =  v.findViewById(R.id.txt_moneda);
            TextView can = v.findViewById(R.id.txt_cantidad);
            TextView tot = v.findViewById(R.id.Total);
            ImageButton rm = v.findViewById(R.id.btn_borrar);
            rm.setTag(e);

            try {
                Double moneda = e.getDouble("Moneda");
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
        pneGastos = (LinearLayout)findViewById(R.id.pneGastos);
        pneEfectivo = (LinearLayout)findViewById(R.id.pneEfectivo);
        txtCambio = (TextView)findViewById(R.id.lblCambio);
        txtEfectivo = (TextView)findViewById(R.id.lblEfectivo);
        txtGastos = (TextView)findViewById(R.id.lblGastos);
        this.cx = this;
    }

    public void addEfectivo(View v){
       final Dialog dlg = new Dialog(this);
       dlg.setContentView(R.layout.add_efectivo);
       dlg.setTitle("Agregar efectivo");
       ImageButton s = dlg.findViewById(R.id.btn_salir_monedas);
       ImageButton ok = dlg.findViewById(R.id.btn_aceptar_monedas);
       final TextView m = (TextView) dlg.findViewById(R.id.txtMoneda);
       final TextView c = (TextView) dlg.findViewById(R.id.txtCantidad);
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
          final TextView txtDes = (TextView) dlg.findViewById(R.id.txtDescripcion);
          final TextView imp = (TextView) dlg.findViewById(R.id.txtImporte);
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
          final TextView txtDes = (TextView) dlg.findViewById(R.id.cambio);
          s.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  dlg.cancel();
              }
          });
          ok.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                try{
                  cambio = Double.parseDouble(txtDes.getText().toString());
                  txtCambio.setText(String.format("%s €", cambio));
                  dlg.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

      public void CargarPreferencias(){
          JSON json = new JSON();
          try {
              JSONObject pref = json.deserializar("preferencias.dat", this);
              if(pref==null){
                  Intent intent = new Intent(this,PreferenciasTPV.class);
                  startActivity(intent);
              }else{
                  server = pref.getString("URL");
                  new HTTPRequest(server+"/arqueos/getcambio",new ContentValues(),"cambio", controller_http);
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
