package com.valleapp.valletpvlib.models

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.IBaseDao
import com.valleapp.valletpvlib.interfaces.IServiceState
import com.valleapp.valletpvlib.tools.ApiRequest
import com.valleapp.valletpvlib.tools.Instrucciones
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom
import kotlinx.coroutines.launch


class MainModel(private val app: Application): AndroidViewModel(app), IServiceState {

    private var serverConfig: ServerConfig = ServerConfig()
    private var mService: ServiceCom? by mutableStateOf(null)


    var isAunthValid by mutableStateOf(false)
    var isPreferenciasCargadas by mutableStateOf(false)

    override fun invalidateAuth() {
        isAunthValid = false
        println("Auth invalido")
    }

    fun cargarPreferencias() {
         if (!isPreferenciasCargadas) {
            JSON.deserializar("preferencias.dat", app.applicationContext)?.let {
                if (it.has("url")) {
                    serverConfig.url = it.getString("url")
                }
                if (it.has("codigo")) {
                    serverConfig.codigo = it.getString("codigo")
                }
                if (it.has("UID")) {
                    serverConfig.uid = it.getString("UID")
                }
                isPreferenciasCargadas = !serverConfig.isEmpty()

                if (isPreferenciasCargadas) {
                    mService?.runSync(serverConfig, this)
                    isAunthValid = true
                }
            }
        }
    }

    fun guardarPreferencias() {
        serverConfig.toJson().let {
            if (it != null) {
                JSON.serializar("preferencias.dat", it, app.applicationContext)
                isAunthValid = true
                isPreferenciasCargadas = true
                mService?.runSync(serverConfig, this)
            }
        }
    }

    fun setUrl(url: String) {
        serverConfig.url = url
        ApiRequest.init(serverConfig.getParseUrl())
    }

    fun loadJSON(data: Map<String, Any>) {
        serverConfig.loadJSON(data)
    }

    fun validarCodigo(codigo: String): Boolean {
         return codigo == serverConfig.codigo
    }

    fun getParams(mapOf: Map<String, Any>): Map<String, String> {
        return serverConfig.getParams(mapOf)
    }

    fun addInstruccion(inst: Instrucciones) {
        mService?.addInstruccion(inst)
    }

    fun getDB(s: String): IBaseDao<*>? {
        return mService?.getDB(s)
    }

    fun getUrl(url: String?): String {
        return serverConfig.getUrlBase() + if (url != null) url else ""
    }

    fun setService(service: ServiceCom) {
        mService = service
    }

    fun getCodigo(): String? {
        return serverConfig.codigo
    }

    fun preImprimir(mesaId: Long) {
        viewModelScope.launch {
            mService?.preImprimir(mesaId)
        }
    }

    fun abrirCajon() {
        viewModelScope.launch {
            mService?.abrirCajon()
        }
    }


}
