package com.valleapp.valletpv.adaptadoresDatos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.valleapp.valletpv.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by valle on 14/09/14.
 */
public class AdaptadorSelCam extends ArrayAdapter<JSONObject> {

    Context cx;
    List<JSONObject> values;

    public AdaptadorSelCam(Context context, List<JSONObject> obj) {
        super(context, R.layout.item_simple, obj);
        this.cx = context;
        this.values= obj;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_simple, parent, false);

        try {
            JSONObject obj = values.get(position);
            TextView nombre = (TextView) rowView.findViewById(R.id.texto_linea);
            nombre.setText(String.format("%s",obj.getString("Nombre")));
            rowView.setTag(obj);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rowView;
    }
}
