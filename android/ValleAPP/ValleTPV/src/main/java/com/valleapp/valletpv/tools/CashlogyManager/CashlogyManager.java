package com.valleapp.valletpv.tools.CashlogyManager;

import android.os.Handler;


public class CashlogyManager {
    private CashlogySocketManager socketManager;

    public CashlogyManager(CashlogySocketManager socketManager) {
        this.socketManager = socketManager;
    }

    // Método para inicializar la máquina Cashlogy
    public void initialize() {
        CashlogyAction action = new InitializeAction(socketManager);
        action.execute();
    }

    // Método para realizar un pago
    public PaymentAction makePayment(double amount, Handler uiHandler) {
        PaymentAction action = new PaymentAction(socketManager, amount);
        socketManager.setUiHandler(uiHandler);
        socketManager.setCurrentAction(action);
        action.execute();
        return action;
    }

    public ChangeAction makeChange(Handler uiHandler) {
        ChangeAction action = new ChangeAction(socketManager);
        socketManager.setUiHandler(uiHandler);
        socketManager.setCurrentAction(action);
        action.execute();
        return action;
    }
}
