package com.valleapp.valletpvlib.interfaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by valle on 19/10/14.
 */
public interface IControladorCuenta {
    void setEstadoAutoFinish(boolean reset, boolean stop);
    void mostrarCobrar(final JSONArray lsart, Double totalCobro);
    void cobrar(JSONArray lsart, Double totalCobro, Double entrega );
    void pedirArt(JSONObject art);
    void clickMostrarBorrar(final JSONObject art);
    void borrarArticulo(JSONObject art) throws JSONException;
}
