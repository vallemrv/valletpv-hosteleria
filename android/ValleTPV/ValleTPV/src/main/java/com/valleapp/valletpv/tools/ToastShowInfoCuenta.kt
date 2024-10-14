package com.valleapp.valletpv.tools;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.R;

public class ToastShowInfoCuenta {

    private final int SHORT_TOAST_DURATION = 2000;

    CountDownTimer t;


    public void show(Double entrega, Double cambio, long durationInMillis, Context cx, View l) {

        TextView txtCambio = l.findViewById(R.id.txt_info_cambio);
        txtCambio.setText(String.format("%01.2f €",cambio));
        TextView txtEntrega = l.findViewById(R.id.txt_info_entrega);
        txtEntrega.setText(String.format("%01.2f €",entrega));


        if (t == null){
            t = new CountDownTimer(durationInMillis, SHORT_TOAST_DURATION) {
                @Override
                public void onFinish() {

                }

                @Override
                public void onTick(long millisUntilFinished) {
                    Toast toast = new Toast(cx);
                    toast.setView(l);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
            };
            t.start();

        }
    }

    public void cancel(){
        t.cancel();
    }
}
