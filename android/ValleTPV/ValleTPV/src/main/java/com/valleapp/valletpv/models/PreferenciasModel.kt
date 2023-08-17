package com.valleapp.valletpv.models


import android.content.ContentValues
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.tools.HTTPRequest
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig
import kotlinx.coroutines.launch
import org.json.JSONObject

class PreferenciasModel(cx: Context) : ViewModel() {

    var url by mutableStateOf("")
    var codigo by mutableStateOf("")
    var isCardVisible by mutableStateOf(false)
    var serverConfig: ServerConfig by mutableStateOf(ServerConfig())
    var preferenciasCargadas by mutableStateOf(false)
    var error: Boolean by mutableStateOf(false)
    var strError: String by mutableStateOf("")
    private val context: Context = cx

    val hanler: Handler = Handler(Looper.getMainLooper()){
        val respose = it.data.getString("response") ?: ""
        val op = it.data.getString("op") ?: ""
        try {
            when(op){
                "ERROR" -> {
                    error = true
                    strError = respose
                }
                "GET_CODIGO" -> {
                    val obj = JSONObject(respose)
                    if (obj.has("codigo")) {
                        codigo = ""
                        serverConfig.codigo = obj.getString("codigo")
                        serverConfig.UID = obj.getString("UID")
                        isCardVisible = true
                        error = false
                        preferenciasCargadas = true
                        serverConfig.toJson()?.let { json ->
                            JSON.serializar("preferencias.dat", json, context.applicationContext)
                        }
                    }
                }
                else -> {
                    error = true
                    strError = "Operación no soportada"
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
       true
    }

    init {

        if (!preferenciasCargadas) {
            JSON.deserializar("preferencias.dat", cx.applicationContext)?.let {
                if (it.has("url")) {
                    url = it.getString("url")
                    serverConfig.url = url
                }
                if (it.has("codigo")) {
                    codigo = it.getString("codigo")
                    serverConfig.codigo = codigo
                }
                if (it.has("UID")) {
                   serverConfig.UID = it.getString("UID")
                }
                preferenciasCargadas = !serverConfig.isEmpty()
                isCardVisible = preferenciasCargadas
                error = !preferenciasCargadas
                if (error) {
                    strError = "Error al cargar preferencias"
                }
            }
        }
    }


    fun onOkClick() {
        if (!serverConfig.isEmpty()) serverConfig.url = ServerConfig.parseUrl(serverConfig.url!!)
        viewModelScope.launch {
            HTTPRequest(
                serverConfig.getFullUrl("/dispositivos/new/"),
                ContentValues(),
                "GET_CODIGO",
                hanler
            )
        }
    }

    fun onValidarClick() {
        if (!serverConfig.isEmpty() && !serverConfig.isEqualsCode(codigo)) {
            serverConfig.toJson()?.let {
                JSON.serializar("preferencias.dat", it, context.applicationContext)
                error = false
                preferenciasCargadas = true
            } ?: run {
                error = true
                strError = "Error al guardar preferencias"
                preferenciasCargadas = false
            }
        } else {
            error = true
            strError = "Código incorrecto"
            preferenciasCargadas = false
        }
    }
}


