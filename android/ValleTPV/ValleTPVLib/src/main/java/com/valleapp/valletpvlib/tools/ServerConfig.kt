package com.valleapp.valletpvlib.tools

import android.content.ContentValues
import org.json.JSONObject

data class ServerConfig(
    var codigo: String? = null,
    var UID: String? = null,
    var url: String? = null
) {

    fun loadJSON(json: String?): Boolean {
        return try {
            val obj = json?.let { JSONObject(it) }
            if (obj != null) {
                codigo = obj.getString("codigo")
            }
            if (obj != null) {
                UID = obj.getString("UID")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isEmpty(): Boolean {
        return codigo.isNullOrEmpty() || UID.isNullOrEmpty() || url.isNullOrEmpty()
    }

    fun getParams(args: Map<String, Any>? = null): ContentValues {
        val aux = ContentValues()
        if (args != null && !isEmpty()) {
            aux.put("codigo", codigo)
            aux.put("UID", UID)
            for((k, v) in args){
                aux.put(k, v.toString())
            }
        }
        return aux
    }

    fun toJson(): JSONObject? {
        return if (url != null && codigo != null && UID != null)
            JSONObject("{\"codigo\":\"$codigo\",\"UID\":\"$UID\",\"url\":\"$url\"}")
        else null
    }

    fun isEqualsCode(c: String): Boolean {
        return codigo == c
    }

    fun getFullUrl(endpoint: String): String {
           return parseUrl(url!!) + endpoint
    }

    companion object{
        fun parseUrl(url: String): String {
            var aux = url
            if (!aux.startsWith("https://") && !aux.startsWith("http://")) {
                aux = "http://$aux"
            }
            if (!aux.endsWith("/")) {
                aux += "/"
            }
            if (!aux.endsWith("/api/")) {
                aux += "api/"
            }
            return aux
        }
    }
}
