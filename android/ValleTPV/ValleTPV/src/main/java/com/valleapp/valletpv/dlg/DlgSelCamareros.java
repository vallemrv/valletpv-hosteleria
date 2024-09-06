package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSelCam;
import com.valleapp.valletpv.db.DBCamareros;
import com.valleapp.valletpv.interfaces.IAutoFinish;
import com.valleapp.valletpv.tools.ServicioCom;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by valle on 19/10/14.
 */
public class DlgSelCamareros extends Dialog{


    private final ServicioCom servicio;
    private final IAutoFinish controlador;


    ListView lsnoautorizados;
    ListView lstautorizados;


    private ArrayList<JSONObject> noautorizados = new ArrayList<>();
    private ArrayList<JSONObject> autorizados = new ArrayList<>();


    public DlgSelCamareros(Context context, ServicioCom servicio, boolean mostrar_add, IAutoFinish controlador) {
        super(context);
        this.servicio = servicio;
        this.controlador = controlador;
        setContentView(R.layout.seleccionar_camareros);

        ImageButton s = findViewById(R.id.salir);

        if (!mostrar_add) findViewById(R.id.btn_add_nuevo_camarero).setVisibility(View.GONE);

        lstautorizados = findViewById(R.id.lstautorizados);
        lsnoautorizados = findViewById(R.id.lstnoautorizados);

        s.setOnClickListener(view -> cancel());


        lsnoautorizados.setOnItemClickListener((adapterView, view, i, l) -> {

            try {
                JSONObject obj = (JSONObject)view.getTag();
                autorizados.add(obj);
                noautorizados.remove(obj);
                obj.put("autorizado", "1");
                servicio.autorizarCam(obj);
                DBCamareros db = (DBCamareros) servicio.getDb("camareros");
                db.setAutorizado(obj.getInt("ID"), true);
                lstautorizados.setAdapter(new AdaptadorSelCam(getContext(), autorizados));
                lsnoautorizados.setAdapter(new AdaptadorSelCam(getContext(), noautorizados));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });



        lstautorizados.setOnItemClickListener((adapterView, view, i, l) -> {
            try {
                JSONObject obj = (JSONObject)view.getTag();
                autorizados.remove(obj);
                noautorizados.add(obj);
                obj.put("autorizado", "0");
                servicio.autorizarCam(obj);
                DBCamareros db = (DBCamareros) servicio.getDb("camareros");
                db.setAutorizado(obj.getInt("ID"), false);
                lstautorizados.setAdapter(new AdaptadorSelCam(getContext(), autorizados));
                lsnoautorizados.setAdapter(new AdaptadorSelCam(getContext(), noautorizados));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });



    }

    public ImageButton get_btn_ok(){
        return findViewById(R.id.aceptar);
    }

    public void setNoautorizados(ArrayList<JSONObject> ls) {
        noautorizados = ls;
        lsnoautorizados.setAdapter(new AdaptadorSelCam(getContext(), ls));
    }


    public void setAutorizados(ArrayList<JSONObject> ls) {
        autorizados = ls;
        lstautorizados.setAdapter(new AdaptadorSelCam(getContext(), ls));
    }

    @Override
    protected void onStop() {
        super.onStop();
        controlador.setEstadoAutoFinish(true, false);
    }
}
