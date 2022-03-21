package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.valleapp.comandas.Util.Ticket;


public class Cuenta extends Activity {

    String totalMesa;
    String server = "";
    JSONObject mesa;
    ArrayList<JSONObject> lineasTicket = new ArrayList<JSONObject>();
    Context cx;


    @SuppressLint("HandlerLeak")
    private final Handler controller_cuenta = new Handler() {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op.equals("ticket")){
                RellenarTicket(res);
            }else if(op.equals("salir")){
                finish();
            }
         }
    };

    private void RellenarTicket(String res) {
        try {

            if(!res.equals("")) {

                TextView l =  findViewById(R.id.txtTotal);
                ListView lst =  findViewById(R.id.lstCuenta);
                lineasTicket.clear();

                JSONObject ticket = new JSONObject(res);
                JSONArray lineas = ticket.getJSONArray("lineas");

                totalMesa = String.format("%.2f", ticket.getDouble("total"));
                l.setText(String.format("%s €", totalMesa));


                for(int i=0; i < lineas.length(); i++){
                    lineasTicket.add(lineas.getJSONObject(i));
                }
                lst.setAdapter(new Ticket(cx, lineasTicket));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        TextView lbl = findViewById(R.id.lblMesa);
        this.cx = this;
        try {
            List<NameValuePair> p = new ArrayList<>();
            server = getIntent().getExtras().getString("url");
            mesa = new JSONObject(getIntent().getExtras().getString("mesa"));
            lbl.setText("Mesa "+mesa.getString("Nombre"));
            p.add(new BasicNameValuePair("idm",mesa.getString("ID")));
            new HTTPRequest(server+"/cuenta/ticket",p,"ticket", controller_cuenta);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clickImprimir(View v){
        if(Double.parseDouble(totalMesa.replace(",","."))>0) {
            try {
                List<NameValuePair> p = new ArrayList<>();
                p.add(new BasicNameValuePair("idm", mesa.getString("ID")));
                new HTTPRequest(server + "/impresion/preimprimir", p, "salir", controller_cuenta);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickMarcarRojo(View v){
        if(Double.parseDouble(totalMesa.replace(",","."))>0) {
            try {
                List<NameValuePair> p = new ArrayList<>();
                p.add(new BasicNameValuePair("idm", mesa.getString("ID")));
                new HTTPRequest(server + "/comandas/marcar_rojo", p, "salir", controller_cuenta);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickSalir(View v){
        finish();
    }
}
