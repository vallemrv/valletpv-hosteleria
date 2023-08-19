package com.valleapp.valletpvlib.tools.tareas

import com.valleapp.valletpvlib.tools.ApiErrorMessages
import com.valleapp.valletpvlib.tools.ApiRequest
import com.valleapp.valletpvlib.tools.ApiResponse
import com.valleapp.valletpvlib.tools.Instrucciones
import com.valleapp.valletpvlib.tools.safeApiCall
import kotlinx.coroutines.sync.Mutex
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class InstruccionesManager {

    private val mutex = Mutex(false)
    private val cola: Queue<Instrucciones> = ConcurrentLinkedQueue()

    @Volatile
    private var isRunning = true


    suspend fun procesarCola() {
        while (isRunning) {
            println("procesando instrucciones ${cola.size}")
            if (cola.isNotEmpty()) {
                val inst = cola.peek()
                inst?.let {
                    val result = safeApiCall {
                        ApiRequest.service.post(inst.endPoint, inst.params)
                    }
                    when (result) {
                        is ApiResponse.Success -> {
                            cola.poll()
                        }

                        is ApiResponse.Error -> {
                            if (!((result.errorMessage == ApiErrorMessages.TIMEOUT) ||
                                        (result.errorMessage == ApiErrorMessages.NO_CONNECTION))
                            ) {
                                cola.poll()
                            }
                            inst.mensaje?.let {
                                it.mensaje = result.errorMessage.toString()
                                it.tipo = "error"
                            }
                        }
                    }

                } ?: run {
                    cola.poll()
                }

            } else {
                mutex.lock()
            }

        }
    }

    fun stopProcesarCola() {
        isRunning = false
        mutex.unlock()
    }

    fun addInstruccion(instruccion: Instrucciones) {
        cola.add(instruccion)
        mutex.unlock()
        println("Agregando   instruccion ${cola.size}, Estado del mutex: ${mutex.isLocked}")
    }
}
