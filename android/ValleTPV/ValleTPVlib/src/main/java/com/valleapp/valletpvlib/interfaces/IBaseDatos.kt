package com.valleapp.valletpvlib.interfaces

import org.json.JSONArray

interface IBaseDatos {
    suspend fun filter(cWhere: String?): List<Any> // Sustituye Any por una entidad específica
    suspend fun rellenarTabla(objs: JSONArray)
    suspend fun inicializar()
}
