package com.valleapp.vallecom.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.valleapp.vallecom.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by valle on 14/09/14.
 */
public class AdaptadorBuscarPedidos extends ArrayAdapter<JSONObject> {

    Context cx;
    List<JSONObject> values;

    public AdaptadorBuscarPedidos(Context context, List<JSONObject> obj) {
        super(context, R.layout.linea_pedido_externo, obj);
        this.cx = context;this.values= obj;
      }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

         LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.linea_pedido_externo, parent, false);

        try {
            TextView nombre = rowView.findViewById(R.id.lblNombre);
            TextView cantidad = rowView.findViewById(R.id.lblCantidad);
            JSONObject art = values.get(position);
            nombre.setText(String.format("%s - %s",art.getString("Descripcion"), art.getString(("nomMesa"))));
            cantidad.setText(String.format("%s",art.getString("Can")));

            ImageButton btnRm = rowView.findViewById(R.id.btnBorrarPedido);
            if (!art.getString("servido").equals("0")) btnRm.setVisibility(View.GONE);
            btnRm.setTag(art);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rowView;
    }
}
