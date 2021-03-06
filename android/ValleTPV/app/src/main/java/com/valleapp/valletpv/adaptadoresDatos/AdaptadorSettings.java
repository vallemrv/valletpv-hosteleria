package com.valleapp.valletpv.adaptadoresDatos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;

import com.valleapp.valletpv.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by valle on 14/09/14.
 */
public class AdaptadorSettings extends ArrayAdapter<JSONObject> {

    Context cx;
    List<JSONObject> values;
    public JSONArray lista = new JSONArray();

    public AdaptadorSettings(Context context, List<JSONObject> obj) {
        super(context, R.layout.item_simple, obj);
        this.cx = context; this.values= obj;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.item_settings, parent, false);

        try {
            JSONObject art = values.get(position);
            lista.put(art);
            Boolean activo = art.getBoolean("Activo");
            Switch linea = (Switch) rowView.findViewById(R.id.linea_settings);
            linea.setChecked(activo);
            linea.setText(String.format("%s", art.getString("Nombre")));
            linea.setTag(art);
            linea.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    JSONObject obj = (JSONObject) buttonView.getTag();
                    obj.put("Activo", isChecked);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rowView;
    }
}
