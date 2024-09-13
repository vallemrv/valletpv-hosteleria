package com.valleapp.vallecash

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.valleapp.valletpvlib.Interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.WSClient
import org.json.JSONObject

class WebSocketService : Service(), IControllerWS {

    private lateinit var wsClient: WSClient
    private var serverUrl = "wss://api.server.com"  // La URL de tu servidor
    private val endpoint = "/comunicacion/cashlogy"  // El endpoint de tu WebSocket



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::wsClient.isInitialized) {
            serverUrl = intent?.getStringExtra("SERVER_URL") ?: "wss://api.server.com" // URL por defecto
            wsClient = WSClient(serverUrl, endpoint, this)
            wsClient.connect()  // Conectar solo si no está inicializado
            Log.d("WebSocketService", "WebSocket conectado a $serverUrl")
        } else {
            Log.d("WebSocketService", "WebSocket ya está conectado")
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
            put("instruccion", instruction)  // Esto dependerá del formato esperado por tu servidor WebSocket
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
        Log.d("WebSocketService", "Sincronización solicitada")
    }

    // Recibir respuestas del WebSocket
    override fun procesarRespose(o: JSONObject) {
       if (o.has("respuesta") && o.has("instruccion")) {
           // Enviar el JSON recibido al Fragment a través de un Broadcast
           val intent = Intent("WebSocketMessage")
           intent.putExtra("instruccion", o.getString("instruccion"))
           intent.putExtra("message", o.getString("respuesta"))
           LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
       }
    }
}
