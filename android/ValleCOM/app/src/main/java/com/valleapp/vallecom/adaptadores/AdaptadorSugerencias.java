package com.valleapp.vallecom.adaptadores;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.valleapp.vallecom.R;

/**
 * Created by valle on 17/09/14.
 */
public class AdaptadorSugerencias extends ArrayAdapter<JSONObject> {

    Context cx;
    List<JSONObject> values;

    public AdaptadorSugerencias(Context context, List<JSONObject> obj) {
        super(context, R.layout.linea_simple, obj);
        this.cx = context;
        this.values= obj;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) cx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.linea_simple, parent, false);

        try {
            TextView can = (TextView) rowView.findViewById(R.id.labelTitle);
            // Set the text size 25 dip for ListView each item
            can.setTextSize(TypedValue.COMPLEX_UNIT_DIP,25);

            String sug = values.get(position).getString("sugerencia");
            can.setText(String.format("%s", sug));
            RelativeLayout btnInfo = (RelativeLayout)rowView.findViewById(R.id.item);
            btnInfo.setTag(sug);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rowView;
    }

}
