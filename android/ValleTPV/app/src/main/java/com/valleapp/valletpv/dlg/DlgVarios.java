package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.valleapp.valletpv.Interfaces.IControlador;
import com.valleapp.valletpv.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by valle on 19/10/14.
 */
public class DlgVarios extends Dialog {

    IControlador controlador;

    public DlgVarios(Context context, final IControlador controlador) {
        super(context);
        this.controlador = controlador;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.varios);
        this.setTitle("Varios ");
        final TextView can = (TextView) this.findViewById(R.id.txt_varios_can);
        final TextView p = (TextView) this.findViewById(R.id.txt_varios_precio);
        final TextView nom = (TextView) this.findViewById(R.id.txt_varios_nombre);
        Button ok = (Button) this.findViewById(R.id.btn_varios_aceptar);
        ImageButton s = (ImageButton) this.findViewById(R.id.btn_varios_salir);

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
                    controlador.pedirArt(art, strCan);
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
