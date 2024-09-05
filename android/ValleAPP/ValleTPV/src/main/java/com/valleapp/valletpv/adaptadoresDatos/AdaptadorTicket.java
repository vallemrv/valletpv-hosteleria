package com.valleapp.valletpv.adaptadoresDatos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.valleapp.valletpv.interfaces.IControladorCuenta;
import com.valleapp.valletpv.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by xbmc on 8/09/14.
 */
public class AdaptadorTicket extends ArrayAdapter<JSONObject> implements View.OnClickListener {

    private final Context context;
    private final java.util.List<JSONObject> values;
    IControladorCuenta controlador;

    public AdaptadorTicket(Context context, ArrayList<JSONObject> values, IControladorCuenta controlador) {
        super(context, R.layout.item_art, (java.util.List<JSONObject>) values);
        this.context = context;
        this.controlador = controlador;
        this.values = values;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.item_art, parent, false);

        try {
            TextView can = (TextView) rowView.findViewById(R.id.lblCan);
            TextView nombre = (TextView) rowView.findViewById(R.id.lblNombre);
            TextView p = (TextView) rowView.findViewById(R.id.lblPrecio);
            TextView t = (TextView) rowView.findViewById(R.id.lblTotal);
            can.setText(String.format("%s", values.get(position).getString("Can")));
            nombre.setText(String.format("%s",values.get(position).getString("descripcion_t")));
            p.setText(String.format("%01.2f €", values.get(position).getDouble("Precio")));
            t.setText(String.format("%01.2f €", values.get(position).getDouble("Total")));
            ImageButton rm = (ImageButton)rowView.findViewById(R.id.btn_borrar);
            rm.setTag(values.get(position));
            rm.setOnClickListener(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rowView;
    }

    @Override
    public void onClick(View view) {
        try {
            JSONObject art = (JSONObject)view.getTag();
            String estado = art.getString("Estado");
            if(estado.equals("N")) controlador.borrarArticulo(art);
            else controlador.clickMostrarBorrar(art);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
