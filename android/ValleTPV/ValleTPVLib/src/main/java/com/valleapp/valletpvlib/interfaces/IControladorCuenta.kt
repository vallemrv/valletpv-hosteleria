package com.valleapp.valletpvlib.interfaces

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by valle on 19/10/14.
 */
interface IControladorCuenta {
    fun setEstadoAutoFinish(reset: Boolean, stop: Boolean)
    fun mostrarCobrar(lsart: JSONArray?, totalCobro: Double?)
    fun cobrar(lsart: JSONArray?, totalCobro: Double?, entrega: Double?)
    fun pedirArt(art: JSONObject?)
    fun clickMostrarBorrar(art: JSONObject?)

    @Throws(JSONException::class)
    fun borrarArticulo(art: JSONObject?)
}