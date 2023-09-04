package com.valleapp.valletpvlib.models


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.ApiRequest
import com.valleapp.valletpvlib.tools.ApiResponse
import com.valleapp.valletpvlib.tools.safeApiCall
import kotlinx.coroutines.launch

class PreferenciasModel(private val mainModel: MainModel, message: String): ViewModel() {

    var url by mutableStateOf("")
    var codigo by mutableStateOf("")

    var showMessage: Boolean by mutableStateOf(false)
    var message: String by mutableStateOf("Prefencias cargadas correctamente")

    var isCardVisible by mutableStateOf(false)


   init {
       mostrarMessage(message)
        if (mainModel.isPreferenciasCargadas) {
            url = mainModel.getUrl(null)
            codigo = mainModel.getCodigo().toString()
            isCardVisible = true
        }
    }

    private fun mostrarMessage(msg: String) {
        showMessage = true
        message = msg
    }


    fun onOkClick() {
        if (url.isNotEmpty()) {
            codigo = ""
            mainModel.setUrl(url)

            viewModelScope.launch {
                val result = safeApiCall {
                    ApiRequest.service.post(ApiEndPoints.DISPOSITIVO_NUEVO, mapOf())
                }
                when (result) {
                    is ApiResponse.Success -> {
                        mainModel.loadJSON(result.data)
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
        if (mainModel.validarCodigo(codigo)) {
            mainModel.guardarPreferencias()
            isCardVisible = false
            mostrarMessage("Preferencias guardadas correctamente")
        } else {
            mostrarMessage("Codigo inválido")
            codigo = ""
        }
    }
}


