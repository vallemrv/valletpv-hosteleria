package com.valleapp.vallecom.Activitys;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.valleapp.vallecom.R;
import com.valleapp.vallecom.adaptadores.AdaptadorComanda;
import com.valleapp.vallecom.adaptadores.AdaptadorPedidos;
import com.valleapp.vallecom.db.DBCuenta;
import com.valleapp.vallecom.db.DBMesas;
import com.valleapp.vallecom.db.DBSecciones;
import com.valleapp.vallecom.db.DBSubTeclas;
import com.valleapp.vallecom.db.DBTeclas;
import com.valleapp.vallecom.interfaces.IComanda;
import com.valleapp.vallecom.interfaces.INota;
import com.valleapp.vallecom.interfaces.ITeclados;
import com.valleapp.vallecom.pestañas.Comanda;
import com.valleapp.vallecom.pestañas.SeccionesCom;
import com.valleapp.vallecom.utilidades.ActivityBase;
import com.valleapp.vallecom.utilidades.Instruccion;
import com.valleapp.vallecom.utilidades.JSON;
import com.valleapp.vallecom.utilidades.Nota;
import com.valleapp.vallecom.utilidades.ServicioCom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HacerComandas extends ActivityBase implements  INota, IComanda, ITeclados {

    private Comanda comanda = null;
    private SeccionesCom seccionesCom = null;
    private String server = "";

    DBMesas dbMesas;
    DBTeclas dbTeclas;
    DBSubTeclas dbSubTeclas;
    DBSecciones dbSecciones;
    DBCuenta dbCuenta;

    Context cx = null;
    JSONObject cam = null;
    JSONObject mesa = null;

    int can = 1;
    int tarifa = 1;

    JSONObject sec = null;

    JSONObject artSel ;
    Nota nota;

    TextView cantidad;
    TextView infPedio;
    ServicioCom myServicio = null;
    boolean es_aplicable = true;
    boolean pause = false;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if (myServicio != null){
                dbMesas = (DBMesas) myServicio.getDb("mesas");
                dbSecciones = (DBSecciones) myServicio.getDb("seccionescom");
                dbTeclas = (DBTeclas) myServicio.getDb("teclas");
                dbSubTeclas = (DBSubTeclas) myServicio.getDb("subteclas");
                dbCuenta = (DBCuenta) myServicio.getDb("lineaspedido");
                comanda = new Comanda((IComanda) cx);
                seccionesCom = new SeccionesCom((ITeclados) cx, dbSecciones.getAll());
                AdaptadorComanda aComanda = new AdaptadorComanda(getSupportFragmentManager(), comanda, seccionesCom);
                cargarPreferencias();
                ViewPager vpPager = findViewById(R.id.pager);
                TextView title = findViewById(R.id.lblTitulo);
                vpPager.setAdapter(aComanda);
                try {
                    title.setText(cam.getString("Nombre") + " -- " + mesa.getString("Nombre"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };


    public  void cargarNota(){
        nota = new Nota(mesa, this,this);
        rellenarComanda();
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if(!pref.isNull("sec")) {
                sec = dbSecciones.filter("Nombre = '"+pref.getString("sec")+"'").getJSONObject(0);
            }else{
                sec = dbSecciones.getAll().optJSONObject(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    public void rellenarComanda() {
        List<JSONObject> lPedidos = new ArrayList<>();
        comanda.setCantidad("0");

        if(nota.getNum() > 0) {
            String num = Integer.toString(nota.getNum());
            lPedidos = nota.getLineas();
            comanda.setCantidad(num);
            infPedio.setText(num + " articulos");

        } else  infPedio.setText("Ningun articulo");

        comanda.getLista().setAdapter(new AdaptadorPedidos(cx, lPedidos));
        can = 1;
        cantidad.setText(Integer.toString(can));

    }

    public void rellenarBotonera() {
        try {
            if (dbTeclas != null && sec != null) {
                JSONArray lsart = dbTeclas.getAll(sec.getString("ID"), tarifa);
                LinearLayout ll = (LinearLayout) seccionesCom.getPanel();
                ll.removeAllViews();

                if (lsart.length() > 0) {


                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    rowParams.weight = 1;
                    LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    colParams.weight = 1;
                    colParams.setMargins(5, 5, 5, 5);


                    LinearLayout row = new LinearLayout(cx);
                    row.setOrientation(LinearLayout.HORIZONTAL);

                    ll.addView(row, rowParams);

                    for (int i = 0; i < lsart.length(); i++) {

                        final JSONObject a = lsart.getJSONObject(i);

                        LayoutInflater inflater = (LayoutInflater) cx.getSystemService
                                (Context.LAYOUT_INFLATER_SERVICE);
                        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.btn_art, null);

                        Button btn = v.findViewById(R.id.boton_art);
                        if (!a.getString("RGB").equals("")) {
                             String[] rgb = a.getString("RGB").split(",");
                             btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
                        }else{
                            btn.setBackgroundResource(R.drawable.bg_pink);
                        }
                        btn.setId(i);
                        btn.setSingleLine(false);


                        if (sec.getBoolean("es_promocion")) {
                            if (!es_aplicable) btn.setBackgroundResource(R.drawable.bg_red);
                            else {
                                double precio = a.getDouble("Precio");
                                double descuento = sec.getDouble("descuento");
                                a.put("Precio", precio - ((precio * descuento) /100));
                            }
                        }

                        String nombre = a.getString("Nombre");
                        btn.setText(nombre.trim());
                        btn.setTag(new JSONObject(a.toString()));


                        btn.setOnClickListener(view -> {
                            findViewById(R.id.btn_reffil).setVisibility(View.GONE);
                            JSONObject art = (JSONObject) view.getTag();
                            pedirArt(art);
                        });

                        row.addView(v, colParams);

                        if ((i < lsart.length() - 1) && ((i + 1) % 3) == 0) {
                            row = new LinearLayout(cx);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            ll.addView(row, rowParams);
                        }
                    }
                }

            }
            } catch(Exception e){
                e.printStackTrace();
            }


    }

    private void rellenarSub() {

        try {

            JSONArray lsart = dbSubTeclas.getAll(artSel.getString("ID"));
            LinearLayout ll = (LinearLayout) seccionesCom.getPanel();
            ll.removeAllViews();
            int length = lsart.length();
            if ( length > 0) {


                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int w = (int) (metrics.density * 100);
                int ww = 0;
                if (length > 9 ){
                     w = LinearLayout.LayoutParams.MATCH_PARENT;
                     ww = 1;
                }

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, w);
                rowParams.weight = ww;

                LinearLayout.LayoutParams colParms = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );

                colParms.weight = 1;

                colParms.setMargins(5,5,5,5);

                LinearLayout row = new LinearLayout(cx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                ll.addView(row, rowParams);

                for (int i = 0; i < lsart.length(); i++) {

                    JSONObject m = lsart.getJSONObject(i);

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.btn_art, null);


                    Button btn = v.findViewById(R.id.boton_art);

                    btn.setId(i);
                    btn.setTag(m);
                    btn.setSingleLine(false);
                    btn.setText(m.getString("Nombre"));
                    btn.setOnClickListener(view -> addSubTecla((JSONObject)view.getTag()));
                    btn.setBackgroundResource(R.drawable.bg_pink);
                    row.addView(v, colParms);

                    if ((i<lsart.length()-1) && ((i + 1) % 3) == 0) {
                        row = new LinearLayout(cx);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        ll.addView(row, rowParams);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private String componerDescripcion(JSONObject o, String descipcion){
        String aux = "";
        try {
            String des = o.getString(descipcion);
            if (!des.equals("null") && !des.equals("")) {
                aux =  o.getString(descipcion);
            }else{
                aux = o.getString("Nombre");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return  aux;
    }


    private void addSubTecla(JSONObject sub){

        try {
            String des = sub.getString("descripcion_r");
            if (!des.equals("null") && !des.equals("")){
                artSel.put("Descripcion", des);
            }else{
                String nom = artSel.getString("Descripcion");
                String subnom = sub.getString("Nombre");
                artSel.put("Descripcion", nom+" "+subnom);

            }
            des = sub.getString("descripcion_t");
            if (!des.equals("null") && !des.equals("")){
                artSel.put("descripcion_t", des);
            }else if(artSel.getString("descripcion_t").equals(artSel.getString("Nombre"))){
                String nom = artSel.getString("descripcion_t");
                String subnom = sub.getString("Nombre");
                artSel.put("descripcion_t", nom+" "+subnom);
            }
            mostrarToast(artSel.getString("Descripcion"));
            double d = artSel.getDouble("Precio");
            double inc = sub.getDouble("Incremento");
            artSel.put("Precio", d+inc);
            nota.addArt(artSel, can);
            rellenarBotonera();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void pedirArt(JSONObject art) {
        try {

            JSONObject aux = new JSONObject(art.toString());
            aux.put("Descripcion", componerDescripcion(aux, "descripcion_r"));
            aux.put("descripcion_t", componerDescripcion(aux, "descripcion_t"));

            if(art.getString("tipo").equals("SP")){
                mostrarToast(art.getString("Nombre"));
                nota.addArt(aux, can);
            }else{
                this.artSel = aux;
                rellenarSub();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clickMenu(View v) throws JSONException {
        findViewById(R.id.btn_reffil).setVisibility(View.GONE);
        sec = dbSecciones.filter("Nombre = '"+v.getTag().toString()+"'").getJSONObject(0);
        rellenarBotonera();
    }

    @SuppressLint("SetTextI18n")
    public void clickCan(View v){
        can = Integer.parseInt(v.getTag().toString());
        cantidad.setText(Integer.toString(can));
    }

    public void clickEnviarComanda(View v){
        try{
          ContentValues p = new ContentValues();
          List<JSONObject> nl = nota.getLineas();
          String idm = mesa.getString("ID");
          String idc = cam.getString("ID");
          String nom_mesa = mesa.getString("Nombre");
          p.put("idm", idm);
          p.put("pedido", nl.toString());
          p.put("idc", idc);
           if(myServicio!=null){
              nota.eliminarComanda();
              myServicio.addColaInstrucciones(new Instruccion(p, "/comandas/pedir"));
              dbCuenta.addArt(idm, nl, idc, nom_mesa);
              dbMesas.abrirMesa(mesa.getString("ID"), "0");
              finish();
          }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onBorrarLinea(View v){
        nota.rmArt((JSONObject)v.getTag());
    }

    public void clickSugerencia(View v){
        pause = true;
        JSONObject art = (JSONObject)v.getTag();
        Intent intent = new Intent(cx, Sugerencias.class);
        intent.putExtra("url", server);
        intent.putExtra("art",nota.getArt(art));
        startActivityForResult(intent, 200);
    }

    public void clickBuscarArticulo(View v){
        pause = true;
        Intent intent = new Intent(cx, BuscadorTeclas.class);
        intent.putExtra("Tarifa", String.valueOf(tarifa));
        startActivityForResult(intent, 100);
    }

    public void onRefill(View view){
        try {
            pause = true;
            Intent intent = new Intent(cx, Refill.class);
            intent.putExtra("id_mesa", mesa.getString("ID"));
            startActivityForResult(intent, 300);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void asociarBotonera(View view) {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            pref.put("sec",view.getTag().toString());
            json.serializar("preferencias.dat", pref, cx);
            mostrarToast("Asociacion realizada");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hacer_comanda);

        cantidad = findViewById(R.id.listaPedidoComanda);
        infPedio = findViewById(R.id.lblPedido);

        cx = this;

        try {

            server = getIntent().getExtras().getString("url");
            cam = new JSONObject(getIntent().getExtras().getString("cam"));
            mesa = new JSONObject(getIntent().getExtras().getString("mesa"));
            tarifa = mesa.getInt("Tarifa");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myServicio == null) {
            Intent intent = new Intent(getBaseContext(), ServicioCom.class);
            intent.putExtra("server", server);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        }else {
            cargarPreferencias();
        }
    }

    @Override
    public void onBackPressed() {
        //nota.eliminarComanda();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if(resultCode == RESULT_OK){
               try{
                   JSONObject art = new JSONObject(data.getStringExtra("art"));
                   nota.addArt(art, can);
                   } catch (JSONException e) {
                     e.printStackTrace();
               }
            }

        }else if (requestCode == 200) {
            if(resultCode == RESULT_OK){
                String sug = data.getStringExtra("sug");
                nota.addSug(sug);
            }

        }
        else if (requestCode == 300) {
            if(resultCode == RESULT_OK){
                String idpedido = data.getStringExtra("IDPedido");
                List<JSONObject> lPedidos = dbCuenta.filterList("estado = 'P' AND IDPedido = "+idpedido, false);
                for(JSONObject art: lPedidos){
                    try {
                        nota.addArt(art, art.getInt("Can"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
             }
       }
        pause = false;
    }

    @Override
    protected void onDestroy() {
        if(myServicio != null) unbindService(mConexion);
        super.onDestroy();
    }

    @Override
    public void cobrarExtra(View view){
        es_aplicable = !es_aplicable;
        rellenarBotonera();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}