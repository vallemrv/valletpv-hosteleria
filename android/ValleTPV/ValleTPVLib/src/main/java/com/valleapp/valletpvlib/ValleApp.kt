package com.valleapp.valletpvlib

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.valleapp.valletpvlib.models.MainModel
import com.valleapp.valletpvlib.tools.ServiceCom


class ValleApp : Application() {

    val mainModel: MainModel = MainModel(this)

    private var mBound = false
    private var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ServiceCom.LocalBinder
            mainModel.setService(binder.getService())
            mainModel.cargarPreferencias()
            mBound = true
            println("Servicio enlazado")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            println("Servicio desenlazado")
            mBound = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (!mBound) {
            Intent(this, ServiceCom::class.java).also { intent ->
                this.bindService(
                    intent,
                    connection,
                    Context.BIND_AUTO_CREATE
                )
            }
        }
    }


    override fun onTerminate() {
        super.onTerminate()
        if (mBound) {
            unbindService(connection)
            println("Servicio desenlazado")
        }
    }

    fun exit() {
        if (mBound) {
            this.unbindService(connection)
            mBound = false
        }
        val intent = Intent(this, ServiceCom::class.java)
        stopService(intent)

        Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    }

}