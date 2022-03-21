package com.valleapp.comandas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.valleapp.comandas.Util.HTTPRequest;


public class MostrarPedidos extends Activity {

    String server;
    JSONObject mesa;
    JSONObject zn;

    Context cx;


    @SuppressLint("HandlerLeak")
    private final Handler controller_pedidos = new Handler() {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op.equals("salir")){
                finish();
            }else if(op.equals("pedido")){
                RellenarPedido(res);
            }else{
                Toast toast= Toast.makeText(getApplicationContext(),
                        "Peticion enviadaaaa", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
                toast.show();
            }
        }
    };

    private void RellenarPedido(String res) {

          try{

            JSONArray lineas = new JSONArray(res);
            LinearLayout ll = findViewById(R.id.pneListado);
            ll.removeAllViews();

              if(lineas.length()>0){

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(5,0,5,0);

                for (int i = 0; i < lineas.length(); i++) {

                    JSONObject  art =  lineas.getJSONObject(i);

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

                    View v = inflater.inflate(R.layout.linea_pedido_interno, null);
                    TextView t = v.findViewById(R.id.lblCantidad);
                    TextView s = v.findViewById(R.id.lblNombre);
                    t.setText(String.format("%s", art.getString("Can")));
                    s.setText(String.format("%s",art.getString("Nombre")));
                    RelativeLayout btn = v.findViewById(R.id.btnPedir);
                    ImageButton btnCamb = v.findViewById(R.id.btnCambiar);
                    btnCamb.setTag(art);
                    btn.setTag(art);
                    btn.setLongClickable(true);

                    btn.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            ((MostrarPedidos) cx).clickPedir(view);
                            return false;
                        }
                    });

                    ll.addView(v, params);
                }
           }

        }catch (Exception e){
           e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_pedidos);
        TextView lbl = findViewById(R.id.lblMesa);
        this.cx = this;
        try {
            server = getIntent().getExtras().getString("url");
            mesa = new JSONObject(getIntent().getExtras().getString("mesa"));
            lbl.setText("Mesa "+ mesa.getString("Nombre"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        try{
            List<NameValuePair> p = new ArrayList<NameValuePair>();
            p.add(new BasicNameValuePair("idm",mesa.getString("ID")));
            new HTTPRequest(server+"/comandas/lspedidos",p,"pedido", controller_pedidos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    public void clickPedir(View v){
        try{
            JSONObject obj = (JSONObject)v.getTag();
            List<NameValuePair> p = new ArrayList<>();
            p.add(new BasicNameValuePair("idp",obj.getString("IDPedido")));
            p.add(new BasicNameValuePair("id",obj.getString("IDArt")));
            p.add(new BasicNameValuePair("Nombre",obj.getString("Nombre")));
            new HTTPRequest(server+"/impresion/reenviarlinea",p,"", controller_pedidos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void clickSalir(View v){
        finish();
    }

    public void clickCambiar(View v){
       JSONObject m = (JSONObject)v.getTag();
        Intent intent = new Intent(cx,OpMesas.class);
        intent.putExtra("op","art");
        intent.putExtra("mesa", mesa.toString());
        intent.putExtra("art",m.toString());
        intent.putExtra("url",server);
        startActivity(intent);
    }
}
