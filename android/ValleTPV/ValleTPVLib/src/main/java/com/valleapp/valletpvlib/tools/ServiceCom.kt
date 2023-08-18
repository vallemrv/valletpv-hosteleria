package com.valleapp.valletpvlib.tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.valleapp.valletpvlib.db.AppDatabase
import com.valleapp.valletpvlib.tools.tareas.InstruccionesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ServiceCom : Service() {

    private val binder = LocalBinder()
    private var serverConfig: ServerConfig? = null
    private var appDatabase: AppDatabase? = null
    private val procesarCola = InstruccionesManager()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appDatabase = AppDatabase.getDatabase(this)
        intent?.let {
            val resId = it.getIntExtra("res_id", 0)
            val chanelId = it.getStringExtra("chanel_id")
            val titulo = it.getStringExtra("titulo")
            val texto = it.getStringExtra("texto")


             if (chanelId != null && texto != null && titulo != null && resId != 0) {
                createNotificationChannel(chanelId, texto)
                val notification = NotificationCompat.Builder(this, chanelId )
                    .setContentTitle(titulo)
                    .setContentText(texto)
                    .setSmallIcon(resId)
                    .build()

                startForeground(1, notification)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        procesarCola.stopProcesarCola()
    }

    private fun createNotificationChannel(id: String, name: String) {
        val serviceChannel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    inner class LocalBinder : Binder() {
        fun getService(): ServiceCom = this@ServiceCom
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setServerConfig(serverConfig: ServerConfig) {
        if (this.serverConfig == null) {
            this.serverConfig = serverConfig
            ApiRequest.init(serverConfig.getParseUrl())
            GlobalScope.launch {
                procesarCola.procesarCola()
            }
            println("ServiceCom: setServerConfig")
        }
    }



    fun getDB(): AppDatabase? {
        return appDatabase
    }

    fun addInstruccion(inst: Instrucciones) {
        procesarCola.addInstruccion(inst)
        println("ServiceCom: addInstruccion")
    }
}
