package com.valleapp.vallecash

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
import com.valleapp.valletpvlib.Interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.WSClient
import org.json.JSONObject

class WebSocketService : Service(), IControllerWS {

    private lateinit var wsClient: WSClient
    private var serverUrl = ""  // La URL de tu servidor
    private val endpoint = "/comunicacion/cashlogy"  // El endpoint de tu WebSocket
    private val CHANNEL_ID = "WebSocketServiceChannel"  // ID del canal de notificación

    private fun startForegroundService() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ValleCASH service",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

        val notificationIntent = Intent(this, ValleCASH::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WebSocket activo")
            .setContentText("Conectado a $serverUrl")
            .setSmallIcon(R.drawable.ic_cashlogy)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)  // Iniciar el servicio en primer plano
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::wsClient.isInitialized) {
            serverUrl = intent?.getStringExtra("SERVER_URL") ?: ""
            wsClient = WSClient(serverUrl, endpoint, this)
            wsClient.connect()  // Conectar solo si no está inicializado
            Log.d("WebSocketService", "WebSocket conectado a $serverUrl")
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
        // Iniciar el servicio en primer plano
        startForegroundService()
        return START_STICKY
    }


    private fun sendInstructionToWebSocket(instruction: String) {
        // Crear un JSON con la instrucción que se va a enviar
        val jsonMessage = JSONObject().apply {
            put("instruccion", instruction)
            put( "device", "ValleCASH")
        }
        wsClient.sendMessage(jsonMessage.toString()) // Enviar el mensaje al WebSocket
        Log.d("WebSocketService", "Instrucción enviada: $jsonMessage")
    }

    override fun onDestroy() {
        super.onDestroy()

        // Cierra la conexión del WebSocket cuando el servicio se destruye
        wsClient.stopReconnection()
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
