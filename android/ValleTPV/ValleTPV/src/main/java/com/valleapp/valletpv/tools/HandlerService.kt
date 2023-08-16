package com.valleapp.valletpv.tools

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.valleapp.valletpvlib.tools.ServiceCom

data class ServiceConnectionResult(
    var mBound: Boolean,
    val connection: ServiceConnection,
    var mService: ServiceCom?
)

fun createServiceConnection(): ServiceConnectionResult {
    var mBound = false
    var mService: ServiceCom? = null

    val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ServiceCom.LocalBinder
            mService = binder.getService()
            mBound = true
            println("Servicio enlazado")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    return ServiceConnectionResult(mBound, connection, mService)
}

