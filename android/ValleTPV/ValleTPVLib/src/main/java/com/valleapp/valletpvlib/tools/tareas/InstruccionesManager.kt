package com.valleapp.valletpvlib.tools.tareas

import com.valleapp.valletpvlib.interfaces.IServiceState
import com.valleapp.valletpvlib.tools.ApiErrorMessages
import com.valleapp.valletpvlib.tools.ApiRequest
import com.valleapp.valletpvlib.tools.ApiResponse
import com.valleapp.valletpvlib.tools.Instrucciones
import com.valleapp.valletpvlib.tools.safeApiCall
import kotlinx.coroutines.sync.Mutex
import java.lang.Thread.sleep
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class InstruccionesManager {

    private val mutex = Mutex(false)
    private val cola: Queue<Instrucciones> = ConcurrentLinkedQueue()

    @Volatile
    private var isRunning = true


    suspend fun procesarCola(controller: IServiceState) {
        isRunning = true
        while (isRunning) {

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
                            if (result.errorMessage == ApiErrorMessages.UNAUTHORIZED){
                                this.stopProcesarCola()
                                controller.invalidateAuth()

                            }else if (!((result.errorMessage == ApiErrorMessages.TIMEOUT)
                                        || (result.errorMessage == ApiErrorMessages.NO_CONNECTION))) {
                                cola.poll()


                            }else{
                                sleep(5000)
                            }

                        }
                    }

                }

            } else {
                mutex.lock()
            }

        }
    }

    fun stopProcesarCola() {
        isRunning = false
        if (mutex.isLocked)  mutex.unlock()
    }

     fun addInstruccion(instruccion: Instrucciones) {
        cola.add(instruccion)
        if (mutex.isLocked) mutex.unlock()
        println("Agregando   instruccion ${cola.size}")
    }

}
