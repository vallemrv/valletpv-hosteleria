package com.valleapp.vallecash

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.valleapp.valletpvlib.Interfaces.IControllerWS
import com.valleapp.valletpvlib.comunicacion.WSClient
import org.json.JSONObject

class WebSocketService : Service(), IControllerWS {

    private lateinit var wsClient: WSClient
    private val serverUrl = "wss://api.server.com"  // La URL de tu servidor
    private val endpoint = "/comunicacion/cashlogy"  // El endpoint de tu WebSocket



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serverUrl = intent?.getStringExtra("SERVER_URL") ?: "wss://api.server.com" // URL por defecto
        wsClient = WSClient(serverUrl, endpoint, this)
        wsClient.connect()

        return START_STICKY
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

    override fun procesarRespose(o: JSONObject) {
        Log.d("WebSocketService", "Mensaje recibido: $o")
    }
}
