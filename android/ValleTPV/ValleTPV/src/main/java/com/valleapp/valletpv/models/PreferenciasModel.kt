package com.valleapp.valletpv.models


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.valleapp.valletpvlib.tools.HTTPRequest
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.JSON

class PreferenciasModel(val context: Context) : ViewModel() {

    var url by mutableStateOf("")
    var codigo by mutableStateOf("")
    var isCardVisible by mutableStateOf(false)
    private lateinit var serverConfig: ServerConfig

    private val handler = Handler(Looper.getMainLooper()) { message ->
        val response: String? = message.data.getString("RESPONSE")
        val op = message.data.getString("op")
        Log.d("PreferenciasModel", "handler: $op")
        Log.d("PreferenciasModel", "handler: $response")
        when (op) {
            "ERROR" -> {
                isCardVisible = false
                Toast.makeText(context, response, Toast.LENGTH_LONG).show()
            }
            "GETCONFIG" -> {
                if (::serverConfig.isInitialized) {
                    isCardVisible = true
                    val isLoad = serverConfig.loadJSON(response)
                    if (!isLoad) {
                       Toast.makeText(context, "Error al cargar la configuración", Toast.LENGTH_LONG).show()
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
            serverConfig = ServerConfig(url)
            var strUrl = serverConfig.getUrl("dispositivos/new/")

            if (strUrl != null) HTTPRequest(strUrl.toString(), serverConfig.getParams(), "GETCONFIG", handler)
        }
    }

    fun onValidarClick(){
        if (url.isNotEmpty() && codigo.isNotEmpty()) {
            Log.d("PreferenciasModel", "$codigo")
            Log.d("PreferenciasModel", "${serverConfig.codigo}")
            if (codigo.equals(serverConfig.codigo)) {
                Toast.makeText(context, "Código correcto", Toast.LENGTH_LONG).show()
                JSON.serializar("preferencias.dat", serverConfig.toJson(), context)
            } else {
                Toast.makeText(context, "Error código incorrecto", Toast.LENGTH_LONG).show()
            }
        }
    }
}

