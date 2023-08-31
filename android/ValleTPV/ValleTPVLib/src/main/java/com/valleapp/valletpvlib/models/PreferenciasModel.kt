package com.valleapp.valletpvlib.models


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

class PreferenciasModel(
    private val app: Application,
) : AndroidViewModel(app) {


    var serverConfig: ServerConfig by mutableStateOf(ServerConfig())

    var url by mutableStateOf("")
    var codigo by mutableStateOf("")

    var showMessage: Boolean by mutableStateOf(false)
    var message: String by mutableStateOf("Prefencias cargadas correctamente")

    var isCardVisible by mutableStateOf(false)

    var preferenciasCargadas by mutableStateOf(false)


    init {
        println("PreferenciasModel init")
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
                if (!preferenciasCargadas) message = "Error al cargar preferencias"
                showMessage = true
            }
        }
    }

    fun mostrarMessage(msg: String) {
        showMessage = true
        message = msg
    }


    fun onOkClick() {
        if (url.isNotEmpty()) {
            codigo = ""
            serverConfig.url = url
            preferenciasCargadas = false

            ApiRequest.init(serverConfig.getParseUrl())

            viewModelScope.launch {
                val result = safeApiCall {
                    ApiRequest.service.post(ApiEndPoints.DISPOSITIVO_NUEVO, mapOf())
                }
                when (result) {
                    is ApiResponse.Success -> {
                        serverConfig.loadJSON(result.data)
                        serverConfig.toJson()?.let {
                            JSON.serializar("preferencias.dat", it, app.applicationContext)
                        }
                        isCardVisible = true

                        mostrarMessage("Dispisitivo registrado correctamente, compruebe codigo.")
                    }

                    is ApiResponse.Error -> {
                        mostrarMessage(result.errorMessage.toString())

                    }

                }
            }

        }
    }

    fun onValidarClick() {
        if (!serverConfig.isEmpty() && serverConfig.isEqualsCode(codigo)) {
            serverConfig.toJson()?.let {
                JSON.serializar("preferencias.dat", it, app.applicationContext)
                mostrarMessage("Preferencias guardadas correctamente")
                preferenciasCargadas = true
            } ?: run {
                mostrarMessage("Error al guardar preferencias")
            }
        } else {
            mostrarMessage("Codigo inválido")
            codigo = ""
        }
    }
}


