package com.valleapp.comandas.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.valleapp.comandas.R;

import org.json.JSONObject;

import java.util.List;

public class AdaptadorMensajes extends ArrayAdapter<JSONObject> {

    List<JSONObject> values;
    Context cx;

    public AdaptadorMensajes(@NonNull Context context, List<JSONObject> obj) {
        super(context, R.layout.linea_simple, obj);
        this.values = obj;
        this.cx = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.linea_simple, parent, false);
        try {
            JSONObject o = values.get(position);
            TextView t = v.findViewById(R.id.labelTitle);
            RelativeLayout btn = v.findViewById(R.id.item);
            btn.setTag(o.getString("ID"));
            t.setText(o.getString("nombre"));


        }catch (Exception e){
            e.printStackTrace();
        }

        return v;
    }
}
