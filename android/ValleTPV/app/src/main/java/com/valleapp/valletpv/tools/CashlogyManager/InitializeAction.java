package com.valleapp.valletpv.tools.CashlogyManager;


public class InitializeAction extends CashlogyAction {

    public InitializeAction(CashlogySocketManager socketManager) {
        super(socketManager);
    }

    @Override
    public void execute() {
        // Enviar el comando #I# para inicializar la máquina
        socketManager.setCurrentAction(this);
        socketManager.sendCommand("#I#");
    }

    @Override
    public void handleResponse(String comando, String response) {

    }
}
