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
import com.valleapp.valletpv.tools.CashlogyManager.ArqueoAction;
import com.valleapp.valletpv.tools.CashlogyManager.CashlogySocketManager;
import com.valleapp.valletpv.tools.HTTPRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class ArqueoCashlogyActivity extends Activity {

    private TextView tvInformacionSalida;
    private ImageButton btnSalir;
    private ImageButton btnArquearCaja;

    private CashlogySocketManager cashlogySocketManager;
    private ArqueoAction arqueoAction;

    private double cambio;
    private String server;

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
        boolean hayArqueo = intent.getBooleanExtra("hayArqueo", false);

        // Verificar si se puede hacer el arqueo
        if (hayArqueo) {
            inicializarCashlogyManager();
        } else {
            mostrarMensaje("No se puede realizar el arqueo porque no hay ticket de cierre.");
            btnArquearCaja.setVisibility(View.GONE);
        }

        btnSalir.setOnClickListener(v -> finish());

        btnArquearCaja.setOnClickListener(v -> realizarArqueo());
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
        arqueoAction.execute();
    }

    private void realizarArqueo() {
        // Paso 1: Obtener las denominaciones desde ArqueoAction
        JSONArray objEfectivo = new JSONArray();
        double totalEfectivo = 0.0;

        Map<Integer, Integer> denominaciones = arqueoAction.getDenominaciones();

        // Paso 2: Convertir las denominaciones a objetos JSON y calcular el total de efectivo
        for (Map.Entry<Integer, Integer> entry : denominaciones.entrySet()) {
            int cantidad = entry.getValue();
            double moneda = entry.getKey() / 100.0;

            JSONObject obj = new JSONObject();
            try {
                obj.put("Can", cantidad);
                obj.put("Moneda", moneda);
                objEfectivo.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            totalEfectivo += cantidad * moneda;
        }

        // Paso 3: Crear ContentValues para enviar los datos al servidor
        ContentValues p = new ContentValues();
        p.put("cambio", Double.toString(cambio));
        p.put("efectivo", Double.toString(totalEfectivo));
        p.put("gastos", Double.toString(0.0));
        p.put("des_efectivo", objEfectivo.toString());
        p.put("es_cashlogy", true);
        p.put("des_gastos", "[]"); // Asume que objGastos está vacío o usa objGastos si tiene datos

        // Enviar la solicitud al servidor para cerrar el arqueo
        new HTTPRequest(server + "/arqueos/arquear", p, "arqueo", new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String response = msg.getData().getString("RESPONSE");
                if ("success".equals(response)) {
                    // Paso 4: Ejecutar el comando para cerrar Cashlogy
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
                    Log.d("CASHLOGY", String.format("Último cambio registrado: %f", cambio));
                    break;
                case "CASHLOGY_ARQUEADA":
                    arqueoAction.getTotalCashlogy();
                    break;
                case "CASHLOGY_CASH":
                    actualizarEfectivoEnServidor(value);
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

    private void actualizarEfectivoEnServidor(String totalCashlogy) {
        ContentValues updateCashValues = new ContentValues();
        updateCashValues.put("cambio", totalCashlogy);
        new HTTPRequest(server + "/arqueos/setcambio", updateCashValues, "updateCash", new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String updateResponse = msg.getData().getString("RESPONSE");
                if ("success".equals(updateResponse)) {
                    mostrarMensaje("Cierre completado correctamente.");
                } else {
                    mostrarMensaje("Error al actualizar el efectivo en el servidor.");
                }
            }
        });
    }
}
