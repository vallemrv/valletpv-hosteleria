package com.valleapp.valletpv.models


import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.valleapp.valletpvlib.tools.HTTPRequest
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig

class PreferenciasModel(app: Application) : AndroidViewModel(app) {

    var url by mutableStateOf("")
    var codigo by mutableStateOf("")
    var UID by mutableStateOf("")
    var isCardVisible by mutableStateOf(false)
    var serverConfig: ServerConfig by mutableStateOf(ServerConfig())
    var preferenciasCargadas by mutableStateOf(false)
    var error: Boolean by mutableStateOf(false)
    var app: Application by mutableStateOf(app)
    var strError: String by mutableStateOf("")

    init {
        if (!preferenciasCargadas) {
            JSON.deserializar("preferencias.dat", app.applicationContext)?.let {
                if (it.has("url")) {
                    url = it.getString("url")
                    serverConfig.url = url
                }
                if (it.has("codigo")) {
                    codigo = it.getString("codigo")
                    serverConfig.codigo = codigo
                }
                if (it.has("UID")) {
                    UID = it.getString("UID")
                    serverConfig.UID = UID
                }
                preferenciasCargadas = url.isNotEmpty() && codigo.isNotEmpty() && UID.isNotEmpty()
                isCardVisible = preferenciasCargadas
                error = !preferenciasCargadas
                if (error) {
                    strError = "Error al cargar preferencias"
                }
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper()) { message ->
        val response: String? = message.data.getString("RESPONSE")
        when (message.data.getString("op")) {
            "ERROR" -> {
                isCardVisible = false
                error = true
                strError = response ?: "Error desconocido"
            }

            "GETCONFIG" -> {
                serverConfig.let {
                    isCardVisible = true
                    error = !it.loadJSON(response)
                    if (error) {
                        strError = "Error en la respuesta del servidor"
                    }
                }
            }

            else -> {
                isCardVisible = false
            }
        }
        true
    }

    fun onOkClick() {
        if (url.isNotEmpty()) {
            serverConfig = ServerConfig(url = url)
            val strUrl = serverConfig.getUrl("dispositivos/new/")
            HTTPRequest(strUrl, serverConfig.getParams(), "GETCONFIG", handler)
        }
    }

    fun onValidarClick() {
        if (url.isNotEmpty() && codigo.isNotEmpty()) {
            if (codigo == serverConfig.codigo) {
                serverConfig.toJson()?.let {
                    JSON.serializar("preferencias.dat", it, app.applicationContext)
                    error = false
                }
            } else {
                error = true
                strError = "Código incorrecto"
            }
        }
    }
}

