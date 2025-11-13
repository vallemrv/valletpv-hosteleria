package com.valleapp.vallecom.utilidades

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import com.valleapp.vallecom.activitys.Camareros
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.ServiceBase
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.db.DBReceptores
import com.valleapp.valletpvlib.db.DBSugerencias
import com.valleapp.valletpvlib.interfaces.IBaseDatos
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask



class ServiceCOM: ServiceBase() {

    // Use 'val' for final fields, 'var' for mutable fields.
    // Initialize directly or in an init block/constructor.
    // Explicitly declare types or let Kotlin infer them.
    private val myBinder: IBinder = MyBinder()
    private val channelId = "ValleCOM"
    private val statusWs: Timer = Timer()

    // Use 'var' and nullable type 'JSONObject?'
    private var cam: JSONObject? = null


    override fun onCreate() {
        super.onCreate()
        // Initialization or setup code here if needed
    }

    override fun startForegroundService() {
        // Verifica si el canal ya existe
        val channel = NotificationChannel(
            channelId,
            "ValleTPV service",
            NotificationManager.IMPORTANCE_DEFAULT  // IMPORTANCE_HIGH si necesitas más visibilidad
        )
        val manager = getSystemService(NotificationManager::class.java)
        if (manager?.getNotificationChannel(channelId) == null) {
            manager?.createNotificationChannel(channel)
        }

        // Configurar la notificación para el servicio en primer plano
        val notificationIntent = Intent(this, Camareros::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("WebSocket activo")
            .setContentText("Conectado a ${getServerUrl()}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // Ajusta según sea necesario
            .build()

        // Iniciar el servicio en primer plano con la notificación
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val out = super.onStartCommand(intent, flags, startId)
        statusWs.schedule(object : TimerTask() {
            override fun run() {
                val numArticulosEnCola = obtenerNumeroDeArticulosEnCola() // Método para obtener el número de artículos
                val estadoWebSocket = verificarEstadoWebSocket() // Método para verificar si el WebSocket está conectado
                 // Crear un Bundle para los datos
                val bundle = Bundle().apply {
                    putString("numPendientes", numArticulosEnCola.toString())
                    putBoolean("status", estadoWebSocket)
                }
                
                getExHandler("estadows")?.obtainMessage()?.apply {
                    what = "estadows".hashCode() // Identificador del mensaje
                    data = bundle
                    sendToTarget()
                }
            }
        }, 0, 1000) // Ejecuta cada 1000 ms (1 segundo)
        return out
    }

    /**
     * Returns the binder instance. This method is essential for bound services.
     * It was missing in the original Java snippet but is required if you intend
     * clients to bind to this service using the MyBinder interface.
     */
    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    // Use 'inner class' to access members of the outer class (ServicioCom)
    inner class MyBinder : Binder() {
        // Function definition using 'fun'. Specify the return type.
        fun getService(): ServiceCOM {
            // Use 'this@OuterClassName' to refer to the outer class instance
            return this@ServiceCOM
        }
    }

    fun setCam(cam: JSONObject?) {
        this.cam = cam
        comprobarMensajes()

    }

    fun getCam(): JSONObject? {
        return cam
    }

    private fun comprobarMensajes() {
        try {
            val h: Handler? = getExHandler("mensajes")
            if (cam != null && h != null && server != null) {
                val p = ContentValues()
                p.put("idautorizado", cam!!.getString("ID"))
                p.put("uid", getUid())
                HTTPRequest("$server/autorizaciones/get_lista_autorizaciones", p, "men_lista", h)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun iniciarDB() {
        super.iniciarDB() // Llama al método base para inicializar las bases de datos comunes

        // Agregar e inicializar las bases de datos específicas de ServiceCOM
        (dbs as HashMap<String?, IBaseDatos>)["sugerencias"] = DBSugerencias(applicationContext)
        (dbs as HashMap<String?, IBaseDatos>)["receptores"] = DBReceptores(applicationContext)

        for (db in listOf("sugerencias", "receptores")) {
            (dbs as HashMap<String?, IBaseDatos>)[db]?.inicializar()
        }

        // Convertir tbNameUpdateLow a una lista mutable si no es null, o inicializarla como una lista mutable
        tbNameUpdateLow = (tbNameUpdateLow?.toMutableList() ?: mutableListOf()).apply {
            addAll(listOf("sugerencias", "receptores"))
        }.toTypedArray()
    }

    override fun procesarRespose(o: JSONObject) {
        super.procesarRespose(o) // Llama a la implementación base para actualizar DBs

        try {
            val op = o.getString("op")
            if (op.equals("men", ignoreCase = true)) {

                getExHandler("mensajes")?.let { handler ->
                    val messageBundle = Bundle().apply {
                        putString("op", "men_once") // Como se espera en Mesas.kt para un solo mensaje
                        putString("RESPONSE", o.getJSONObject("obj").toString()) // El objeto del mensaje como String
                    }
                    val msg = Message().apply {
                        data = messageBundle // Asigna el Bundle al Message
                    }
                    handler.sendMessage(msg) // Envía el Message al handler
                }
            }
        } catch (e: Exception) {
            Log.e("ServiceCOM", "Error en procesarRespose al manejar 'men': $e")
        }
    }



    fun comprobarCamareros(handler: Handler) {
        val uid = getUid() // Obtener el UID directamente desde ServiceBase
        val server = getServerUrl() // Obtener la URL directamente desde ServiceBase
        if (uid == null || server == null) {
            Log.e("ServiceCOM", "UID o URL no proporcionados para comprobar camareros.")
            return
        }
        val params = ContentValues()
        params.put("uid", uid)
        HTTPRequest("$server/camareros/listado", params, "listado", handler)
    }


}