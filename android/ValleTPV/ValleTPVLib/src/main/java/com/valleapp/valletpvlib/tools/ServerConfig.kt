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
            val obj = JSONObject(json)
            codigo = obj.getString("codigo")
            UID = obj.getString("UID")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    fun getParams(): ContentValues {
        val params = ContentValues()
        if (codigo != null && UID != null) {
            params.put("code", codigo)
            params.put("UID", UID)
        }
        return params
    }

    fun getUrl(endPoint: String): String {
        if (url == null || url!!.isEmpty()) return ""
        var strUrl = ""
        if (!url!!.contains("http://") && !url!!.contains("https://")) strUrl = "http://$url"
        var adjustedEndpoint = if (!endPoint.startsWith("/")) "/$endPoint" else endPoint
        if (strUrl.endsWith("/")) strUrl = strUrl.substring(0, strUrl.length - 1)
        if (!strUrl.endsWith("api")) strUrl += "/api"
        return strUrl + adjustedEndpoint
    }

    fun toJson(): JSONObject? {
        return if (url != null && codigo != null && UID != null)
            JSONObject("{\"codigo\":\"$codigo\",\"UID\":\"$UID\",\"url\":\"$url\"}")
        else null
    }
}
