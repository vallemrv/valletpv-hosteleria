package com.valleapp.valletpvlib.cashlogymanager

import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class CashlogySocketManager(private val cashlogyUrl: String) {

    private val cashlogyPort = 8092
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: OutputStream? = null
    private val instructionQueue: BlockingQueue<Instruction> = LinkedBlockingQueue()
    private var isProcesando = false
    @Volatile private var running = true

    private data class Instruction(val command: String, val handler: Handler)

    init {
        // Iniciar el hilo de procesamiento
        Thread(InstructionProcessor()).start()
    }

    fun start() {
        Thread { initializeSocket() }.start()
    }

    private fun initializeSocket() {
        try {
            socket = Socket(cashlogyUrl, cashlogyPort)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            writer = socket!!.getOutputStream()
            Log.i("CASHLOGY", "Socket conectado exitosamente")
        } catch (e: Exception) {
            Log.e("CASHLOGY", "Error al inicializar Cashlogy: ${e.message}")
        }
    }

    // Hilo que procesa la cola de instrucciones
    private inner class InstructionProcessor : Runnable {
        override fun run() {
            while (running) {
                try {
                    // Esperar hasta que haya una instrucción en la cola (bloquea si está vacía)
                    if(isProcesando) continue
                    val instruction = instructionQueue.take()

                    processCommand(instruction.command, instruction.handler)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    Log.e("CASHLOGY", "Thread interrumpido: ${e.message}")
                }
            }
        }
    }

    // Método para encolar la instrucción
    fun sendCommand(command: String, handler: Handler) {
        try {
            if (command == "#!#"){
                processCommand(command, handler)
                return
            }
            instructionQueue.put(Instruction(command, handler))

        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Log.e("CASHLOGY", "Error al agregar instrucción a la cola: ${e.message}")
        }
    }

    // Método que realmente procesa el comando y maneja la respuesta
    private fun processCommand(command: String, handler: Handler) {
        if (!isConnected()) {
            Log.e("CASHLOGY", "El socket no está conectado, esperando...")
            waitForConnectionAndSend(command, handler)
            return
        }
        println("Enviando comando: $command")
        try {
            this.isProcesando = true
            writer!!.write("$command\r\n".toByteArray())
            writer!!.flush()

            val buffer = CharArray(4096)
            val charsRead = reader!!.read(buffer)

            if (charsRead != -1) {
                val response = String(buffer, 0, charsRead)
                val msg = handler.obtainMessage().apply {
                    data = Bundle().apply {
                        putString("key", "CASHLOGY_RESPONSE")
                        putString("value", response)
                        putString("instruccion", command)
                    }
                }
                handler.sendMessage(msg)
                this.isProcesando = false
            } else {
                Log.e("CASHLOGY", "No se pudo leer la respuesta o el socket está cerrado.")
            }

        } catch (e: Exception) {
            Log.e("CASHLOGY", "Error al enviar comando: ${e.message}", e)
        }
    }

    private fun waitForConnectionAndSend(command: String, handler: Handler) {
        Thread {
            var attempts = 5
            while (attempts > 0 && !isConnected()) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Log.e("CASHLOGY", "Error al esperar: ${e.message}")
                }
                attempts--
            }

            if (isConnected()) {
                sendCommand(command, handler)
            } else {
                Log.e("CASHLOGY", "No se pudo establecer la conexión después de varios intentos.")
            }
        }.start()
    }

    private fun isConnected(): Boolean {
        return socket?.isConnected == true && socket?.isClosed == false
    }

    fun stop() {
        try {
            running = false
            socket?.close()
        } catch (e: Exception) {
            Log.e("CASHLOGY", "Error al cerrar CashlogySocketManager: ${e.message}")
        }
        Log.i("CASHLOGY", "Servidor cerrado correctamente.")
    }
}
