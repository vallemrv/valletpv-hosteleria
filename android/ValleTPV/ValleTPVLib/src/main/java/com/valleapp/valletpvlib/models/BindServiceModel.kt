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
import com.valleapp.valletpvlib.tools.ServiceCom

class BindServiceModel(private val app: Application): AndroidViewModel(app) {


    private var mBound by mutableStateOf(false)
    private var connection: ServiceConnection? by mutableStateOf(null)
    var mService: ServiceCom? by mutableStateOf(null)

    fun bindService(onBind: (  ) -> Unit = {}) {

        connection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as ServiceCom.LocalBinder
                mService = binder.getService()
                mBound = true
                println("Servicio enlazado")
                if (mService != null)  onBind()
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
}