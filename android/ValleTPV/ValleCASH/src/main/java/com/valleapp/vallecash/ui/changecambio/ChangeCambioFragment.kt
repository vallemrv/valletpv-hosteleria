package com.valleapp.vallecash.ui.changecambio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.valleapp.vallecash.R
import com.valleapp.vallecash.WebSocketService
import com.valleapp.vallecash.databinding.FragmentChangeCambioBinding
import java.util.Locale

class ChangeCambioFragment : Fragment() {

    private var _binding: FragmentChangeCambioBinding? = null
    private val binding get() = _binding!!
    private var serverURL = ""

    private lateinit var viewModel: ChangeCambioViewModel
    private var isAcceptingChange = false
    private val handler = Handler(Looper.getMainLooper())
    private val queryRunnable = object : Runnable {
        override fun run() {
            sendInstructionToWebSocket("#Q#")
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChangeCambioBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChangeCambioViewModel::class.java)

        serverURL = arguments?.getString("server").toString()

        // Configurar los observadores de LiveData
        setupObservers()

        // Configurar los listeners de los botones
        setupListeners()

        // Registrar el BroadcastReceiver
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(webSocketReceiver, IntentFilter("WebSocketMessage"))

        // Obtener los totales del servidor
        viewModel.getTotales(serverURL)

        // Enviar instrucción #T# después de 1 segundo
        Handler(Looper.getMainLooper()).postDelayed({
            sendInstructionToWebSocket("#T#")
        }, 1000)

        return binding.root
    }

    private fun setupObservers() {
        viewModel.cambio.observe(viewLifecycleOwner) { cambio ->
            binding.textTotalCambio.text = String.format(Locale.getDefault(),
                                    "Total Cambio: %.2f €", cambio / 100)
        }

        viewModel.totalAlmacenes.observe(viewLifecycleOwner) { almacenes ->
            binding.textTotalAlmacen.text = String.format(Locale.getDefault(),
                  "Total Almacén: %.2f €", almacenes / 100)
        }

        viewModel.totalAdmitido.observe(viewLifecycleOwner) { totalAdmitido ->
            binding.textTotalCambio.text = String.format(Locale.getDefault(),
                                    "Total Admitido: %.2f €", totalAdmitido / 100)
        }

        viewModel.esActualizado.observe(viewLifecycleOwner) { actualizado ->
            if (actualizado) {
               Log.d("ChangeCambioFragment", "Totales actualizados")
                var cambio = viewModel.cambio.value ?: 0.0
                var almacenes = viewModel.totalAlmacenes.value ?: 0.0
                var totalAdmitido = viewModel.totalAdmitido.value ?: 0.0

            }
        }
    }

    private fun setupListeners() {
        binding.buttonDispenseCambio.setOnClickListener {
            if (!isAcceptingChange) {
                // Iniciar admisión de cambio
                sendInstructionToWebSocket("#A#")
                isAcceptingChange = true
                // Cambiar el ícono a una 'X' de cancelar
                binding.buttonDispenseCambio.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.cancelar)
                )
                // Iniciar envío de #Q# cada 200ms
                handler.post(queryRunnable)
            } else {
                // Finalizar admisión de cambio
                sendInstructionToWebSocket("#J#")
                isAcceptingChange = false
                // Restaurar el ícono original
                binding.buttonDispenseCambio.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.insesrtar_cambio)
                )
                // Detener el envío de #Q#
                handler.removeCallbacks(queryRunnable)
                // Actualizar los totales en el servidor
                viewModel.updateTotales(serverURL)
            }
        }

        // Configurar el listener para button_modificar_cambio si es necesario
    }

    // Método para enviar la instrucción al WebSocketService
    private fun sendInstructionToWebSocket(instruction: String) {
        val intent = Intent(requireContext(), WebSocketService::class.java)
        intent.putExtra("action", "SEND_INSTRUCTION")
        intent.putExtra("instruction", instruction)
        requireContext().startService(intent)
    }

    // BroadcastReceiver para recibir las respuestas del WebSocketService
    private val webSocketReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val instruction = intent.getStringExtra("instruccion")
            val message = intent.getStringExtra("message")

            message?.let {
                when {
                    instruction?.contains("#T#") == true -> {
                        procesarTCadena(it)
                    }
                    instruction?.contains("#Q#") == true -> {
                        procesarQCadena(it)
                    }
                    instruction?.contains("#A#") == true -> {
                        // Procesar respuesta de #A# si es necesario
                    }
                    instruction?.contains("#J#") == true -> {
                        procesarJCadena(it)
                    }
                    // Añadir más casos según sea necesario
                }
            }
        }
    }

    private fun procesarTCadena(cadena: String) {
        // Suponiendo que la cadena es del formato #a#b#c#
        val partes = cadena.split("#")
        if (partes.size >= 4) {
            val a = partes[1] // Código de error
            val b = partes[2] // Total Cambio
            val c = partes[3] // Total Almacén

            // Actualizar los LiveData en el ViewModel
            viewModel.cambio.postValue(b.toDoubleOrNull()  ?: 0.0)
            viewModel.totalAlmacenes.postValue(c.toDoubleOrNull() ?: 0.0)
        }
    }

    private fun procesarQCadena(cadena: String) {
        // Suponiendo que la cadena es del formato #a#b#
        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val a = partes[1] // Código de error
            val b = partes[2] // Importe recibido

            // Actualizar el total admitido en el ViewModel
            viewModel.totalAdmitido.postValue(b.toDoubleOrNull() ?: 0.0)
        }
    }

    private fun procesarJCadena(cadena: String) {
        // Procesar la respuesta del comando #J#
        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val a = partes[1] // Código de error
            val b = partes[2] // Importe cobrado

            // Actualizar el total admitido y otros estados si es necesario
            viewModel.totalAdmitido.postValue(0.0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Desregistrar el BroadcastReceiver cuando se destruya la vista
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(webSocketReceiver)
        _binding = null
        handler.removeCallbacks(queryRunnable)
    }
}
