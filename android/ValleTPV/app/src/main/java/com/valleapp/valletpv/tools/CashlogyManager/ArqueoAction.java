package com.valleapp.valletpv.tools.CashlogyManager;

import android.os.Handler;
import android.os.Looper;

public class ArqueoAction extends CashlogyAction {

    public ArqueoAction(CashlogySocketManager socketManager) {
        super(socketManager);
    }

    @Override
    public void execute() {
        // Envía el comando para iniciar el arqueo en Cashlogy
        socketManager.sendCommand("#X#");
    }

    @Override
    public void handleResponse(String command, String response) {
        if (command.startsWith("#X#")) {
            if (response.startsWith("#0#")) {
                // Arqueo exitoso, notificar a la UI
                socketManager.notifyUI("CASHLOGY_ARQUEO_COMPLETADO", "Arqueo completado exitosamente.");
            } else {
                // Manejar error de arqueo
                socketManager.notifyUI("CASHLOGY_ERR", "Error durante el arqueo en Cashlogy.");
            }
        }
    }

    public void arquearCashlogy() {
        // Ejecutar la acción de arqueo
        execute();
    }
}
