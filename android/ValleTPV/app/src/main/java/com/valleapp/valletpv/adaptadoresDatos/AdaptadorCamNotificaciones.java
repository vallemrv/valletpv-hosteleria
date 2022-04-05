package com.valleapp.valletpv.adaptadoresDatos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones;

import org.json.JSONObject;

import java.util.List;

public class AdaptadorCamNotificaciones extends ArrayAdapter<JSONObject> {


    private final List<JSONObject> objects;
    private IControladorAutorizaciones controlador;

    public AdaptadorCamNotificaciones(Context context, int resource,
                                      List<JSONObject> objects,
                                      IControladorAutorizaciones controlador) {
        super(context, resource, objects);
        this.objects = objects;
        this.controlador = controlador;
    }


    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View v = inflater.inflate(R.layout.item_camarero_notificable, parent, false);
        try {
            TextView n = (TextView) v.findViewById(R.id.txt_nombre_camarero_notificaciones);
            JSONObject o = objects.get(position);
            Log.i("ADAPTADOR", o.toString());
            n.setText(o.getString("Nombre"));
            ImageView btn = (ImageView) v.findViewById(R.id.btn_send_cam_autorizado);
            btn.setTag(o.getString("ID"));
            btn.setOnClickListener(view -> {
                  controlador.pedirAutorizacion(view.getTag().toString());
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return v;
    }
}