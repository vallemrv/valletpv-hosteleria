package com.valleapp.valletpvlib.cashlogymanager

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.valleapp.valletpvlib.tools.round

class ArqueoAction(socketManager: CashlogySocketManager, uiHandler: Handler) :
    CashlogyAction(socketManager, uiHandler) {

    private val denominacionesDisponibles: MutableMap<Int, Int> = HashMap()
    var totalRecicladores = 0.0
    var totalAlmacenes = 0.0
    private var cambio = 0.0

    var handler: Handler = Handler(Looper.getMainLooper()) { msg ->
        val key = msg.data.getString("key")
        if (key == null || key != "CASHLOGY_RESPONSE") return@Handler true

        val comando = msg.data.getString("instruccion")
        val response = msg.data.getString("value")

        if (comando != null && response != null) {
            when {
                comando.startsWith("#Y") -> manejarRespuestaDenominaciones(response)
                comando.startsWith("#U") -> notifyUI("CASHLOGY_CASH", "")
            }
        }
        true
    }

    override fun execute() {
        socketManager.sendCommand("#Y#", handler)
    }

    private fun procesarRespuestaCambios() {
        if (totalRecicladores > cambio) {
            var excessAmount = totalRecicladores - cambio
            val uCommandBuilder = StringBuilder("#U#;")
            var hasDenominations = false

            val denominationsToMove = listOf(20, 10, 5)

            denominationsToMove.forEach { denomination ->
                if (excessAmount >= denomination) {
                    val numDenominations = (excessAmount / denomination).toInt()
                    if (numDenominations > 0) {
                        uCommandBuilder.append("${denomination * 100}:$numDenominations,")
                        totalAlmacenes += numDenominations * denomination
                        totalRecicladores -= numDenominations * denomination
                        excessAmount -= numDenominations * denomination
                        hasDenominations = true
                    }
                }
            }

            if (hasDenominations) {
                if (uCommandBuilder.last() == ',') {
                    uCommandBuilder.deleteCharAt(uCommandBuilder.lastIndex)
                }
                uCommandBuilder.append("#1#0#0#")
                socketManager.sendCommand(uCommandBuilder.toString(), handler)
            } else {
                notifyUI("CASHLOGY_CASH", "")
            }
        } else {
            notifyUI("CASHLOGY_CASH", "")
        }
    }

    private fun manejarRespuestaDenominaciones(response: String) {
        try {
            val parts = response.split("#")

            if (parts.size < 3) {
                notifyUI("CASHLOGY_ERR", "Respuesta de denominaciones no válida: $response")
                Log.e("ArqueoAction", "Respuesta de denominaciones no válida: $response")
                return
            }

            val recicladoresParte = parts[2]
            val almacenesParte = parts[3]
            denominacionesDisponibles.clear()
            totalAlmacenes = 0.0
            totalRecicladores = 0.0

            parseDenominations(recicladoresParte, isRecycler = true)
            parseDenominations(almacenesParte, isRecycler = false)

            notifyUI("CASHLOGY_DENOMINACIONES_LISTAS", "Las denominaciones están listas para su uso.")

        } catch (e: Exception) {
            notifyUI("CASHLOGY_ERR", "Error al manejar la respuesta de denominaciones: ${e.message}")
            Log.e("ArqueoAction", "Error al manejar la respuesta de denominaciones: ${e.message}", e)
        }
    }

    private fun parseDenominations(part: String, isRecycler: Boolean) {
        val secciones = part.split(";")
        val monedas = secciones.getOrNull(0)?.split(",") ?: emptyList()
        val billetes = secciones.getOrNull(1)?.split(",") ?: emptyList()

        (monedas + billetes).forEach { denomination ->
            val parts = denomination.split(":")
            if (parts.size == 2) {
                val valorEnCentimos = parts[0].toInt()
                val cantidad = parts[1].toInt()
                if (isRecycler) {
                    denominacionesDisponibles[valorEnCentimos] = cantidad
                    totalRecicladores += (valorEnCentimos * cantidad / 100.0).round(2)
                    if (valorEnCentimos > 2000 && billetes.contains(denomination)) return@forEach
                } else {
                    totalAlmacenes += (valorEnCentimos * cantidad / 100.0).round(2)
                }
            }
        }
    }

    fun getDenominaciones(): MutableMap<Int, Int> {
        return denominacionesDisponibles
    }

    fun cerrarCashlogy() {
        procesarRespuestaCambios()
    }

    @Synchronized
    fun setFondoCaja(cambio: Double) {
        this.cambio = cambio
    }

    fun cashLogyCerrado() {
        notifyUI("CASHLOGY_CIERRE_COMPLETADO", "")
    }
}