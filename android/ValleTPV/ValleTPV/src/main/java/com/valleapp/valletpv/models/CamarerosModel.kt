package com.valleapp.valletpv.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom

class CamarerosModel(cx: Context) : ViewModel() {

    var serverConfig: ServerConfig? by mutableStateOf(null)
    private var intent: Intent? by mutableStateOf(null)
    var mService: ServiceCom? by mutableStateOf(null)
    var mBound by mutableStateOf(false)
    var connection: ServiceConnection? by mutableStateOf(null)

    init {
        val preferencias = JSON.deserializar("preferencias.dat", cx)
        preferencias?.let {
            serverConfig = ServerConfig(
                url = it.getString("url"),
                codigo = it.getString("codigo"),
                UID = it.getString("UID")
            )
            intent = Intent(cx, ServiceCom::class.java).apply {
                putExtra("url", serverConfig?.url)
                putExtra("codigo", serverConfig?.codigo)
                putExtra("UID", serverConfig?.UID)
                putExtra("res_id", R.mipmap.ic_launcher)
                putExtra("chanel_id", "valle_tpv_channel")
                putExtra("titulo", "ValleTPV")
                putExtra("texto", "Servicio de comunicación")
            }
            cx.startService(intent)
            connection = object : ServiceConnection {

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
        }

    }





}