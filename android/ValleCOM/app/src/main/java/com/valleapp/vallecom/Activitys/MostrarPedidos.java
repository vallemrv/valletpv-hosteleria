package com.valleapp.vallecom.Activitys;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.valleapp.vallecom.R;
import com.valleapp.vallecom.db.DBCuenta;
import com.valleapp.vallecom.db.DBMesas;
import com.valleapp.vallecom.utilidades.ActivityBase;
import com.valleapp.vallecom.utilidades.HTTPRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MostrarPedidos extends ActivityBase {

    String server;
    JSONObject mesa;
    DBCuenta dbCuenta;
    Context cx;
    boolean pause = false;


    @SuppressLint("HandlerLeak")
    private final Handler handlerHttp = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op.equals("actualizar")){
                try {
                    dbCuenta.actualizarMesa(new JSONArray(res), mesa.getString("ID"));
                    rellenarPedido();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                mostrarToast("Peticion envidada....");
            }
        }
    };

    private void rellenarPedido() {

        try{
            JSONArray lineas = dbCuenta.filterByPedidos("IDMesa = "+mesa.getString("ID"));

            LinearLayout ll = findViewById(R.id.pneListado);
            ll.removeAllViews();

            if(lineas.length()>0){

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (metrics.density * 65f));

                params.setMargins(5,0,5,0);


                for (int i = 0; i < lineas.length(); i++) {

                    JSONObject  art =  lineas.getJSONObject(i);

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.linea_pedido_interno, null);
                    TextView t = v.findViewById(R.id.lblCantidad);
                    TextView s = v.findViewById(R.id.lblNombre);
                    t.setText(String.format("%s", art.getString("Can")));
                    s.setText(String.format("%s",art.getString("Descripcion")));
                    RelativeLayout btn = v.findViewById(R.id.btnPedir);
                    ImageButton btnCamb = v.findViewById(R.id.btnCambiar);
                    btnCamb.setTag(art);
                    btn.setTag(art);
                    btn.setLongClickable(true);

                    btn.setOnLongClickListener(view -> {
                        clickPedir(view);
                        return false;
                    });

                    ll.addView(v, params);
                }
            }else {
                DBMesas db = new DBMesas(cx);
                db.cerrarMesa(mesa.getString("ID"));
                finish();
            }

        }catch (Exception e){
           e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_pedidos);
        TextView lbl = findViewById(R.id.lblMesa);
        this.cx = this;
        dbCuenta = new DBCuenta(this);
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
            rellenarPedido();
            /*String idm = mesa.getString("ID");
            if(!pause) {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ContentValues p = new ContentValues();
                        p.put("mesa_id", idm);
                        new HTTPRequest(server + "/cuenta/get_cuenta", p, "actualizar", handlerHttp);
                    }
                }, 1000);
            }
            pause = false;*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        pause = true;
        super.onPause();
    }

    public void clickPedir(View v){
        try{
            JSONObject obj = (JSONObject)v.getTag();
            ContentValues p = new ContentValues();
            p.put("idp",obj.getString("IDPedido"));
            p.put("id",obj.getString("IDArt"));
            p.put("Descripcion",obj.getString("Descripcion"));
            new HTTPRequest(server+"/impresion/reenviarlinea",p,"", handlerHttp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clickCambiar(View v){
        JSONObject m = (JSONObject)v.getTag();
        Intent intent = new Intent(cx, OpMesas.class);
        intent.putExtra("op","art");
        intent.putExtra("mesa", mesa.toString());
        intent.putExtra("art",m.toString());
        intent.putExtra("url",server);
        startActivity(intent);
    }

    public  void clickSalir(View v){
        finish();
    }
}
