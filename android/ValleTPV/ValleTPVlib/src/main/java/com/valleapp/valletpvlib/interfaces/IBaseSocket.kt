package com.valleapp.valletpvlib.interfaces

import org.json.JSONObject

interface IBaseSocket {
    fun rm(o: JSONObject)
    fun insert(o: JSONObject)
    fun update(o: JSONObject)
}
