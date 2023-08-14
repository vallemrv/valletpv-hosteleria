package com.valleapp.valletpvlib.interfaces

import org.json.JSONObject

interface IController {
    fun sync_device(devices: Array<String?>?, timeout: Long)
    fun updateTables(o: JSONObject?)
}