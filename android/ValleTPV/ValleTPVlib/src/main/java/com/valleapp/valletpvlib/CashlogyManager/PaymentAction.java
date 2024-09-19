package com.valleapp.valletpvlib.CashlogyManager;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.Locale;

public class PaymentAction extends CashlogyAction {

    double amountToCollect;
    double admittedAmount;
    boolean isCancel = false;
    boolean isAceptar = true;
    boolean esBloqueado = false;
    boolean isPCommandSent = false;

    public PaymentAction(CashlogySocketManager socketManager, double amountToCollect) {
        super(socketManager);
        this.amountToCollect = Double.parseDouble(String.format(Locale.getDefault(), "%.2f", amountToCollect));
        this.admittedAmount = 0.0;
    }

    @Override
    public void execute() {
        admittedAmount = 0.0;
        isAceptar = true;
        esBloqueado = false;
        isPCommandSent = false;
        sendBCommand();
    }

    @Override
    public void handleResponse(String command, String response) { }

    private void sendBCommand() {
        socketManager.sendCommand("#B#0#0#0#",
                new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(@NonNull android.os.Message msg) {
                        String response = msg.getData().getString("value");
                        assert response != null;
                        sendQCommand();
                    }
                });
    }


    private void sendQCommand() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                socketManager.sendCommand("#Q#", new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull android.os.Message msg) {
                String response = msg.getData().getString("value");
                assert response != null;
                String[] parts = response.split("#");
                if (parts.length >= 3) {

                    double importeRecibido = Double.parseDouble(parts[2]) / 100;

                    if (importeRecibido != admittedAmount) {
                        admittedAmount = importeRecibido;
                        socketManager.notifyUI("CASHLOGY_IMPORTE_ADMITIDO", String.valueOf(admittedAmount));
                    }
                }
                if (isAceptar) {
                    sendQCommand();
                }else{
                    sendJCommand();
                }
            }
        }), 200);  // Esperar 200ms antes de volver a enviar #Q#
    }

    private void sendJCommand() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                socketManager.sendCommand("#J#", new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull android.os.Message msg) {
                String response = msg.getData().getString("value");
                assert response != null;
                String[] parts = response.split("#");
                if (parts.length >= 3) {

                    double importeRecibido = Double.parseDouble(parts[2]) / 100;

                    if (importeRecibido != admittedAmount) {
                        admittedAmount = importeRecibido;
                        socketManager.notifyUI("CASHLOGY_IMPORTE_ADMITIDO", String.valueOf(admittedAmount));
                    }
                }
                sendPCammand();
            }
        }), 200);  // Esperar 200ms antes de enviar #J#
    }



    private void sendPCammand() {
        if (!isPCommandSent) {
            isPCommandSent = true;
            int importeADevolver ;
            if (!isCancel){
                importeADevolver = (int) Math.round((admittedAmount - amountToCollect) * 100);
            } else {
                importeADevolver = (int) Math.round(admittedAmount * 100);
            }

            if (importeADevolver > 0) {
                final int mostrarPantallaPrimaria = 0;
                final int mostrarPantallaDurantePago = 0;
                final int devolverSoloMonedas = 0;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    String comandoP = "#P#" + importeADevolver + "#" + mostrarPantallaPrimaria + "#" + mostrarPantallaDurantePago + "#" + devolverSoloMonedas + "#";
                    socketManager.sendCommand(comandoP, new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(@NonNull android.os.Message msg) {
                            if (!isCancel) {
                                socketManager.notifyUI("CASHLOGY_COBRO_COMPLETADO", "Cobro completado..");
                            } else {
                                socketManager.notifyUI("CASHLOGY_COBRO_COMPLETADO", "Cobro cancelado.");
                            }
                        }
                    });
                }, 200);
            } else {
                socketManager.notifyUI("CASHLOGY_COBRO_COMPLETADO", "Cobro completado.");
            }
        }
    }


    public boolean  cancelarCobro() {
        if(!esBloqueado) {
            esBloqueado = true;
            isCancel = true;
            isAceptar = false;
            return true;
        }
        return false;
    }

    public void cobrar() {
        if (!esBloqueado) {
            esBloqueado = true;
            isCancel = false;
            isAceptar = false;
        }
    }

    public boolean sePuedeCobrar() {
        double cambio = Double.parseDouble(
                String.format(Locale.getDefault(),
                        "%.2f",admittedAmount - amountToCollect));
        return cambio >= 0;
    }
}
