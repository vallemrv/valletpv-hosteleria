package com.valleapp.valletpvlib.interfaces

import org.json.JSONObject

interface IBaseSocket {
    suspend fun rm(o: JSONObject)
    suspend fun insert(o: JSONObject)
    suspend fun update(o: JSONObject)
}
