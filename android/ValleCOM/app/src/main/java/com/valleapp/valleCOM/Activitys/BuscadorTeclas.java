package com.valleapp.valleCOM.Activitys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.valleapp.valleCOM.R;
import com.valleapp.valleCOM.db.DBSubTeclas;
import com.valleapp.valleCOM.db.DBTeclas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BuscadorTeclas extends Activity implements TextWatcher{

    Context cx;
    String tarifa = "1";
    JSONArray lsart = new JSONArray();
    DBTeclas dbTeclas = new DBTeclas(this);
    DBSubTeclas dbSubTeclas = new DBSubTeclas(this);
    JSONObject artSel = null;

    private final Handler handlerBusqueda = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            rellenaBotonera();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador);
        tarifa = getIntent().getExtras().getString("Tarifa");
        this.cx = this;
        TextView t = findViewById(R.id.txtBuscador);
        t.addTextChangedListener(this);
    }


    private String componerDescripcion(JSONObject o, String descipcion){
        String aux = "";
        try {
            String des = o.getString(descipcion);
            if (des != null && !des.equals("null") && !des.equals("")) {
                aux =  o.getString(descipcion);
            }else{
                aux = o.getString("Nombre");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return  aux;
    }


    private void rellenaBotonera() {

        try {

            if (lsart.length() > 0) {

                TableLayout ll =  findViewById(R.id.pneBuscador);
                ll.removeAllViews();
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,Math.round(metrics.density * 120));

                rowparams.setMargins(9, 3, 9, 3);

                TableRow row = new TableRow(cx);
                ll.addView(row, params);


                for (int i = 0; i < lsart.length(); i++) {

                    final JSONObject m = lsart.getJSONObject(i);

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.btn_art, null);


                    Button btn = v.findViewById(R.id.boton_art);

                    btn.setId(i);
                    btn.setTag(m);
                    btn.setSingleLine(false);
                    btn.setText(m.getString("Nombre").trim().replace(" ","\n"));
                    if (m.has("RGB")){
                    String[] rgb = m.getString("RGB").split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(view -> {
                        try {
                            artSel = (JSONObject) view.getTag();
                            artSel = new JSONObject(artSel.toString());
                            artSel.put("Descripcion", componerDescripcion(artSel, "descripcion_r"));
                            artSel.put("descripcion_t", componerDescripcion(artSel, "descripcion_t"));
                            if (artSel.getString("tipo").equals("SP")){
                                Intent it = getIntent();
                                it.putExtra("art", artSel.toString());
                                setResult(RESULT_OK, it);
                                finish();
                            }else{
                                lsart = dbSubTeclas.getAll(artSel.getString("ID"));
                                rellenaBotonera();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    } else{
                        btn.setBackgroundResource(R.drawable.bg_pink);
                        btn.setOnClickListener(view -> {
                            try {
                                JSONObject sub = (JSONObject) view.getTag();
                                Intent it = getIntent();
                                String des = sub.getString("descripcion_r");
                                if (des != null && !des.equals("null") && !des.equals("") ){
                                    artSel.put("Descripcion", des);
                                }else{
                                    String nom = artSel.getString("Descripcion");
                                    String subnom = sub.getString("Nombre");
                                    artSel.put("Descripcion", nom+" "+subnom);

                                }
                                des = sub.getString("descripcion_t");
                                if (des != null && !des.equals("null") && !des.equals("") ){
                                    artSel.put("descripcion_t", des);
                                }else if(artSel.getString("descripcion_t") == artSel.getString("Nombre")){
                                    String nom = artSel.getString("descripcion_t");
                                    String subnom = sub.getString("Nombre");
                                    artSel.put("descripcion_t", nom+" "+subnom);
                                }
                                it.putExtra("art", artSel.toString());
                                setResult(RESULT_OK, it);
                                finish();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        });
                    }
                    row.addView(v, rowparams);

                    if (((i + 1) % 3) == 0) {
                        row = new TableRow(cx);
                        ll.addView(row, params);
                    }
                }
            }else{
                TableLayout ll =  findViewById(R.id.pneBuscador);
                ll.removeAllViews();
            }

        } catch (Exception e) {
             e.printStackTrace();
        }


    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        if(charSequence.length()>0) {
            try {
                new Thread(() ->{
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String str = charSequence.toString();
                    lsart = dbTeclas.findLike(str, tarifa);
                    handlerBusqueda.sendEmptyMessage(0);
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
