package com.valleapp.valletpv.tools.CashlogyManager;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class ArqueoAction extends CashlogyAction {

    Map<Integer, Integer> denominacionesDisponibles = new HashMap<>();

    public ArqueoAction(CashlogySocketManager socketManager) {
        super(socketManager);
    }

    @Override
    public void execute() {
        // Envía el comando para obtener todas las denominaciones en Cashlogy
        socketManager.sendCommand("#Y#");
    }


    @Override
    public void handleResponse(String comando, String response) {
        Log.d("CASHLOGY", String.format("Comando enviado:  %s, Respuesta recibida:  %s", comando, response));
        if (comando.startsWith("#Y")) {
            manejarRespuestaDenominaciones(response);
        }
    }
    private void manejarRespuestaDenominaciones(String response) {
        try {
            String[] parts = response.split("#");
            if (parts.length < 3) {
                Log.e("CASHLOGY", "Respuesta de denominaciones no válida.");
                socketManager.notifyUI("CASHLOGY_ERR", "Respuesta de denominaciones no válida.");
                return;
            }

            // Parte b de la respuesta: Monedas/Billetes en recicladores
            String recicladoresParte = parts[2].split(";")[0];
            // Parte c de la respuesta: Billetes en almacenes no devolvibles (stacker + caja fuerte)
            String almacenesParte = parts[2].split(";")[1];

            // Limpiar el mapa antes de rellenarlo
            denominacionesDisponibles.clear();

            // Procesar recicladores
            String[] recicladores = recicladoresParte.split(",");
            for (String reciclador : recicladores) {
                String[] denomination = reciclador.split(":");
                if (denomination.length == 2) {
                    int valorEnCentimos = Integer.parseInt(denomination[0]);
                    int cantidad = Integer.parseInt(denomination[1]);

                    // Almacenar en el mapa de denominaciones disponibles
                    denominacionesDisponibles.put(valorEnCentimos, cantidad);
                }
            }

            // Procesar almacenes no devolvibles (stacker + caja fuerte)
            String[] almacenes = almacenesParte.split(",");
            for (String almacen : almacenes) {
                String[] denomination = almacen.split(":");
                if (denomination.length == 2) {
                    int valorEnCentimos = Integer.parseInt(denomination[0]);
                    int cantidad = Integer.parseInt(denomination[1]);

                    // Almacenar también en el mapa de denominaciones disponibles
                    denominacionesDisponibles.put(valorEnCentimos, cantidad);
                }
            }

            // Notificar a la UI que las denominaciones están listas
            socketManager.notifyUI("CASHLOGY_DENOMINACIONES_LISTAS", "Las denominaciones están listas para su uso.");

        } catch (Exception e) {
            Log.e("CASHLOGY", "Error al manejar la respuesta de denominaciones: " + response + " Error: " + e.getMessage(), e);
            socketManager.notifyUI("CASHLOGY_ERR", "Error al manejar la respuesta de denominaciones.");
        }
    }

    public Map<Integer, Integer> getDenominaciones() {
        return denominacionesDisponibles;
    }

    public  void cerrarCashlogy(){

    }

    public void getTotalCashlogy() {
    }
}
