package com.valleapp.valletpvlib.interfaces

import org.json.JSONArray

interface IBaseDatos {
    fun filter(cWhere: String?): JSONArray
    fun rellenarTabla(objs: JSONArray)
    fun inicializar()
}
