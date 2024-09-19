package com.valleapp.valletpvlib.CashlogyManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChangeAction extends CashlogyAction {

    private double importeAdmitido = 0.0;

    boolean isCancel = false;
    boolean isAceptar = true;
    boolean isSending = false;

    int denominacionMinima = 0;

    boolean esBloqueado = false;
     Map<Integer, Integer> denominacionesDisponibles = new HashMap<>();

    public ChangeAction(CashlogySocketManager socketManager) {
        super(socketManager);
    }

    @Override
    public void execute() {
        importeAdmitido = 0;
        denominacionMinima = 0;
        isAceptar = true;
        isCancel = false;
        esBloqueado = false;
        isSending = false;
        socketManager.sendCommand("#Y#");
    }

    @Override
    public void handleResponse(String comando, String response) {
        if (comando.startsWith("#Y")) {
            manejarRespuestaDenominaciones(response);
        } else if (comando.startsWith("#B")) {
            sendQCommand();
        } else if (comando.startsWith("#Q#")) {
            manejarRespuestaQ(response);
        } else if (comando.startsWith("#J#")) {
            manejarRespuestaJ(response);
        }else if (comando.startsWith("#U")){
            manejarDispensacionCambio();
        } else if (comando.startsWith("#P")) {
            manejarRespuestaP();
        }
    }



    private void sendQCommand() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                socketManager.sendCommand("#Q#"), 200);  // Esperar 200ms antes de volver a enviar #Q#
    }

    private void sendJCommand() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                socketManager.sendCommand("#J#"), 200);  // Esperar 300ms antes de enviar #J#
    }

    private void sendPCommand(Double importe) {
        int centimosADevolver = (int) Math.round(importe * 100);
        String comandoDispensar = "#P#" + centimosADevolver + "#0#0#0#";
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                socketManager.sendCommand(comandoDispensar), 200);  // Enviar comando #P# después de un pequeño retraso
    }

    private void manejarRespuestaDenominaciones(String response) {
        try {
            String[] parts = response.split("#");
            if (parts.length < 3) {
                Log.e("CASHLOGY", "Respuesta de denominaciones no válida.");
                socketManager.notifyUI("CASHLOGY_ERR", "Respuesta de denominaciones no válida.");
                return;
            }

            // Parte b de la respuesta, que contiene los recicladores (antes del primer punto y coma)
            String recicladoresParte = parts[2].split(";")[0]; // Primer parte son los recicladores (monedas y billetes)
            String billetesParte = parts[2].split(";")[1]; // Segunda parte para billetes

            // Limpiar el mapa antes de rellenarlo
            denominacionesDisponibles.clear();

            // Separar las denominaciones y cantidades para monedas (primera parte)
            String[] recicladores = recicladoresParte.split(",");
            for (String reciclador : recicladores) {
                String[] denomination = reciclador.split(":");
                if (denomination.length == 2) {
                    int valorEnCentimos = Integer.parseInt(denomination[0]);
                    int cantidad = Integer.parseInt(denomination[1]);

                    // Convertir de céntimos a euros
                    denominacionesDisponibles.put(valorEnCentimos, cantidad);
                }
            }

            // Separar las denominaciones y cantidades para billetes (segunda parte)
            String[] billetes = billetesParte.split(",");
            for (String billete : billetes) {
                String[] denomination = billete.split(":");
                if (denomination.length == 2) {
                    int valorEnCentimos = Integer.parseInt(denomination[0]);
                    int cantidad = Integer.parseInt(denomination[1]);
                    denominacionesDisponibles.put(valorEnCentimos, cantidad);
                }
            }

            socketManager.sendCommand("#B#0#0#0#");

        } catch (Exception e) {
            Log.e("CASHLOGY", "Error al manejar la respuesta de denominaciones: " + response + " Error: " + e.getMessage(), e);
            socketManager.notifyUI("CASHLOGY_ERR", "Error al manejar la respuesta de denominaciones.");
        }
    }

    private void manejarRespuestaQ(String response) {
        manejarRespuestaImporteAdmitido(response);
        // Si la admisión aún no ha finalizado, continuar consultando el importe
        if (isAceptar) {
            sendQCommand();
        }  else {
            sendJCommand();
        }
    }

    private void manejarRespuestaJ(String response) {
        manejarRespuestaImporteAdmitido(response);
        calcularYDispensarCambio();
    }


    private void manejarRespuestaImporteAdmitido(String response) {
        String[] parts = response.split("#");
        if (parts.length >= 3) {
            double nuevoImporteAdmitido = Double.parseDouble(parts[2]) / 100;

            // Solo notificar a la UI si el importe recibido es diferente al guardado
            if (nuevoImporteAdmitido != importeAdmitido) {
                importeAdmitido =Double.parseDouble( String.format(Locale.getDefault(),"%.2f", nuevoImporteAdmitido));
                socketManager.notifyUI("CASHLOGY_IMPORTE_ADMITIDO", String.valueOf(importeAdmitido));


                // Convertir la nueva cantidad a céntimos
                int importeRestante = (int) Math.round(importeAdmitido * 100);

                // Filtrar denominaciones menores o iguales a la cantidad introducida y que no tengan valor 0
                Map<Integer, Integer> denominacionesFiltradas = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : denominacionesDisponibles.entrySet()) {
                    int denominacion = entry.getKey();
                    int cantidad = entry.getValue();

                    if (denominacion <= importeRestante && cantidad > 0) {
                        denominacionesFiltradas.put(denominacion, cantidad);
                    }
                }

                // Enviar las denominaciones disponibles a la UI
                notificarDenominacionesDisponiblesUI(denominacionesFiltradas);
            }

        }
    }

    private void notificarDenominacionesDisponiblesUI(Map<Integer, Integer> denominacionesDisponibles) {
        StringBuilder cantidadesDisponibles = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : denominacionesDisponibles.entrySet()) {
            int denominacion = entry.getKey();
            int cantidad = entry.getValue();
            cantidadesDisponibles.append(denominacion).append(":").append(cantidad).append(",");
        }

        // Eliminar la última coma si existe
        if (cantidadesDisponibles.length() > 0) {
            cantidadesDisponibles.setLength(cantidadesDisponibles.length() - 1);
        }

        // Enviar la información de las denominaciones a la UI
        socketManager.notifyUI("CASHLOGY_DENOMINACIONES_DISPONIBLES", cantidadesDisponibles.toString());
    }



    private void calcularYDispensarCambio() {
        if (isSending) {
            return;
        }

        isSending = true;
        if (isCancel) {
            if (importeAdmitido > 0) {
                sendPCommand(importeAdmitido);
            }else{
                socketManager.notifyUI("CASHLOGY_CAMBIO", "Operación cancelada.");
            }
        } else {
            sendUCommand();
        }

    }

    private void manejarRespuestaP() {
        if(!isCancel){
            socketManager.notifyUI("CASHLOGY_CAMBIO", "Operación cancelada.");
        }else {
            socketManager.notifyUI("CASHLOGY_CAMBIO", "Operación finalizada.");
        }
    }

    private void manejarDispensacionCambio() {
        double importeRestante = Double.parseDouble(String.format(Locale.getDefault(),
                "%.2f", importeAdmitido - (denominacionMinima / 100.0)));
        sendPCommand(importeRestante);
    }

    private void sendUCommand() {
        StringBuilder comandoBuilder = new StringBuilder("#U#");

        if (denominacionMinima < 500) {
            // Si la denominación mínima es menor a 500 céntimos (monedas)
            comandoBuilder.append(denominacionMinima).append(":1;");
        } else {
            // Si la denominación mínima es 500 céntimos o mayor (billetes)
            comandoBuilder.append(";").append(denominacionMinima).append(":1");
        }

        // Completar el comando con los parámetros adicionales
        comandoBuilder.append("#0#0#0#");

        // Enviar el comando construido
        socketManager.sendCommand(comandoBuilder.toString());
    }




    public void cancelar(){
        if(!esBloqueado) {
            esBloqueado = true;
            isCancel = true;
            isAceptar = false;
        }
    }

    public void cambiar(int denominacionMinima){
        if (importeAdmitido > 0) {
            if(!esBloqueado) {
                esBloqueado = true;
                isCancel = false;
                isAceptar = false;
                this.denominacionMinima = denominacionMinima;
            }
        }
    }
}
