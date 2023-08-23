package com.valleapp.valletpv.models


import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CamarerosModel(app: Application, private val serverConfig: ServerConfig) :
    AndroidViewModel(app) {

    var showDialog: Boolean by mutableStateOf(false)

    private var mService: ServiceCom? by mutableStateOf(null)
    private var db: CamareroDao? = null

    fun addCamarero(camarero: Camarero) {
        viewModelScope.launch(Dispatchers.IO) {
            val inst = Instrucciones(
                params = serverConfig.getParams(mapOf("nombre" to camarero.nombre, "apellido" to camarero.nombre)),
                endPoint = ApiEndPoints.CAMAREROS_ADD,
            )
            mService?.addInstruccion(inst)
            db?.insertCamarero(camarero)
        }
    }

    fun setAutorizado(id: Long, b: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val inst = Instrucciones(
                params = serverConfig.getParams(mapOf("autorizado" to b, "id" to id)),
                endPoint = ApiEndPoints.CAMAREROS_SET_AUTORIZADO
            )
            mService?.addInstruccion(inst)
            db?.setAutorizado(id, b)
        }
    }

    fun setService(mService: ServiceCom?) {
        if (mService != null) {
            this.mService = mService
            mService.setServerConfig(serverConfig)
            db = mService.getDB("camareros") as CamareroDao
        }
    }


}

