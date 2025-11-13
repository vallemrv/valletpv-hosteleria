package com.valleapp.valletpvlib.interfaces

import org.json.JSONObject

interface IControllerWS {
    fun sincronizar()
    fun procesarRespose(o: JSONObject)
}