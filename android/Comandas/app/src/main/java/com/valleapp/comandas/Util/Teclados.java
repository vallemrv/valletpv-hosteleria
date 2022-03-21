package com.valleapp.comandas.Util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.valleapp.comandas.IUtil.ITeclados;
import com.valleapp.comandas.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by valle on 12/09/14.
 */
public class Teclados extends Fragment {
    // Store instance variables

    private int[] secciones_com = {R.id.seccion_uno, R.id.seccion_dos, R.id.seccion_tres,
            R.id.seccion_cuatro, R.id.seccion_cinco, R.id.seccion_seis};

    private View panel;
    ITeclados click;
    JSONArray secciones;

    public Teclados(){ }

    @SuppressLint("ValidFragment")
    public Teclados(ITeclados click, JSONArray secciones){
        super();
        this.click = click;
        this.secciones = secciones;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        click.RellenarBotonera();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.teclados, container, false);
        panel = view.findViewById(R.id.pneArt);
        for (int i = 0; i < secciones_com.length; i++ ){
            try {
                JSONObject sec  = this.secciones.getJSONObject(i);
                ImageButton b = view.findViewById(this.secciones_com[i]);
                int d = getResources().getIdentifier(sec.getString("Icono"),"drawable",
                                                          getContext().getPackageName());
                b.setImageResource(d);
                b.setTag(sec.getString("Nombre"));
                if (sec.getBoolean("Es_promocion")){
                    b.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            click.cobrarExtra(view);
                            return false;
                        }
                    });
                }else{
                    b.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            click.AsociarBotonera(view);
                            return false;
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    public View getPanel() {
        return panel;
    }
}
