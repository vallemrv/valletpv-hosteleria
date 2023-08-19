package com.valleapp.valletpv.models


import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.tools.ApiEndPoints
import com.valleapp.valletpvlib.tools.Instrucciones
import com.valleapp.valletpvlib.tools.Mensaje
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CamarerosModel(private val app: Application, private val serverConfig: ServerConfig) :
    AndroidViewModel(app) {

    private var mService: ServiceCom? by mutableStateOf(null)
    private var mBound by mutableStateOf(false)
    private var connection: ServiceConnection? by mutableStateOf(null)

    var db: CamareroDao? by mutableStateOf(null)


    var showDialog: Boolean by mutableStateOf(false)
    var nombre: String by mutableStateOf("")
    var apellido: String by mutableStateOf("")
    private var mensaje: Mensaje by mutableStateOf(Mensaje("", ""))


    fun bindService() {

        connection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as ServiceCom.LocalBinder
                mService = binder.getService()
                mBound = true
                println("Servicio enlazado")
                if (mService != null) {
                    mService!!.setServerConfig(serverConfig)
                    db = mService!!.getDB("camareros") as CamareroDao
                }
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mBound = false
            }
        }
        Intent(app.applicationContext, ServiceCom::class.java).also { intent ->
            app.applicationContext.bindService(
                intent,
                connection as ServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    fun unbindService() {
        if (mBound) {
            mService?.let {
                app.applicationContext.unbindService(connection!!)
                mBound = false
            }
        }
    }

    fun addCamarero() {
        viewModelScope.launch(Dispatchers.IO) {
            val inst = Instrucciones(
                params = serverConfig.getParams(mapOf("nombre" to nombre, "apellido" to apellido)),
                endPoint = ApiEndPoints.CAMAREROS_ADD,
                mensaje = mensaje
            )
            mService?.addInstruccion(inst)

            if (db != null) {
                db!!.insertCamarero(
                    Camarero(
                        nombre = nombre,
                        apellidos = apellido
                    )
                )
                nombre = ""
                apellido = ""
            }
        }
    }

    fun setAutorizado(id: Long, b: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {

            db?.setAutorizado(id, b)
            val inst = Instrucciones(
                params = serverConfig.getParams(mapOf("autorizado" to b, "id" to id)),
                endPoint = ApiEndPoints.CAMAREROS_SET_AUTORIZADO,
                mensaje = mensaje
            )
            mService?.addInstruccion(inst)
        }
    }


}
