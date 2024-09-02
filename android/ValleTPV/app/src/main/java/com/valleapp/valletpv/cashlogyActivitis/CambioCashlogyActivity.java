package com.valleapp.valletpv.cashlogyActivitis;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.valleapp.valletpv.R;
import com.valleapp.valletpv.tools.CashlogyManager.ChangeAction;
import com.valleapp.valletpv.tools.ServicioCom;

public class CambioCashlogy extends Activity {

     TextView tvTotalAdmitido;
     TextView tvListaMonedas;
     StringBuilder currentInput = new StringBuilder();
     ServicioCom myServicio;

     ChangeAction changeAction;

    // Definición del ServiceConnection para conectar y desconectar el servicio ServicioCom
    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder) iBinder).getService();
            iniciarAccion();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    private void iniciarAccion(){
        // Configurar la acción de cambio de Cashlogy
        changeAction = myServicio.cashLogyChange(new Handler(message -> {
            Bundle data = message.getData();

            if (data != null) {
                String key = data.getString("key", "");
                String value = data.getString("value", "");

                switch (key) {
                    case "CASHLOGY_WR":
                        // Mostrar un Toast con la advertencia
                        Toast.makeText(this, "Advertencia: " + value, Toast.LENGTH_LONG).show();
                        break;

                    case "CASHLOGY_ERR":
                        // Mostrar un Toast con el error y finalizar la actividad
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                        Toast.makeText(this, "Error: " + value, Toast.LENGTH_LONG).show();
                        break;

                    case "CASHLOGY_IMPORTE_ADMITIDO":
                        // Actualizar el TextView para el importe admitido

                        break;

                    case "CASHLOGY_CAMBIO_COMPLETADO":

                        break;

                    default:
                        Log.d("CASHLOGY", "Clave no reconocida: " + key);
                        break;
                }
            }

            return true;
        }));

        // Ejecutar la acción de cambio
        changeAction.execute();  // Enviar el comando #A# para iniciar el cambio
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_cahslogy);

        // Vincular las vistas
        tvTotalAdmitido = findViewById(R.id.tvTotalAdmitido);
        tvListaMonedas = findViewById(R.id.tvListaMonedas);


        // Configurar el teclado numérico
        setupNumericKeypad();



        // Conectar al ServicioCom
        Intent intent = new Intent(this, ServicioCom.class);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desvincular el servicio cuando la actividad se destruya
        if (myServicio != null) {
            unbindService(mConexion);
            myServicio = null;
        }
    }

    private void setupNumericKeypad() {
        int[] buttonIds = {
                R.id.btnNum0, R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4,
                R.id.btnNum5, R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9,
                R.id.btnNumDot, R.id.btnNumClear
        };

        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(this::onNumericButtonClick);
        }
    }

    private void onNumericButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        if (buttonText.equals("C")) {
            currentInput.setLength(0); // Clear input
        } else {
            currentInput.append(buttonText);
        }

        // Actualizar la vista de total admitido con el valor ingresado
        tvTotalAdmitido.setText(currentInput.toString());
    }




}
