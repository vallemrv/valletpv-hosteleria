package com.valleapp.valletpvlib.CashlogyManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.valleapp.valletpvlib.Interfaces.IControllerWS;
import com.valleapp.valletpvlib.comunicacion.WSClinet;

import org.json.JSONException;
import org.json.JSONObject;


public class CashlogyManager implements IControllerWS {
    private final CashlogySocketManager socketManager;
    WSClinet ws;

    public void openWS(String server){
        if (ws == null) ws = new WSClinet(server, "/comunicacion/cashlogy", this);
        ws.connect();
    }

    public void closeWS(){
        if (ws != null){
            ws.stopReconnection();
        }
    }

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


    @Override
    public void sincronizar() {

    }

    @Override
    public void procesarRespose(@NonNull JSONObject o) {
        try {
            if (o.has("instruccion") && o.getString("instruccion").startsWith("#")) {
                socketManager.sendCommand(o.getString("instruccion"), new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(@NonNull android.os.Message msg) {
                        super.handleMessage(msg);
                        String key = msg.getData().getString("key");
                        String value = msg.getData().getString("value");
                        assert key != null;
                        if (key.equals("CASHLOGY_RESPONSE")) {
                            try {
                                assert value != null;
                                ws.sendMessage(String.valueOf(new JSONObject(value)));
                            }catch (Exception e){
                                Log.e("CASHLOGY", "Error al procesar respuesta: " + e.getMessage());
                            }
                        }
                    }
                });
            }
        }catch (JSONException e){
            Log.e("CASHLOGY", "Error al procesar respuesta: " + e.getMessage());
        }
    }
}
