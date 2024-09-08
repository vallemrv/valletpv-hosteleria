package com.valleapp.valletpv.cashlogyActivitis;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.valleapp.valletpv.R;
import com.valleapp.valletpvlib.CashlogyManager.ArqueoAction;
import com.valleapp.valletpvlib.CashlogyManager.CashlogySocketManager;
import com.valleapp.valletpvlib.comunicacion.HTTPRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

public class ArqueoCashlogyActivity extends Activity {

     TextView tvInformacionSalida;
     ImageButton btnSalir;
     ImageButton btnArquearCaja;

     CashlogySocketManager cashlogySocketManager;
     ArqueoAction arqueoAction;

     double cambio;
     String server;
     double stacke;
     double cambio_real;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arqueo_cashlogy);

        // Vincular los elementos de la UI
        tvInformacionSalida = findViewById(R.id.tvInformacionSalida);
        btnSalir = findViewById(R.id.btnSalir);
        btnArquearCaja = findViewById(R.id.arquearCaja);

        // Ocultar el botón de cerrar caja al inicio
        btnArquearCaja.setVisibility(View.GONE);

        // Obtener los datos del Intent
        Intent intent = getIntent();
        server = intent.getStringExtra("URL");
        cambio = intent.getDoubleExtra("cambio", 0.0);
        stacke = intent.getDoubleExtra("stacke", 0.0);
        cambio_real = intent.getDoubleExtra("cambio_real", 0.0);
        boolean hayArqueo = intent.getBooleanExtra("hayArqueo", false);
        hayArqueo = true;

        // Verificar si se puede hacer el arqueo
        if (hayArqueo) {
            inicializarCashlogyManager();
        } else {
            mostrarMensaje("No se puede realizar el arqueo porque no hay ticket de cierre.");
            btnArquearCaja.setVisibility(View.GONE);
        }

        btnSalir.setOnClickListener(v -> finish());

        btnArquearCaja.setOnClickListener(v -> {
            realizarArqueo();
            v.setVisibility(View.GONE);
        });
    }

    private void inicializarCashlogyManager() {
        // Iniciar el CashlogySocketManager con los datos obtenidos
        String urlCashlogy = getIntent().getStringExtra("URL_Cashlogy");
        cashlogySocketManager = new CashlogySocketManager(urlCashlogy);
        cashlogySocketManager.start();

        // Crear la acción de arqueo
        arqueoAction = new ArqueoAction(cashlogySocketManager);

        // Configurar el Handler para recibir las respuestas del Cashlogy
        Handler uiHandler = new Handler(Looper.getMainLooper(), this::manejarMensajeCashlogy);

        cashlogySocketManager.setUiHandler(uiHandler);
        cashlogySocketManager.setCurrentAction(arqueoAction);
        arqueoAction.setCambioStacker(cambio);
        arqueoAction.execute();
    }

    private void realizarArqueo() {
        // Paso 1: Obtener las denominaciones desde ArqueoAction
        JSONArray objEfectivo = new JSONArray();

        Map<Integer, Integer> denominaciones = arqueoAction.getDenominaciones();
        double total_efectivo_anterior = stacke + cambio_real;
        double total_efectivo_ahora = arqueoAction.getTotalRecicladores() + arqueoAction.getTotalAlmacenes();
        double totalCaja = total_efectivo_ahora - total_efectivo_anterior;

        // Paso 2: Convertir las denominaciones a objetos JSON y calcular el total de efectivo
        try {
            for (Map.Entry<Integer, Integer> entry : denominaciones.entrySet()) {
                int cantidad = entry.getValue();
                double moneda = entry.getKey() / 100.0;

                JSONObject obj = new JSONObject();
                obj.put("Can", cantidad);
                obj.put("Moneda", String.format(Locale.getDefault(), "%.2f", moneda)); // Formatear moneda a dos decimales
                objEfectivo.put(obj);

            }
            JSONObject obj = new JSONObject();
            obj.put("Can", 1);
            obj.put("Moneda", String.format(Locale.getDefault(),
                    "%.2f", (totalCaja+cambio) - arqueoAction.getTotalRecicladores())); // Formatear moneda a dos decimales
            objEfectivo.put(obj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Paso 3: Crear ContentValues para enviar los datos al servidor
        ContentValues p = new ContentValues();
        p.put("cambio", String.format(Locale.getDefault(), "%.2f", cambio));  // Formatear cambio a dos decimales
        p.put("efectivo", String.format(Locale.getDefault(),"%.2f", totalCaja+cambio));  // Formatear totalEfectivo a dos decimales
        p.put("gastos", String.format(Locale.getDefault(), "%.2f", 0.0));  // Formatear gastos a dos decimales
        p.put("des_efectivo", objEfectivo.toString());
        p.put("usaCashlogy", "true");
        p.put("des_gastos", "[]");

        Log.d("ArqueoCashlogyActivity", p.toString());
        // Enviar la solicitud al servidor para cerrar el arqueo
        new HTTPRequest(server + "/arqueos/arquear", p, "arqueo", new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String response = msg.getData().getString("RESPONSE");
                if ("success".equals(response)) {
                    arqueoAction.cerrarCashlogy();
                } else {
                    mostrarMensaje("Error al realizar el cierre de caja en el servidor.");
                }
            }
        });
    }

    private boolean manejarMensajeCashlogy(Message message) {
        Bundle data = message.getData();
        String key = data.getString("key");
        String value = data.getString("value");

        if (key != null) {
            switch (key) {
                case "CASHLOGY_DENOMINACIONES_LISTAS":
                    mostrarMensaje("La contabilidad está lista para cerrar caja. Puede pulsar el botón de cierre de caja.");
                    btnArquearCaja.setVisibility(View.VISIBLE);
                    break;
                case "CASHLOGY_CIERRE_COMPLETADO":
                    mostrarMensaje("Cierre completado ya puedes pulsar salir. Gracias por su colaboracion.");
                    break;
                case "CASHLOGY_CASH":
                     actualizarEfectivoEnServidor();
                    break;
                case "CASHLOGY_ERR":
                    mostrarMensaje(value);
                    break;
            }
        }
        return true;
    }

    private void mostrarMensaje(String mensaje) {
        tvInformacionSalida.setText(mensaje);
    }

    private void actualizarEfectivoEnServidor() {
        ContentValues p = new ContentValues();
        p.put("cambio", String.valueOf(cambio));
        p.put("stacke", String.valueOf(arqueoAction.getTotalAlmacenes()));
        p.put("cambio_real", String.valueOf(arqueoAction.getTotalRecicladores()));

        new HTTPRequest(server + "/arqueos/setcambio", p, "updateCash", new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String updateResponse = msg.getData().getString("RESPONSE");
                if ("success".equals(updateResponse)) {
                    arqueoAction.cashLogyCerrado();
                } else {
                    mostrarMensaje("Error al actualizar el efectivo en el servidor.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(cashlogySocketManager != null) {
            cashlogySocketManager.stop();
        }
        super.onDestroy();
    }
}


