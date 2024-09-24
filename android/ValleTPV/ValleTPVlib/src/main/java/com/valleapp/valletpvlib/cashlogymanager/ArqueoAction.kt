package com.valleapp.valletpvlib.cashlogymanager

import android.os.Handler
import android.os.Looper
import com.valleapp.valletpvlib.tools.round

class ArqueoAction(socketManager: CashlogySocketManager,
                   uiHandler: Handler):
    CashlogyAction(socketManager, uiHandler) {

    private val denominacionesDisponibles: MutableMap<Int, Int> = HashMap()
    private var totalRecicladores = 0.0
    private var totalAlmacenes = 0.0
    private var cambio = 0.0

    var handler: Handler = Handler(Looper.getMainLooper()) { msg ->
        val key = msg.data.getString("key")
        if (key == null  || key != "CASHLOGY_RESPONSE") return@Handler true

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
        // Envía el comando para obtener todas las denominaciones en Cashlogy
        socketManager.sendCommand("#Y#", handler)
    }


    private fun procesarRespuestaCambios() {
        if (totalRecicladores > cambio) {
            var excessAmount = totalRecicladores - cambio
            val uCommandBuilder = StringBuilder("#U#;")
            var hasDenominations = false

            if (excessAmount >= 20) {
                val num20s = (excessAmount / 20).toInt()
                if (num20s > 0) {
                    uCommandBuilder.append("2000:").append(num20s).append(",")
                    totalAlmacenes += num20s * 20
                    totalRecicladores -= num20s * 20
                    excessAmount -= num20s * 20
                    hasDenominations = true
                }
            }

            if (excessAmount >= 10) {
                val num10s = (excessAmount / 10).toInt()
                if (num10s > 0) {
                    uCommandBuilder.append("1000:").append(num10s).append(",")
                    totalAlmacenes += num10s * 10
                    totalRecicladores -= num10s * 10
                    excessAmount -= num10s * 10
                    hasDenominations = true
                }
            }

            if (excessAmount >= 5) {
                val num5s = (excessAmount / 5).toInt()
                if (num5s > 0) {
                    uCommandBuilder.append("500:").append(num5s).append(",")
                    totalAlmacenes += num5s * 5
                    totalRecicladores -= num5s * 5
                    excessAmount -= num5s * 5
                    hasDenominations = true
                }
            }

            if (hasDenominations) {
                if (uCommandBuilder[uCommandBuilder.length - 1] == ',') {
                    uCommandBuilder.deleteCharAt(uCommandBuilder.length - 1)
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
            val parts = response.split("#").toTypedArray()

            if (parts.size < 3) {
                notifyUI("CASHLOGY_ERR", "Respuesta de denominaciones no válida.")
                return
            }

            val recicladoresParte = parts[2]
            val almacenesParte = parts[3]
            denominacionesDisponibles.clear()
            totalAlmacenes = 0.0
            totalRecicladores = 0.0

            val seccionesRecicladores = recicladoresParte.split(";").toTypedArray()
            val recicladoresMonedas = seccionesRecicladores[0].split(",").toTypedArray()
            val recicladoresBilletes = seccionesRecicladores[1].split(",").toTypedArray()

            for (reciclador in recicladoresMonedas) {
                val denomination = reciclador.split(":").toTypedArray()
                if (denomination.size == 2) {
                    val valorEnCentimos = denomination[0].toInt()
                    val cantidad = denomination[1].toInt()
                    denominacionesDisponibles[valorEnCentimos] = cantidad
                    totalRecicladores += (valorEnCentimos * cantidad / 100.0).round(2)
                }
            }

            for (reciclador in recicladoresBilletes) {
                val denomination = reciclador.split(":").toTypedArray()
                if (denomination.size == 2) {
                    val valorEnCentimos = denomination[0].toInt()
                    val cantidad = denomination[1].toInt()
                    denominacionesDisponibles[valorEnCentimos] = cantidad
                    totalRecicladores += (valorEnCentimos * cantidad / 100.0).round(2)
                    if (valorEnCentimos > 2000) break
                }
            }

            val seccionesAlmacenes = almacenesParte.split(";").toTypedArray()
            val almacenesBilletes = seccionesAlmacenes[1].split(",").toTypedArray()

            for (almacen in almacenesBilletes) {
                val denomination = almacen.split(":").toTypedArray()
                if (denomination.size == 2) {
                    val valorEnCentimos = denomination[0].toInt()
                    val cantidad = denomination[1].toInt()
                    totalAlmacenes += (valorEnCentimos * cantidad / 100.0).round(2)
                }
            }

            notifyUI("CASHLOGY_DENOMINACIONES_LISTAS", "Las denominaciones están listas para su uso.")

        } catch (e: Exception) {
            notifyUI("CASHLOGY_ERR", "Error al manejar la respuesta de denominaciones.")
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

    fun getTotalRecicladores(): Double {
        return totalRecicladores
    }

    fun getTotalAlmacenes(): Double {
        return totalAlmacenes
    }

    fun cashLogyCerrado() {
        notifyUI("CASHLOGY_CIERRE_COMPLETADO", "")
    }
}
