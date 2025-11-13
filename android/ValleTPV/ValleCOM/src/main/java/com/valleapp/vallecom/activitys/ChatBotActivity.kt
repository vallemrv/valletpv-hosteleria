package com.valleapp.vallecom.activitys

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R
import com.valleapp.vallecom.adapters.ChatAdapter
import com.valleapp.vallecom.adapters.ChatMessage
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

class ChatBotActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChatBotActivity"
        private const val RECORD_AUDIO_PERMISSION_CODE = 1001
        
        // Constantes para reconexión
        private const val INITIAL_RECONNECT_DELAY_MS = 1000L // 1 segundo
        private const val MAX_RECONNECT_DELAY_MS = 30000L // 30 segundos
        private const val MAX_RECONNECT_ATTEMPTS = 10
        private const val RECONNECT_MULTIPLIER = 2.0
    }

    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var buttonRecord: ImageButton

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFile: File? = null

    // WebSocket variables
    private var webSocket: WebSocket? = null
    private var deviceUID: String = ""
    private var camareroId: String = ""
    private var serverUrl: String = ""
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Variables para reconexión automática
    private var reconnectAttempts = 0
    private var currentReconnectDelay = INITIAL_RECONNECT_DELAY_MS
    private var reconnectHandler = Handler(Looper.getMainLooper())
    private var reconnectRunnable: Runnable? = null
    private var isConnecting = false
    private var shouldReconnect = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatbot)

        // Obtener parámetros del intent
        getIntentParams()

        initViews()
        setupRecyclerView()
        setupClickListeners()
        checkAudioPermission()

        // Conectar al WebSocket
        connectToWebSocket()
    }

    private fun getIntentParams() {
        deviceUID = intent.getStringExtra("uid") ?: ""
        camareroId = intent.getStringExtra("camarero") ?: ""
        serverUrl = intent.getStringExtra("url") ?: ""

        Log.d(TAG, "Parámetros recibidos - UID: $deviceUID, Camarero: $camareroId, Server: $serverUrl")

        if (deviceUID.isEmpty() || serverUrl.isEmpty()) {
            Toast.makeText(this, "Error: Faltan parámetros necesarios (uid, url)", Toast.LENGTH_LONG).show()
            finish()
            return
        }
    }

    private fun initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        buttonRecord = findViewById(R.id.buttonRecord)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        recyclerViewMessages.adapter = chatAdapter
    }

    private fun setupClickListeners() {
        buttonSend.setOnClickListener {
            sendTextMessage()
        }

        buttonRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun sendTextMessage() {
        val message = editTextMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            addUserMessage(message)
            editTextMessage.text.clear()

            // Enviar mensaje a través del WebSocket
            sendMessageToWebSocket(message)
        }
    }

    private fun sendMessageToWebSocket(message: String) {
        try {
            // Verificar si hay conexión antes de enviar
            if (webSocket == null || !isWebSocketConnected()) {
                addBotMessage("⚠️ Sin conexión. Intentando reconectar...")
                if (shouldReconnect) {
                    forceReconnect()
                }
                return
            }

            val jsonMessage = JSONObject().apply {
                put("type", "message")
                put("text", message)
                put("sender", "user")  // Cambiado de "device" a "user" para consistencia con pedidos_consumer
                put("camarero_id", camareroId)
            }

            val success = webSocket?.send(jsonMessage.toString()) ?: false
            if (success) {
                Log.d(TAG, "Mensaje enviado al WebSocket: $message")
            } else {
                Log.w(TAG, "No se pudo enviar el mensaje, WebSocket no disponible")
                addBotMessage("⚠️ Mensaje no enviado. Reintentando conexión...")
                if (shouldReconnect) {
                    forceReconnect()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error enviando mensaje al WebSocket", e)
            addBotMessage("❌ Error enviando mensaje: ${e.message}")
        }
    }

    private fun addUserMessage(message: String) {
        val chatMessage = ChatMessage(message, true)
        chatAdapter.addMessage(chatMessage)
        scrollToBottom()
    }

    private fun addBotMessage(message: String) {
        val chatMessage = ChatMessage(message, false)
        chatAdapter.addMessage(chatMessage)
        scrollToBottom()
    }

    private fun updateOrAddBotMessage(messageId: String, message: String) {
        // Buscar si existe un mensaje con este messageId
        val existingMessageIndex = messages.indexOfFirst { 
            it.messageId == messageId && !it.isFromUser 
        }
        
        if (existingMessageIndex != -1) {
            // Reemplazar mensaje existente
            messages[existingMessageIndex] = ChatMessage(message, false, messageId)
            chatAdapter.notifyItemChanged(existingMessageIndex)
        } else {
            // Agregar nuevo mensaje con messageId
            val chatMessage = ChatMessage(message, false, messageId)
            chatAdapter.addMessage(chatMessage)
        }
        scrollToBottom()
    }

    private fun scrollToBottom() {
        recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
    }

    // ===== WEBSOCKET METHODS =====

    private fun connectToWebSocket() {
        if (isConnecting) {
            Log.d(TAG, "Ya hay una conexión en progreso, cancelando intento duplicado")
            return
        }
        
        try {
            isConnecting = true
            
            // Construir URL del WebSocket para pedidos
            val wsUrl = buildWebSocketUrl()
            Log.d(TAG, "Conectando a WebSocket (intento ${reconnectAttempts + 1}): $wsUrl")

            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "WebSocket conectado exitosamente")
                    isConnecting = false
                    
                    // Reset de valores de reconexión al conectar exitosamente
                    reconnectAttempts = 0
                    currentReconnectDelay = INITIAL_RECONNECT_DELAY_MS
                    cancelReconnectTimer()
                    
                    runOnUiThread {
                        addBotMessage("🟢 Conectado al servidor")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "Mensaje recibido del WebSocket: $text")
                    handleWebSocketMessage(text)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    Log.d(TAG, "Mensaje binario recibido (no esperado)")
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket cerrándose. Código: $code, Razón: $reason")
                    isConnecting = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket cerrado. Código: $code, Razón: $reason")
                    isConnecting = false
                    
                    runOnUiThread {
                        addBotMessage("🔴 Desconectado del servidor")
                    }
                    
                    // Intentar reconexión si es necesario
                    if (shouldReconnect) {
                        scheduleReconnect()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "Error en WebSocket", t)
                    isConnecting = false
                    
                    runOnUiThread {
                        addBotMessage("❌ Error de conexión: ${t.message}")
                    }
                    
                    // Intentar reconexión si es necesario
                    if (shouldReconnect) {
                        scheduleReconnect()
                    }
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando conexión WebSocket", e)
            isConnecting = false
            
            if (shouldReconnect) {
                scheduleReconnect()
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.w(TAG, "Máximo número de intentos de reconexión alcanzado ($MAX_RECONNECT_ATTEMPTS)")
            runOnUiThread {
                addBotMessage("⚠️ No se pudo reconectar después de $MAX_RECONNECT_ATTEMPTS intentos")
                addBotMessage("💡 Puedes intentar reiniciar la aplicación")
            }
            return
        }

        reconnectAttempts++
        
        Log.d(TAG, "Programando reconexión en ${currentReconnectDelay}ms (intento $reconnectAttempts/$MAX_RECONNECT_ATTEMPTS)")
        
        runOnUiThread {
            addBotMessage("🔄 Intentando reconectar en ${currentReconnectDelay / 1000} segundos... ($reconnectAttempts/$MAX_RECONNECT_ATTEMPTS)")
        }

        // Cancelar cualquier reconexión pendiente
        cancelReconnectTimer()

        // Programar nueva reconexión
        reconnectRunnable = Runnable {
            if (shouldReconnect && !isConnecting) {
                connectToWebSocket()
            }
        }
        
        reconnectHandler.postDelayed(reconnectRunnable!!, currentReconnectDelay)

        // Incrementar el delay para el próximo intento (backoff exponencial)
        currentReconnectDelay = (currentReconnectDelay * RECONNECT_MULTIPLIER).toLong()
            .coerceAtMost(MAX_RECONNECT_DELAY_MS)
    }

    private fun cancelReconnectTimer() {
        reconnectRunnable?.let { runnable ->
            reconnectHandler.removeCallbacks(runnable)
            reconnectRunnable = null
        }
    }

    private fun resetReconnectionState() {
        reconnectAttempts = 0
        currentReconnectDelay = INITIAL_RECONNECT_DELAY_MS
        cancelReconnectTimer()
    }

    /**
     * Fuerza una reconexión inmediata, útil para llamar desde la UI
     */
    private fun forceReconnect() {
        Log.d(TAG, "Forzando reconexión manual")
        
        // Cancelar reconexiones automáticas pendientes
        cancelReconnectTimer()
        
        // Cerrar conexión actual si existe
        webSocket?.close(1000, "Manual reconnect")
        
        // Reset del estado y reconectar
        resetReconnectionState()
        shouldReconnect = true
        
        runOnUiThread {
            addBotMessage("🔄 Reconectando manualmente...")
        }
        
        // Pequeño delay para permitir que se cierre la conexión anterior
        Handler(Looper.getMainLooper()).postDelayed({
            connectToWebSocket()
        }, 500)
    }

    /**
     * Pausa la reconexión automática
     */
    private fun pauseReconnection() {
        shouldReconnect = false
        cancelReconnectTimer()
        Log.d(TAG, "Reconexión automática pausada")
    }

    /**
     * Reanuda la reconexión automática
     */
    private fun resumeReconnection() {
        shouldReconnect = true
        Log.d(TAG, "Reconexión automática reanudada")
        // Si no está conectado, intentar conectar
        if (webSocket == null || !isWebSocketConnected()) {
            scheduleReconnect()
        }
    }

    /**
     * Verifica si el WebSocket está conectado
     */
    private fun isWebSocketConnected(): Boolean {
        // En OkHttp no hay un método directo para verificar el estado
        // pero podemos asumir que si webSocket no es null y no hemos recibido onClosed/onFailure reciente, está conectado
        return webSocket != null && !isConnecting && reconnectAttempts == 0
    }

    private fun buildWebSocketUrl(): String {
        // Convertir HTTP a WS y cambiar /api por /ws
        val baseUrl = serverUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .replace("/api", "/ws")
        
        // Construir URL del WebSocket de pedidos con información del camarero
        return "$baseUrl/pedidos/$deviceUID/?uid=$deviceUID&camarero_id=$camareroId"
    }

    private fun handleWebSocketMessage(messageText: String) = try {
        val jsonMessage = JSONObject(messageText)
        val type = jsonMessage.optString("type", "")
        val message = jsonMessage.optString("message", "")
        val sender = jsonMessage.optString("sender", "")
        val messageId = jsonMessage.optString("message_id", "")

        runOnUiThread {
            when (type) {
                "welcome" -> {
                    val deviceInfo = jsonMessage.optJSONObject("device_info")
                    addBotMessage("🎉 $message")
                }
                "message" -> {
                    if (sender == "bot" || sender == "status") {
                        if (messageId.isNotEmpty()) {
                            // Si tiene message_id, buscar y reemplazar mensaje existente
                            updateOrAddBotMessage(messageId, message)
                        } else {
                            // Sin message_id, agregar nuevo mensaje
                            addBotMessage(message)
                        }
                    }
                }
                "pedido_confirmation" -> {
                    val pedidoId = jsonMessage.optString("pedido_id", "")
                    jsonMessage.optString("status", "")
                    val total = jsonMessage.optDouble("total", 0.0)

                    addBotMessage("✅ $message")
                    if (total > 0) {
                        addBotMessage(buildString {
                            append("💰 Total: ")
                            append(String.format(Locale.getDefault(), "%.2f", total))
                            append("€")
                        })
                    }
                    if (pedidoId.isNotEmpty()) {
                        addBotMessage("📋 ID Pedido: $pedidoId")
                    }
                }
                "error" -> {
                    jsonMessage.optString("error_code", "")
                    val details = jsonMessage.optString("details", "")

                    addBotMessage("❌ Error: $message")
                    if (details.isNotEmpty()) {
                        addBotMessage("ℹ️ Detalles: $details")
                    }
                }
                "status" -> {
                    addBotMessage("ℹ️ $message")
                }
                else -> {
                    addBotMessage(message)
                }
            }
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error procesando mensaje WebSocket", e)
        runOnUiThread {
            addBotMessage("❌ Error procesando respuesta del servidor")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Deshabilitar reconexión automática
        shouldReconnect = false
        cancelReconnectTimer()
        
        // Detener grabación si está activa
        if (isRecording) {
            stopRecording()
        }
        
        // Cerrar conexión WebSocket
        webSocket?.close(1000, "Activity destroyed")
        
        Log.d(TAG, "Activity destruida, recursos liberados")
    }

    // ===== MÉTODOS AUXILIARES =====

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE)
        }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de audio requerido", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            audioFile = File(externalCacheDir, "audio_${System.currentTimeMillis()}.3gp")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFile?.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }

            isRecording = true
            buttonRecord.setImageResource(android.R.drawable.ic_media_pause)
            Toast.makeText(this, "Grabando audio...", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            Log.e(TAG, "Error al iniciar grabación", e)
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            buttonRecord.setImageResource(android.R.drawable.ic_btn_speak_now)

            addUserMessage("🎤 Mensaje de audio enviado")

            // Enviar audio al WebSocket
            sendAudioToWebSocket()

        } catch (e: Exception) {
            Log.e(TAG, "Error al detener grabación", e)
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendAudioToWebSocket() {
        try {
            // Verificar si hay conexión antes de enviar
            if (webSocket == null || !isWebSocketConnected()) {
                addBotMessage("⚠️ Sin conexión para enviar audio. Intentando reconectar...")
                if (shouldReconnect) {
                    forceReconnect()
                }
                return
            }

            audioFile?.let { file ->
                if (file.exists()) {
                    // Leer el archivo de audio y convertirlo a Base64
                    val audioBytes = file.readBytes()
                    val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                    
                    // Crear mensaje JSON para audio
                    val jsonMessage = JSONObject().apply {
                        put("type", "audio")
                        put("audio", audioBase64)
                        put("sender", "device")
                        put("camarero_id", camareroId)
                    }

                    val success = webSocket?.send(jsonMessage.toString()) ?: false
                    if (success) {
                        Log.d(TAG, "Audio enviado al WebSocket (${audioBytes.size} bytes)")
                    } else {
                        Log.w(TAG, "No se pudo enviar el audio, WebSocket no disponible")
                        addBotMessage("⚠️ Audio no enviado. Reintentando conexión...")
                        if (shouldReconnect) {
                            forceReconnect()
                        }
                    }
                    
                    // Limpiar archivo temporal
                    file.delete()
                } else {
                    Log.e(TAG, "Archivo de audio no existe")
                    addBotMessage("❌ Error: No se pudo leer el archivo de audio")
                }
            } ?: run {
                Log.e(TAG, "audioFile es null")
                addBotMessage("❌ Error: No se grabó audio")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error enviando audio al WebSocket", e)
            addBotMessage("❌ Error enviando audio: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de audio concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

