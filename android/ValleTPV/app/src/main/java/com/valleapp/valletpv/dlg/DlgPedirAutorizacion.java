package com.valleapp.valletpv.dlg;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageButton;
import android.widget.ListView;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorCamNotificaciones;
import com.valleapp.valletpv.db.DbCamareros;
import com.valleapp.valletpv.interfaces.IAutoFinish;
import com.valleapp.valletpv.interfaces.IBaseDatos;
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DlgPedirAutorizacion extends Dialog implements IControladorAutorizaciones {

    private final IAutoFinish controladorAutofinish;
    private final IControladorAutorizaciones controladorAutorizaciones;
    private final DbCamareros dbCamareros;
    private final JSONObject params;
    private final String accion;


    public DlgPedirAutorizacion(@NonNull Context context, IAutoFinish controladorAutofinish,
                                IBaseDatos dbCamareros,
                                IControladorAutorizaciones controladorAutorizaciones,
                                JSONObject params, String accion) {
        super(context);
        this.controladorAutofinish = controladorAutofinish;
        this.controladorAutorizaciones = controladorAutorizaciones;
        this.dbCamareros = (DbCamareros) dbCamareros;
        this.params = params;
        this.accion = accion;
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_camareos_para_notificaciones);
        setTitle("Confirmar autorizacion");
        ListView l = (ListView) findViewById(R.id.lista_camareros_notificables);
        AdaptadorCamNotificaciones ad = new AdaptadorCamNotificaciones(getContext(),
                R.layout.item_camarero_notificable,
                dbCamareros.getConPermiso(accion), this);

        ArrayList<String> p = new ArrayList<>();
        p.add("Manolo Rodriguea");
        l.setAdapter(ad);
        ImageButton btn = findViewById(R.id.btn_salir_notificaciones_camareros);
        btn.setOnClickListener(view -> {
            cancel();
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        this.controladorAutofinish.setEstadoAutoFinish(true, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                controladorAutofinish.setEstadoAutoFinish(true, true);
            }
        }, 1000);

    }

    @Override
    public void pedirAutorizacion(String id) {
        try {
            ContentValues p = new ContentValues();
            p.put("idautorizado", id);
            p.put("instrucciones", params.toString());
            p.put("accion", accion);
            controladorAutorizaciones.pedirAutorizacion(p);
            cancel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void pedirAutorizacion(ContentValues params){}
}
