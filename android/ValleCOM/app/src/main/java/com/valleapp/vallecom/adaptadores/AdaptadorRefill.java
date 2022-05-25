package com.valleapp.valleCOM.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.valleapp.valleCOM.R;

import org.json.JSONObject;

import java.util.List;

public class AdaptadorRefill extends ArrayAdapter<JSONObject> {

    List<JSONObject> values;
    Context cx;

    public AdaptadorRefill(@NonNull Context context, List<JSONObject> obj) {
        super(context, R.layout.linea_title_subtiltle, obj);
        this.values = obj;
        this.cx = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.linea_title_subtiltle, parent, false);
        try {
            JSONObject o = values.get(position);
            TextView t = v.findViewById(R.id.labelTitle);
            t.setText("Pedido "+(position+1));
            TextView s = v.findViewById(R.id.labelSubTitle);
            s.setText(o.getString("subtitle"));


        }catch (Exception e){
            e.printStackTrace();
        }

        return v;
    }
}
