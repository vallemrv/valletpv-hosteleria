package com.valleapp.vallecom.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.valleapp.vallecom.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by valle on 14/09/14.
 */
public class AdaptadorEmpresas extends ArrayAdapter<JSONObject> {

    Context cx;
    List<JSONObject> values;

    public AdaptadorEmpresas(Context context, List<JSONObject> obj) {
        super(context, R.layout.item_empresa, obj);
        this.cx = context;this.values= obj;
      }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

         LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_empresa, parent, false);

        try {
            TextView nombre =  rowView.findViewById(R.id.nombre_empresa_label);
            JSONObject empresa = values.get(position);
            nombre.setText(String.format("%s", empresa.getString("nombre")));

            ImageView ico_activo = rowView.findViewById(R.id.icon_empresa_activa);
            if (empresa.getBoolean("activo")){
                ico_activo.setVisibility(View.VISIBLE);
            }else {
                ico_activo.setVisibility(View.GONE);
            }

            ImageButton btnRm = rowView.findViewById(R.id.boton_borrar_empresa);
            btnRm.setTag(position);

            LinearLayout btnInfo = rowView.findViewById(R.id.item_empresa);
            btnInfo.setTag(empresa);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rowView;
    }
}
