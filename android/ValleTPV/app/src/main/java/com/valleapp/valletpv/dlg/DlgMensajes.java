package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorCamMensajes;
import com.valleapp.valletpv.interfaces.IControlMensajes;

import org.json.JSONObject;

import java.util.List;

public class DlgMensajes extends Dialog implements IControlMensajes {

    private final IControlMensajes controlador;
    final Context cx;

    public DlgMensajes(@NonNull Context context, IControlMensajes controlMensajes) {
        super(context);
        this.controlador = controlMensajes;
        this.cx = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_mensaje);
        ImageButton btn = findViewById(R.id.btn_salir_mensajes);
        btn.setOnClickListener(v -> cancel());
        setTitle("Enviar mensajes");
    }

    @Override
    public void sendMensaje(String IDRecptor, String mensaje) {
        TextView t = findViewById(R.id.txt_mensaje);
        if (!t.getText().toString().equals("")) {
            this.controlador.sendMensaje(IDRecptor, t.getText().toString());
            cancel();
        }
    }

    public void mostrarReceptores(List<JSONObject> lista){
        ListView l = findViewById(R.id.lista_camareros_notificables);
        l.setAdapter(new AdaptadorCamMensajes(cx, lista, this));
    }


}
