package com.valleapp.valletpvlib.CashlogyManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PaymentAction extends CashlogyAction {

    double amountToCollect;
    double admittedAmount;
    boolean isCancel = false;
    boolean isAceptar = true;
    boolean isWaitingForFinalQ = false;
    boolean esBloqueado = false;

    public PaymentAction(CashlogySocketManager socketManager, double amountToCollect) {
        super(socketManager);
        this.amountToCollect = amountToCollect;
        this.admittedAmount = 0;
    }

    @Override
    public void execute() {
        admittedAmount = 0;
        isAceptar = true;
        isWaitingForFinalQ = false;
        esBloqueado = false;
        sendBCommand();
    }

    private void sendBCommand() {
        socketManager.sendCommand("#B#0#0#0#");
    }

    @Override
    public void handleResponse(String comando, String response) {
        if (comando.startsWith("#B")) {
            sendQCommand();
        } else if (comando.startsWith("#Q")) {
            String[] parts = response.split("#");
            if (parts.length >= 3) {
                double importeRecibido = Double.parseDouble(parts[2]) / 100;

                if (importeRecibido != admittedAmount) {
                    admittedAmount = importeRecibido;
                    socketManager.notifyUI("CASHLOGY_IMPORTE_ADMITIDO", String.valueOf(admittedAmount));
                }

                if (isAceptar) {
                    sendQCommand();  // Continuar enviando #Q# mientras aceptamos el importe
                } else if (isWaitingForFinalQ) {
                    if(isCancel){
                        sendPCammand(admittedAmount);
                    }else {
                        sendPCammand(admittedAmount - amountToCollect);  // Si ya estamos en la fase final, devolver la diferencia
                    }
                } else {
                    sendJCommand();  // Si no, proceder con #J#
                }
            } else {
                socketManager.notifyUI("CASHLOGY_ERR", "Error en el CASHLOGY, cancelar operación.");
            }
        } else if (comando.startsWith("#J")) {
            isWaitingForFinalQ = true;  // Indica que estamos esperando la última respuesta #Q#
            sendQCommand();      // Después de #J#, enviar un último #Q#
        } else if (comando.startsWith("#P")) {
            Log.e("CASHLOGY", response);
            if (response.contains("#0#") || response.contains("WR:")) {
                if (!isCancel) {
                    socketManager.notifyUI("CASHLOGY_COBRO_COMPLETADO", "Cobro completado sin errores.");
                }
            } else if (response.contains("CASHLOGY_ERR")) {
                isCancel = true;
                socketManager.notifyUI("CASHLOGY_ERR", "Error en Cashlogy: Operación cancelada y cantidad devuelta.");
                sendQCommand();
            }
        }
    }

    private void sendQCommand() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            socketManager.sendCommand("#Q#");
        }, 200);  // Esperar 200ms antes de volver a enviar #Q#
    }

    private void sendJCommand() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            socketManager.sendCommand("#J#");
        }, 300);  // Esperar 300ms antes de enviar #J#
    }

    private void sendPCammand(double cantidadADevolver) {
        int centimosADevolver = (int) Math.round(cantidadADevolver * 100);

        if (centimosADevolver > 0) {
            Log.i("CASHLOGY", "Iniciando dispensación de cambio");
            String comandoDispensar = "#P#" + centimosADevolver + "#0#0#0#";
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                socketManager.sendCommand(comandoDispensar);
            }, 200);  // Enviar comando #P# después de un pequeño retraso
        } else {
            socketManager.notifyUI("CASHLOGY_COBRO_COMPLETADO", "Cobro completado sin errores.");
        }
    }

    public void cancelarCobro() {
        if(!esBloqueado) {
            esBloqueado = true;
            isCancel = true;
            isAceptar = false;
        }
    }

    public void cobrar() {
        if (!esBloqueado) {
            esBloqueado = true;
            isCancel = false;
            isAceptar = false;
        }
    }

    public boolean sePuedeCobrar() {
        double cambio = admittedAmount - amountToCollect;
        return cambio >= 0;
    }
}
