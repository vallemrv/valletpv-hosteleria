package com.valleapp.vallecash.tools

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.content.Context

class CommandSender(
        private val context: Context,
        private val repeatInterval: Long,  // Intervalo de repetición en milisegundos
        private val command: String  // Comando que se va a enviar
) {

    private val handler = Handler(Looper.getMainLooper())

    // Runnable que enviará el comando cada "repeatInterval" milisegundos
    private val sendCommandRunnable = object : Runnable {
        override fun run() {
            sendInstructionToWebSocket(command)  // Enviar la instrucción
            handler.postDelayed(this, repeatInterval)  // Repetir según el intervalo definido
        }
    }

    // Método para iniciar el envío periódico del comando
    fun startSendingCommands() {
        handler.post(sendCommandRunnable)  // Iniciar la ejecución del Runnable
    }

    // Método para detener el envío periódico
    fun stopSendingCommands() {
        handler.removeCallbacks(sendCommandRunnable)  // Detener la ejecución del Runnable
    }

    // Método para enviar la instrucción al WebSocketService
     fun sendInstructionToWebSocket(instruction: String) {
        // Crea un Intent para enviar la instrucción al WebSocketService
        val intent = Intent(context, WebSocketService::class.java)
        intent.putExtra("action", "SEND_INSTRUCTION")  // Acción que el servicio va a manejar
        intent.putExtra("instruction", instruction)  // Instrucción que se va a enviar
        context.startService(intent)  // Enviar la instrucción al servicio
    }


}
