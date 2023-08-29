package com.valleapp.valletpv.models


import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.ApiRequest
import com.valleapp.valletpvlib.tools.ApiResponse
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.safeApiCall
import kotlinx.coroutines.launch

class PreferenciasModel(private val app: Application) : AndroidViewModel(app) {

    var url by mutableStateOf("")
    var codigo by mutableStateOf("")
    var isCardVisible by mutableStateOf(false)
    var serverConfig: ServerConfig = ServerConfig()
    var preferenciasCargadas by mutableStateOf(false)
    var error: Boolean by mutableStateOf(false)
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
                    serverConfig.uid = it.getString("UID")
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

    private fun mostrarError(msg: Any, isCardVisible: Boolean = false){
        error = true
        strError = msg.toString()
        this.isCardVisible = isCardVisible
        preferenciasCargadas = false
    }


    fun onOkClick() {
        if (url.isNotEmpty()) {
            codigo = ""
            serverConfig.url = url
            ApiRequest.init(serverConfig.getParseUrl())
            viewModelScope.launch {
                val result = safeApiCall {
                    ApiRequest.service.post(ApiEndPoints.DISPOSITIVO_NUEVO, mapOf())
                }
                when (result) {
                    is ApiResponse.Success -> {
                        serverConfig.loadJSON(result.data)
                        isCardVisible = true
                        error = false
                        preferenciasCargadas = true
                        serverConfig.toJson()?.let {
                            JSON.serializar("preferencias.dat", it, app.applicationContext)
                        }
                    }

                    is ApiResponse.Error -> {
                        mostrarError(result.errorMessage)
                    }

                    else -> {
                        mostrarError("Error desconocido")
                    }
                }
            }

        }
    }

    fun onValidarClick() {
        if (!serverConfig.isEmpty() && serverConfig.isEqualsCode(codigo)) {
            serverConfig.toJson()?.let {
                JSON.serializar("preferencias.dat", it, app.applicationContext)
                error = false
                preferenciasCargadas = true
            } ?: run {
               mostrarError("Error al guardar preferencias")
            }
        } else {
            mostrarError("Codigo inválido", true)
            codigo  = ""
        }
    }
}


