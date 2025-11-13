package com.valleapp.vallecash.ui.mantenimiento

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.valleapp.vallecash.tools.WebSocketService
import com.valleapp.valletpv.R

class MantenimientoFragment : Fragment() {

    private lateinit var tvRespuesta: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mantenimiento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnConsultar = view.findViewById<Button>(R.id.btnConsultarBilletes)
        val btnResetear = view.findViewById<Button>(R.id.btnResetearContador)
        val btnCorreas = view.findViewById<Button>(R.id.btnRegistrarCorreas)
        tvRespuesta = view.findViewById(R.id.tvRespuestaCashlogy)

        btnConsultar.setOnClickListener {
            enviarComando("#W#0#")
        }

        btnResetear.setOnClickListener {
            enviarComando("#W#1#")
        }

        btnCorreas.setOnClickListener {
            enviarComando("#W#2#")
        }
    }

    private val respuestaReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            intent?.let {
                val mensaje = it.getStringExtra("message") ?: "Sin respuesta"
                val comandoEnviado = it.getStringExtra("instruccion") ?: ""
                val mensajeTraducido = traducirMensaje(mensaje, comandoEnviado)
                tvRespuesta.text = mensajeTraducido
            }
        }
    }

    private fun enviarComando(comando: String) {
        val intent = Intent(requireContext(), WebSocketService::class.java).apply {
            putExtra("action", "SEND_INSTRUCTION")
            putExtra("instruction", comando)
        }
        requireActivity().startService(intent)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(respuestaReceiver, IntentFilter("WebSocketMessage"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(respuestaReceiver)
    }

    private fun traducirMensaje(mensaje: String, comandoEnviado: String): String {
        val partes = mensaje.split("#").filter { it.isNotEmpty() }
        var resultado = ""

        if (mensaje.contains("WR:")) {
            val aviso = mensaje.substringAfter("WR:").substringBefore("#")
            resultado += when (aviso) {
                "LEVEL" -> "Aviso: Nivel bajo de billetes o monedas. \n\n"
                "CANCEL" -> "Aviso: Operación cancelada. \n" +
                        "\n"
                else -> "Aviso desconocido: $aviso. \n" +
                        "\n"
            }
        }

        if (mensaje.contains("ER:")) {
            val error = mensaje.substringAfter("ER:").substringBefore("#")
            resultado += when (error) {
                "GENERIC" -> "Error: Error genérico. \n" +
                        "\n"
                "BAD_DATA" -> "Error: Comando erróneo. \n" +
                        "\n"
                "BUSY" -> "Error: Dispositivo ocupado. \n" +
                        "\n"
                "ILLEGAL" -> "Error: Comando no permitido en este estado. \n" +
                        "\n"
                else -> "Error desconocido: $error. \n" +
                        "\n"
            }
        }

        if (mensaje.contains("#0#")) {
            resultado += "OK: Operación completada con éxito. \n" +
                    "\n"
        }

        val infoExtra = partes.lastOrNull()?.trim()
        if (comandoEnviado == "#W#0#" && infoExtra?.toIntOrNull() != null) {
            resultado += "Billetes restantes hasta mantenimiento: $infoExtra."
        } else if (comandoEnviado == "#W#1#") {
            if (!resultado.contains("Error"))
                resultado += "Contador de ciclos reseteado."
        } else if (comandoEnviado == "#W#2#") {
            if (!resultado.contains("Error"))
                resultado += "Registro de correas completado."
        }

        return resultado.trim()
    }
}