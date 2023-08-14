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
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig

class PreferenciasModel(val context: Context) : ViewModel() {



    var url by mutableStateOf("")
    var codigo by mutableStateOf("")
    var UID by mutableStateOf("")
    var isCardVisible by mutableStateOf(false)
    var serverConfig: ServerConfig? by mutableStateOf(null)

    init {
        JSON.deserializar("preferencias.dat", context)?.let {
            if (it.has("url")) {
                url = it.getString("url")
            }
            if (it.has("codigo")) {
                codigo = it.getString("codigo")
            }
            if (it.has("UID")) {
                UID = it.getString("UID")
            }
            // ... y cualquier otra clave que quieras verificar.
            isCardVisible = true
        }
    }

    private val handler = Handler(Looper.getMainLooper()) { message ->
        val response: String? = message.data.getString("RESPONSE")
        val op = message.data.getString("op")
        when (op) {
            "ERROR" -> {
                isCardVisible = false
                Toast.makeText(context, response, Toast.LENGTH_LONG).show()
            }
            "GETCONFIG" -> {
                serverConfig?.let{
                    isCardVisible = true
                    val isLoad = it.loadJSON(response)
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
            serverConfig = ServerConfig(url=url)
            var strUrl = serverConfig?.getUrl("dispositivos/new/")
            Log.d("PreferenciasModel", "onOkClick: $strUrl")
            strUrl?.let {
                HTTPRequest(it, serverConfig!!.getParams(), "GETCONFIG", handler)
            }

        }
    }

    fun onValidarClick(){
        if (url.isNotEmpty() && codigo.isNotEmpty()) {
            if (codigo.equals(serverConfig?.codigo)) {
                Toast.makeText(context, "Código correcto", Toast.LENGTH_LONG).show()
                serverConfig?.toJson()?.let { JSON.serializar("preferencias.dat", it, context) }
            } else {
                Toast.makeText(context, "Error código incorrecto", Toast.LENGTH_LONG).show()
            }
        }
    }
}

