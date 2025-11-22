package com.valleapp.vallecash.tools

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.valleapp.vallecash.ValleCASH
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.WSClient
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class WebSocketService : Service(), IControllerWS {

    private var wsClient: WSClient? = null
    private var serverUrl = "" // La URL de tu servidor
    private var endpoint = "/comunicacion/cashlogy" // El endpoint de tu WebSocket
    private val channelId = "WebSocketServiceChannel" // ID del canal de notificación

    private fun startForegroundService() {
        // Verifica si el canal ya existe
        val channel = NotificationChannel(
            channelId,
            "ValleCASH service",
            NotificationManager.IMPORTANCE_DEFAULT // IMPORTANCE_HIGH si necesitas más visibilidad
        )
        val manager = getSystemService(NotificationManager::class.java)
        if (manager?.getNotificationChannel(channelId) == null) {
            manager?.createNotificationChannel(channel)
        }

        // Configurar la notificación para el servicio en primer plano
        val notificationIntent = Intent(this, ValleCASH::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("WebSocket activo")
            .setContentText("Conectado a $serverUrl")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Ajusta según sea necesario
            .build()

        // Iniciar el servicio en primer plano con la notificación
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverUrl = intent?.getStringExtra("SERVER_URL") ?: ""
        if (wsClient == null && serverUrl.isNotEmpty()) {
            val uid = cargarUid()
            endpoint = if (uid.isNotEmpty()) "/comunicacion/cashlogy?uid=$uid" else "/comunicacion/cashlogy"
            println("Intentando conectar al WebSocket... $serverUrl$endpoint")
            wsClient = WSClient(serverUrl, endpoint, this)
            wsClient?.connect()
            Log.d("WebSocketService", "WebSocket conectado a $serverUrl$endpoint")
            startForegroundService()
        }

        // Verificar si el intent contiene una instrucción para enviar
        val action = intent?.getStringExtra("action")
        when (action) {
            "SEND_INSTRUCTION" -> {
                val instruction = intent.getStringExtra("instruction")
                instruction?.let {
                    sendInstructionToWebSocket(it)
                }
            }
            "STOP_SERVICE" -> {
                // Acción para cerrar completamente el servicio
                stopService()
                return START_NOT_STICKY // No reiniciar automáticamente
            }
        }

        return START_STICKY
    }

    private fun sendInstructionToWebSocket(instruction: String) {
        // Crear un JSON con la instrucción que se va a enviar
        val jsonMessage = JSONObject().apply {
            put("instruccion", instruction)
            put("device", "ValleCASH")
        }
        wsClient?.sendMessage(jsonMessage.toString()) // Enviar el mensaje al WebSocket
        Log.d("WebSocketService", "Instrucción enviada: $jsonMessage")
    }

    // Método para cerrar completamente el servicio y las conexiones
    fun stopService() {
        try {
            wsClient?.stopReconnection()
            wsClient?.close() // Desconectar WebSocket
            wsClient = null
            stopSelf() // Detener el servicio
            Log.d("WebSocketService", "Servicio y WebSocket cerrados completamente")
        } catch (e: Exception) {
            Log.e("WebSocketService", "Error al cerrar el servicio: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            wsClient?.stopReconnection()
            wsClient?.close()
            wsClient = null
            Log.d("WebSocketService", "WebSocket desconectado en onDestroy")
        } catch (e: Exception) {
            Log.e("WebSocketService", "Error en onDestroy: ${e.message}")
        }
    }

    // Método necesario para el servicio. No lo usamos en este caso.
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Métodos de la interfaz IControllerWS
    override fun sincronizar() {
        val intent = Intent("WebSocketMessage").apply {
            putExtra("instruccion", "sincronizar")
            putExtra("message", "")
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    // Recibir respuestas del WebSocket
    override fun procesarRespose(o: JSONObject) {
        val device = o.optString("device", "ValleCASH")
        if (device == "ValleCASH") return
        // Enviar el JSON recibido al Fragment a través de un Broadcast
        val intent = Intent("WebSocketMessage").apply {
            putExtra("instruccion", o.getString("instruccion"))
            putExtra("message", o.getString("respuesta"))
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun cargarUid(): String {
        val json = JSON()
        return try {
            val obj = json.deserializar("settings.dat", this)
            obj?.optString("uid", "") ?: ""
        } catch (e: JSONException) {
            e.printStackTrace()
            ""
        }
    }
}