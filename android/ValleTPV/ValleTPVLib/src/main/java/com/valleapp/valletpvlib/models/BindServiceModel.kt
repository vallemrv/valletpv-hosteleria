package com.valleapp.valletpvlib.models

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
import com.valleapp.valletpvlib.interfaces.IServiceState
import com.valleapp.valletpvlib.tools.ServiceCom

class BindServiceModel(private val app: Application) : AndroidViewModel(app), IServiceState {


    private var mBound = false
    private var connection: ServiceConnection? = null

    var isAunthValid by mutableStateOf(true)

    var mService: ServiceCom? by mutableStateOf(null)


    override fun invalidateAuth() {
        isAunthValid = false
        println("Auth invalido")
    }


    fun bindService() {
        if (connection == null) {
            connection = object : ServiceConnection {
                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    val binder = service as ServiceCom.LocalBinder
                    mService = binder.getService()
                    mService!!.setValidador(this@BindServiceModel)
                    mBound = true
                    println("Servicio enlazado")
                }

                override fun onServiceDisconnected(arg0: ComponentName) {
                    println("Servicio desenlazado")
                    mBound = false
                }
            }
        }
        if (!mBound) {
            Intent(app.applicationContext, ServiceCom::class.java).also { intent ->
                app.applicationContext.bindService(
                    intent,
                    connection as ServiceConnection,
                    Context.BIND_AUTO_CREATE
                )
            }
        }
    }

    fun unbindService() {
        if (mBound) {
            mService?.let {
                app.applicationContext.unbindService(connection!!)
                mBound = false
            }
            println("Servicio desenlazado")
        }
    }

}