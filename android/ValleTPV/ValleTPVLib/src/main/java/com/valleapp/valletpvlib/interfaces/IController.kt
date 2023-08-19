package com.valleapp.valletpvlib.interfaces

import org.json.JSONObject

interface IController {
    fun syncDevice(lista: List<String>)
    fun updateTables(o: JSONObject?)
}