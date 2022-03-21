package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.Util.AdaptadorSelCam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by valle on 19/10/14.
 */
public class DlgSelCamareros extends Dialog{


    ListView lsnoautorizados;
    ListView lstautorizados;


    private ArrayList<JSONObject> noautorizados = new ArrayList<JSONObject>();
    private ArrayList<JSONObject> autorizados = new ArrayList<JSONObject>();


    public DlgSelCamareros(Context context) {
        super(context);
        //this.controlador = controlador;
        setContentView(R.layout.seleccionar_camareros);

        ImageButton s = findViewById(R.id.salir);

        lstautorizados = findViewById(R.id.lstautorizados);

        lsnoautorizados = findViewById(R.id.lstnoautorizados);

         s.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 cancel();
             }
         });


        lsnoautorizados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        JSONObject obj = (JSONObject)view.getTag();
                        autorizados.add(obj);
                        noautorizados.remove(obj);
                        lstautorizados.setAdapter(new AdaptadorSelCam(getContext(), autorizados));
                        lsnoautorizados.setAdapter(new AdaptadorSelCam(getContext(), noautorizados));
                }
            });



        lstautorizados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    JSONObject obj = (JSONObject)view.getTag();
                    autorizados.remove(obj);
                    noautorizados.add(obj);
                    lstautorizados.setAdapter(new AdaptadorSelCam(getContext(), autorizados));
                    lsnoautorizados.setAdapter(new AdaptadorSelCam(getContext(), noautorizados));
            }
        });



    }

    public Button get_btn_ok(){
        return findViewById(R.id.aceptar);
    }

    public void setNoautorizados(JSONArray ls) throws JSONException {
        for(int i= 0;i<ls.length();i++){
            JSONObject art = ls.getJSONObject(i);
            noautorizados.add(art);
        }
        lsnoautorizados.setAdapter(new AdaptadorSelCam(getContext(), noautorizados));
    }



    public JSONArray getNoautorizados() {
        JSONArray aux = new JSONArray();
        for(JSONObject obj: noautorizados){
            aux.put(obj);
        }
        return aux;
    }


    public JSONArray getAutorizados() {
        JSONArray aux = new JSONArray();
        for(JSONObject obj: autorizados){
            aux.put(obj);
        }
        return aux;
    }

    public void setAutorizados(JSONArray ls) throws JSONException {
        for(int i= 0;i<ls.length();i++){
            JSONObject art = ls.getJSONObject(i);
            autorizados.add(art);
        }
        lstautorizados.setAdapter(new AdaptadorSelCam(getContext(), autorizados));
    }
}
