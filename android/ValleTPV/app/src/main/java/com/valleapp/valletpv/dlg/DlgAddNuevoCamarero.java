package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.tools.ServicioCom;
import com.valleapp.valletpv.db.DBCamareros;

/**
 * Created by valle on 19/10/14.
 */
public class DlgAddNuevoCamarero extends Dialog {


    ServicioCom servicio;
    Context cx;

    public DlgAddNuevoCamarero(Context context, ServicioCom myService) {
        super(context);
        this.servicio = myService;
        cx = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_nuevo_camarero);
        setTitle("Crear camarero nuevo");
        final TextView nombre = this.findViewById(R.id.txt_add_cam_nombre);
        final TextView apellidos = this.findViewById(R.id.txt_add_cam_apellido);
        Button ok =  this.findViewById(R.id.btn_add_cam_aceptar);
        Button s =  this.findViewById(R.id.btn_add_cam_salir);

        s.setOnClickListener(view -> cancel());

        ok.setOnClickListener(view -> {

            try {
                String n = nombre.getText().toString();
                String a = apellidos.getText().toString();
                if (n.isEmpty() && a.isEmpty()){
                    Toast.makeText(cx, "Datos del camarero incorrectos", Toast.LENGTH_LONG).show();
                }else {
                    servicio.addCamNuevo(n, a);
                    Toast.makeText(cx, "Camarero agregado con exito", Toast.LENGTH_LONG).show();
                    servicio.getExHandler("camareros").sendEmptyMessage(0);
                    cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        });

    }
}
