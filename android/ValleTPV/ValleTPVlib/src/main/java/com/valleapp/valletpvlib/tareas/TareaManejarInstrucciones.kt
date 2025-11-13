package com.valleapp.valletpvlib.tareas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.tools.Instruccion
import java.util.Queue
import kotlin.random.Random

class TareaManejarInstrucciones(
    private val cola: Queue<Instruccion>,
    private var uid: String
) {

    private val handler = Handler(Looper.getMainLooper())
    private var procesando = false
    private var esperandoReintento = false
    private var tiempoReintento = 10000L // 10 segundos iniciales
    private val TIMEOUT_RESPONSE = 40000L // 40 segundos para esperar respuesta de HTTPRequest

    fun iniciar() {
        if (!procesando && !esperandoReintento) {
            procesarSiguiente()
        }
    }

    private val handleHttp = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            procesando = false // Liberamos el procesamiento al recibir mensaje

            try {
                val op = msg.data?.getString("op") ?: "unknown_op" // Evitamos NPE
                val res = msg.data?.getString("RESPONSE")
                val codigoEstado = msg.data?.getInt("codigoEstado") ?: -1
                val connectionStatus = msg.data?.getString("connectionStatus")

                when (connectionStatus) {
                    "server_unreachable" -> {
                        // Caso 1: Fallo de conexión, reintentamos indefinidamente
                        Log.w("TareaInstrucciones", "No se pudo conectar ($op), reintentando...")
                        esperarReintento()
                    }
                    "server_response" -> {
                        // El servidor respondió (éxito o error), avanzamos
                        synchronized(cola) {
                            val inst = cola.poll() // Eliminamos la instrucción
                            inst?.handler?.let { handlerInst ->
                                val msgInst = handlerInst.obtainMessage()
                                val bundle = msg.data ?: Bundle()
                                bundle.putString("RESPONSE", res)
                                bundle.putString("op", inst.op)
                                msgInst.data = bundle
                                handlerInst.sendMessage(msgInst)
                            }
                        }
                        if (codigoEstado >= 400) {
                            Log.e("TareaInstrucciones", "Error del servidor ($op): Código $codigoEstado, Respuesta: $res")
                        }
                        tiempoReintento = 10000L
                        procesarSiguiente()
                    }
                    else -> {
                        // Caso 4: Estado desconocido, descartamos y avanzamos
                        Log.e("TareaInstrucciones", "Estado de conexión inválido ($op): $connectionStatus")
                        synchronized(cola) { cola.poll() }
                        procesarSiguiente()
                    }
                }
            } catch (e: Exception) {
                // Caso 3: Excepción al procesar, descartamos y avanzamos
                val op = msg.data?.getString("op") ?: "unknown_op"
                Log.e("TareaInstrucciones", "Error al procesar mensaje ($op): ${e.message}")
                synchronized(cola) { cola.poll() }
                procesarSiguiente()
            }
        }
    }

    private fun procesarSiguiente() {
        synchronized(cola) {
            if (cola.isNotEmpty()) {
                procesando = true // Marcamos como procesando
                val inst = cola.peek() // Miramos la instrucción sin eliminarla
                inst?.let {
                    it.params.put("uid", uid)
                    println("Enviando instrucción: ${it.op} con params: ${it.params}, url: ${it.url}")

                    HTTPRequest(it.url, it.params, it.op, handleHttp) // Enviamos la solicitud

                    // Caso 2: Timeout si HTTPRequest no responde
                    handler.postDelayed({
                        if (procesando) { // Si aún no hemos recibido respuesta
                            Log.w("TareaInstrucciones", "Timeout esperando respuesta ($it.op), reintentando...")
                            procesando = false
                            esperarReintento() // Reintentamos como fallo de conexión
                        }
                    }, TIMEOUT_RESPONSE)
                }
            }
        }
    }

    private fun esperarReintento() {
        esperandoReintento = true
        val jitter = Random.nextLong(-1000, 1000) // Jitter para evitar colisiones
        handler.postDelayed({
            esperandoReintento = false
            tiempoReintento = (tiempoReintento * 2).coerceAtMost(60000) + jitter // Backoff exponencial, máximo 1 minuto
            procesarSiguiente()
        }, tiempoReintento)
        Log.w("TareaInstrucciones", "Reintentando, espera ${tiempoReintento / 1000} segundos")
    }

    fun setUid(uid: String?) {
        this.uid = uid ?: ""
    }
}