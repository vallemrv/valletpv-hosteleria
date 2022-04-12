package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.interfaces.IControladorCuenta;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by valle on 19/10/14.
 */
public class DlgVarios extends Dialog {

    IControladorCuenta controlador;

    public DlgVarios(Context context, final IControladorCuenta controlador) {
        super(context);
        this.controlador = controlador;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.varios);
        this.setTitle("Varios ");
        final TextView can = this.findViewById(R.id.txt_varios_can);
        final TextView p =  this.findViewById(R.id.txt_varios_precio);
        final TextView nom =  this.findViewById(R.id.txt_varios_nombre);
        ImageButton ok = this.findViewById(R.id.btn_aceptar);
        ImageButton s = this.findViewById(R.id.btn_varios_salir);

        can.setText("");
        p.setText("");
        nom.setText("");

        s.setOnClickListener(view -> {
            cancel();
        });

        ok.setOnClickListener(view -> {
            if(p.getText().length()>0) {
                try {
                    String strCan = can.getText().toString().isEmpty() ? "1" : can.getText().toString();
                    JSONObject art = new JSONObject();
                    String nombre = nom.getText().toString().length()>0 ? nom.getText().toString() : "Varios";
                    art.put("ID", "0");
                    art.put("Precio", p.getText().toString().replace(",", "."));
                    art.put("Can", strCan);
                    art.put("Nombre", nombre);
                    controlador.pedirArt(art);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cancel();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        controlador.setEstadoAutoFinish(true, false);
    }
}
