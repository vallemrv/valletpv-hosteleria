package com.valleapp.valletpv.interfaces

import org.json.JSONArray
import org.json.JSONObject

interface IControladorCuenta {
    fun setEstadoAutoFinish(reset: Boolean, stop: Boolean)
    fun mostrarCobrar(lsart: JSONArray, totalCobro: Double, separetar: Boolean)
    fun cobrar(lsart: JSONArray, totalCobro: Double, entrega: Double, recibo: String)
    fun pedirArt(art: JSONObject)
    fun clickMostrarBorrar(art: JSONObject)
    fun borrarArticulo(art: JSONObject)
    fun cobrarConCashlogy(lsart: JSONArray, totalCobro: Double)
    fun cobrarConTpvPC(lsart: JSONArray, totalCobro: Double)
}
