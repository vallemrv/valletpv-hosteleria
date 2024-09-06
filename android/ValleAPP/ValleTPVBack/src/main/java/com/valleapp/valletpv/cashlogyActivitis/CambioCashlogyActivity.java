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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.valleapp.valletpv.R;
import com.valleapp.valletpvlib.tools.CashlogyManager.ChangeAction;
import com.valleapp.valletpvlib.tools.ServicioCom;


import java.util.HashMap;
import java.util.Map;

public class CambioCashlogyActivity extends Activity {

     TextView tvTotalAdmitido;
     ServicioCom myServicio;

     ChangeAction changeAction;

    private boolean mensajeMostrado = false;

    // Mapa para asociar denominaciones con sus respectivos botones
    private Map<Integer, ImageButton> botonDenominacionesMap = new HashMap<>();



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
                        Toast.makeText(this, "Error: " + value, Toast.LENGTH_LONG).show();
                        if (!value.startsWith("Error de ocupación")) {
                            finish();
                        }
                        break;

                    case "CASHLOGY_IMPORTE_ADMITIDO":
                        // Actualizar el TextView para el importe admitido
                        double importeAdmitido = Double.parseDouble(value);
                        tvTotalAdmitido.setText(String.format("%01.2f €", (importeAdmitido)));
                        break;

                    case "CASHLOGY_CAMBIO":
                        Toast.makeText(this,  value, Toast.LENGTH_LONG).show();
                        finish();
                        break;

                    case "CASHLOGY_DENOMINACIONES_DISPONIBLES":
                        Map<Integer, Integer> denominaciones = parseDenominaciones(value);
                        actualizarBotonesConDenominaciones(denominaciones);

                        // Mostrar el mensaje solo una vez
                        if (!mensajeMostrado) {
                            TextView tvMensajeUsuario = findViewById(R.id.tvMensajeUsuario);
                            tvMensajeUsuario.setText("Ahora elija la fracción más pequeña que desea recibir en el cambio.");
                            mensajeMostrado = true;
                        }
                        break;

                    default:
                        Log.d("CASHLOGY", "Clave no reconocida: " + key);
                        break;
                }
            }

            return true;
        }));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_cahslogy);

        // Vincular las vistas
        tvTotalAdmitido = findViewById(R.id.tvTotalAdmitido);
        ImageButton btnSalir = findViewById(R.id.btnSalir);

        // Vincular los botones a las variables y agregarlos al mapa
        botonDenominacionesMap.put(2000, findViewById(R.id.btnVeinteEuros));    // 20 euros -> 2000 céntimos
        botonDenominacionesMap.put(1000, findViewById(R.id.btnDiezEuros));      // 10 euros -> 1000 céntimos
        botonDenominacionesMap.put(500, findViewById(R.id.btnCincoEuros));      // 5 euros -> 500 céntimos
        botonDenominacionesMap.put(200, findViewById(R.id.btnDosEuros));        // 2 euros -> 200 céntimos
        botonDenominacionesMap.put(100, findViewById(R.id.btnUnEuro));          // 1 euro -> 100 céntimos
        botonDenominacionesMap.put(50, findViewById(R.id.btnCincuentaCents));   // 0.50 euros -> 50 céntimos
        botonDenominacionesMap.put(20, findViewById(R.id.btnVeinteCents));      // 0.20 euros -> 20 céntimos
        botonDenominacionesMap.put(10, findViewById(R.id.btnDiezCents));        // 0.10 euros -> 10 céntimos
        botonDenominacionesMap.put(5, findViewById(R.id.btnCincoCents));        // 0.05 euros -> 5 céntimos
        botonDenominacionesMap.put(2, findViewById(R.id.btnDosCents));          // 0.02 euros -> 2 céntimos
        botonDenominacionesMap.put(1, findViewById(R.id.btnUnCentimo));         // 0.01 euros -> 1 céntimo

        // Configurar un único listener para todos los botones
        for (Map.Entry<Integer, ImageButton> entry : botonDenominacionesMap.entrySet()) {
            ImageButton boton = entry.getValue();
            boton.setOnClickListener(v -> {
                Integer denominacionEnCentimos = entry.getKey();
                changeAction.cambiar(denominacionEnCentimos);
            });
        }

        btnSalir.setOnClickListener(v -> {
            // Aquí va el código para manejar la acción de cancelar
            cancelarCambio();
        });



        // Conectar al ServicioCom
        Intent intent = new Intent(this, ServicioCom.class);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
    }


    private Map<Integer, Integer> parseDenominaciones(String value) {
        Map<Integer, Integer> denominacionesMap = new HashMap<>();
        String[] denominacionesArray = value.split(",");

        for (String denominacion : denominacionesArray) {
            String[] parts = denominacion.split(":");
            int denominacionEnCentimos = Integer.parseInt(parts[0]);
            int cantidad = Integer.parseInt(parts[1]);
            denominacionesMap.put(denominacionEnCentimos, cantidad);
        }

        return denominacionesMap;
    }


    private void actualizarBotonesConDenominaciones(Map<Integer, Integer> denominacionesDisponibles) {
        for (Map.Entry<Integer, ImageButton> entry : botonDenominacionesMap.entrySet()) {
            Integer valorEnCentimos = entry.getKey();

            // Verificar si la denominación está disponible en el mapa
            if (denominacionesDisponibles.containsKey(valorEnCentimos) && denominacionesDisponibles.get(valorEnCentimos) > 0) {
                entry.getValue().setVisibility(View.VISIBLE);
            } else {
                entry.getValue().setVisibility(View.GONE);
            }
        }
    }


    private void cancelarCambio() {
        if (changeAction != null ) {
            changeAction.cancelar();
        }
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



}
