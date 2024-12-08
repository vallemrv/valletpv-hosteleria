package com.valleapp.valletpvlib.Interfaces;

import org.json.JSONObject

interface IControllerWS {
    fun sincronizar()
    fun procesarRespose(o: JSONObject)
}