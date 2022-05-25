package com.valleapp.valleCOM.utilidades;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valleCOM.R;
import com.valleapp.valleCOM.db.DBCamareros;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActivityBase extends FragmentActivity {

    protected String server = null;
    protected final Context cx = this;
    protected ServicioCom myServicio;
    protected JSONObject cam = null;

    protected void mostrarToast(String texto){
        Toast toast = new Toast(cx);
        View toast_view = LayoutInflater.from(cx).inflate(R.layout.texto_toast_simple, null);
        TextView textView = toast_view.findViewById(R.id.txt_label);
        textView.setText(texto);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 80);
        toast.show();
    }

    protected void mostrarToast(String texto, int gravity, int xOffset, int yOffset){
        Toast toast = new Toast(cx);
        View toast_view = LayoutInflater.from(cx).inflate(R.layout.texto_toast_simple, null);
        TextView textView = toast_view.findViewById(R.id.txt_label);
        textView.setText(texto);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

    protected void mostrarToast(String texto, int gravity){
        Toast toast = new Toast(cx);
        View toast_view = LayoutInflater.from(cx).inflate(R.layout.texto_toast_simple, null);
        TextView textView = toast_view.findViewById(R.id.txt_label);
        textView.setText(texto);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }

    @Override
    protected void onResume() {
        if (myServicio != null && cam != null) {
            try {
                DBCamareros aux = (DBCamareros) myServicio.getDb("camareros");
                JSONArray ls = aux.filter("ID=" + cam.getString("ID") + " AND autorizado = '1'");
                if (ls.length() == 0) {
                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }
}
