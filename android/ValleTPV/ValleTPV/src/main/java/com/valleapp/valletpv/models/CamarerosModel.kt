package com.valleapp.valletpv.models


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CamarerosModel(cx: Context, serverConfig: ServerConfig) : ViewModel() {

    private var mService: ServiceCom? by mutableStateOf(null)
    private var mBound by mutableStateOf(false)
    private var connection: ServiceConnection? by mutableStateOf(null)
    private var context: Context by mutableStateOf(cx)
    private var serverConfig: ServerConfig by mutableStateOf(serverConfig)

    var db: CamareroDao? by mutableStateOf(null)


    private val handler = Handler(Looper.myLooper()!!) {
        val res = it.data.getString("RESPONSE")
        val op = it.data.getString("op")
        try {
            when (op) {
                "ADD_CAMARERO" -> {
                    println("ADD_CAMARERO")
                    println(res)
                }
            }
        } catch (e: Exception) {
            println("Error en el handler")
        }
        true
    }

    var showDialog: Boolean by mutableStateOf(false)
    var nombre: String by mutableStateOf("")
    var apellido: String by mutableStateOf("")


    fun bindService() {

        connection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as ServiceCom.LocalBinder
                mService = binder.getService()
                mBound = true
                println("Servicio enlazado")
                if (mService != null) {
                    mService!!.setServerConfig(serverConfig)
                    db = mService!!.getDB()?.camareroDao()

                }
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mBound = false
            }
        }
        Intent(context, ServiceCom::class.java).also { intent ->
            context.bindService(
                intent,
                connection as ServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }


    }

    fun unbindService() {
        if (mBound) {
            mService?.let {
                context.unbindService(connection!!)
                mBound = false
            }
        }
    }

    fun add_camrarero() {
        viewModelScope.launch(Dispatchers.IO) {
            /*var inst = Instrucciones(params = serverConfig.getParams(mapOf("nombre" to nombre, "apellido" to apellido)),
            url = serverConfig.getFullUrl("camareros/add/"), op = "ADD_CAMARERO",
            handler = handler)
            mService?.addInstruccion(inst)*/
            println(db.toString())
            if (db != null) {
                // id unico

                val id = System.currentTimeMillis()
                db!!.insertCamarero(
                    Camarero(
                        nombre = nombre,
                        apellidos = apellido,
                        activo = true,
                        passField = "",
                        permisos = "",
                        autorizado = true
                    )
                )
            }
        }
    }

    fun setAutorizado(id: Long, b: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.setAutorizado(id, b)
        }
    }


}
