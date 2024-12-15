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
import com.valleapp.vallecash.R
import com.valleapp.vallecash.ValleCASH
import com.valleapp.valletpvlib.interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.WSClient
import org.json.JSONObject


class WebSocketService : Service(), IControllerWS {

    private var wsClient: WSClient? = null
    private var serverUrl = ""  // La URL de tu servidor
    private val endpoint = "/comunicacion/cashlogy"  // El endpoint de tu WebSocket
    private val channelId = "WebSocketServiceChannel"  // ID del canal de notificación

    private fun startForegroundService() {
        // Verifica si el canal ya existe
        val channel = NotificationChannel(
            channelId,
            "ValleCASH service",
            NotificationManager.IMPORTANCE_DEFAULT  // IMPORTANCE_HIGH si necesitas más visibilidad
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // Ajusta según sea necesario
            .build()

        // Iniciar el servicio en primer plano con la notificación
        startForeground(1, notification)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverUrl = intent?.getStringExtra("SERVER_URL") ?: ""
        if (wsClient == null && serverUrl.isNotEmpty()) {
            println("Intentando conectar al WebSocket... $serverUrl")
            wsClient = WSClient(serverUrl, endpoint, this)
            wsClient!!.connect()
            Log.d("WebSocketService", "WebSocket conectado a $serverUrl")
            startForegroundService()
        }

        // Verificar si el intent contiene una instrucción para enviar
        val action = intent?.getStringExtra("action")
        if (action == "SEND_INSTRUCTION") {
            val instruction = intent.getStringExtra("instruction")
            if (instruction != null) {
                // Aquí llamamos al método que envía la instrucción al WebSocket
                sendInstructionToWebSocket(instruction)
            }
        }


        return START_STICKY
    }


    private fun sendInstructionToWebSocket(instruction: String) {
        // Crear un JSON con la instrucción que se va a enviar
        val jsonMessage = JSONObject().apply {
            put("instruccion", instruction)
            put( "device", "ValleCASH")
        }
        wsClient?.sendMessage(jsonMessage.toString()) // Enviar el mensaje al WebSocket
        Log.d("WebSocketService", "Instrucción enviada: $jsonMessage")
    }

    override fun onDestroy() {
        super.onDestroy()
        wsClient?.stopReconnection()
        Log.d("WebSocketService", "WebSocket desconectado")
    }

    // Método necesario para el servicio. No lo usamos en este caso.
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Métodos de la interfaz IControllerWS
    override fun sincronizar() {
        val intent = Intent("WebSocketMessage")
        intent.putExtra("instruccion", "sincronizar")
        intent.putExtra("message",  "")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    // Recibir respuestas del WebSocket
    override fun procesarRespose(o: JSONObject) {

        val device = o.getString("device")
        if (device == "ValleCASH") return
        // Enviar el JSON recibido al Fragment a través de un Broadcast
       val intent = Intent("WebSocketMessage")
       intent.putExtra("instruccion", o.getString("instruccion"))
       intent.putExtra("message", o.getString("respuesta"))
       LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }
}
