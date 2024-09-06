package com.valleapp.valletpvlib.CashlogyManager;

public abstract class CashlogyAction {
    protected CashlogySocketManager socketManager;

    public CashlogyAction(CashlogySocketManager socketManager) {
        this.socketManager = socketManager;
    }

    // Método para ejecutar la acción. Debe ser implementado por las clases que extienden CashlogyAction.
    public abstract void execute();

    // Método para manejar la respuesta recibida. Debe ser implementado por las clases que extienden CashlogyAction.
    public abstract void handleResponse(String command, String response);
}
