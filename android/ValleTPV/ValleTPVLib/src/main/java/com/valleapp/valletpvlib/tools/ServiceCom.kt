package com.valleapp.valletpvlib.tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ServiceCom : Service() {

    private val binder = LocalBinder()
    var serverConfig: ServerConfig? = null

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            var url = it.getStringExtra("url")
            var codigo = it.getStringExtra("codigo")
            var UID = it.getStringExtra("UID")
            var res_id = it.getIntExtra("res_id", 0)
            var chanel_id = it.getStringExtra("chanel_id")
            var titulo = it.getStringExtra("titulo")
            var texto = it.getStringExtra("texto")
            if (url != null && codigo != null && UID != null)
                serverConfig = ServerConfig(url = url, codigo = codigo, UID = UID)


            println("url: $url  codigo: $codigo  UID: $UID  res_id: $res_id  chanel_id: $chanel_id  titulo: $titulo  texto: $texto")
            if (chanel_id != null && texto != null && titulo != null && res_id != 0) {
                createNotificationChannel(chanel_id, texto)
                val notification = NotificationCompat.Builder(this, chanel_id )
                    .setContentTitle(titulo)
                    .setContentText(texto)
                    .setSmallIcon(res_id)
                    .build()

                startForeground(1, notification)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                id,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): ServiceCom = this@ServiceCom
    }
}
