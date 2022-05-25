package com.valleapp.valleCOM.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.valleapp.valleCOM.R;

import org.json.JSONObject;

import java.util.List;

public class AdaptadorAutorias extends ArrayAdapter<JSONObject> {

    List<JSONObject> values;
    Context cx;

    public AdaptadorAutorias(@NonNull Context context, List<JSONObject> obj) {
        super(context, R.layout.linea_autoria, obj);
        this.values = obj;
        this.cx = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.linea_autoria, parent, false);
        try {
            JSONObject o = values.get(position);
            TextView t = v.findViewById(R.id.txt_mensaje_autoria);
            ImageButton b = v.findViewById(R.id.btn_autorizar);
            if(!o.getString("tipo").equals("informacion")) {
                b.setTag(o);
            }else{
                b.setVisibility(View.GONE);
            }
            ImageButton bc = v.findViewById(R.id.btn_denegar);
            bc.setTag(o);
            t.setText(o.getString("mensaje"));

        }catch (Exception e){
            e.printStackTrace();
        }

        return v;
    }
}
