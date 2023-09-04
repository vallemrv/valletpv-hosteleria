package com.valleapp.valletpvlib.tools

import org.json.JSONObject

data class ServerConfig(
    var codigo: String? = null,
    var uid: String? = null,
    var url: String? = null
) {

    fun getParseUrl(): String {
        var aux = parseUrl(url ?: "")
        if (!aux.endsWith("/")) {
            aux += "/"
        }
        if (!aux.endsWith("/api/")) {
            aux += "api/"
        }
        return aux
    }

    fun getUrlBase(): String {
        return parseUrl(url ?: "")
    }

    fun loadJSON(obj: Map<String, Any>) {
        try {
            codigo = obj["codigo"].toString()
            uid = obj["UID"].toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isEmpty(): Boolean {
        return codigo.isNullOrEmpty() || uid.isNullOrEmpty() || url.isNullOrEmpty()
    }

    fun getParams(args: Map<String, Any>? = null): Map<String, String> {
        val aux = HashMap<String, String>()
        if (args != null && !isEmpty()) {
            codigo?.let { aux.put("codigo", it) }
            uid?.let { aux.put("UID", it) }
            for ((k, v) in args) {
                aux[k] = v.toString()
            }
        }
        return aux
    }

    fun toJson(): JSONObject? {
        return if (url != null && codigo != null && uid != null)
            JSONObject("{\"codigo\":\"$codigo\",\"UID\":\"$uid\",\"url\":\"$url\"}")
        else null
    }

    fun getWSUrl(): String {
        var newUrl = url?: ""
        newUrl = if (newUrl.contains("/api")) newUrl.substring(0, newUrl.indexOf("/api"))
        else "$newUrl/ws/"
        newUrl = when {
            newUrl.startsWith("http://") -> newUrl.replace("http://", "ws://")
            newUrl.startsWith("https://") -> newUrl.replace("https://", "wss://")
            else -> "ws://$newUrl"
        }
        return newUrl
    }


    companion object {
        fun parseUrl(url: String): String {
            var aux = url
            if (!aux.startsWith("https://") && !aux.startsWith("http://")) {
                aux = "http://$aux"
            }
            return aux
        }
    }
}
