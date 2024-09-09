package com.valleapp.valletpvlib.CashlogyManager;


import android.util.Log;

import androidx.annotation.NonNull;

import com.valleapp.valletpvlib.Interfaces.IControllerWS;
import com.valleapp.valletpvlib.comunicacion.WSClinet;

import org.json.JSONException;
import org.json.JSONObject;

public class WSAction extends CashlogyAction implements IControllerWS {

    WSClinet ws;

    public WSAction(CashlogySocketManager socketManager, WSClinet ws) {
        super(socketManager);
        this.ws = ws;
    }

    @Override
    public void execute() {

    }

    @Override
    public void handleResponse(String comando, String response) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("instruccion", comando);
            obj.put("respuesta", response);
            ws.sendMessage(obj.toString());
        }catch (JSONException e){
            Log.e("WSACTION_ERROR", e.toString());
        }
    }

    @Override
    public void sincronizar() {

    }

    @Override
    public void procesarRespose(@NonNull JSONObject o) {

    }
}
