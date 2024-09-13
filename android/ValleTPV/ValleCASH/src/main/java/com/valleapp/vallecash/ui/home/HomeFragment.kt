package com.valleapp.vallecash.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.valleapp.vallecash.R
import com.valleapp.vallecash.WebSocketService
import com.valleapp.vallecash.databinding.FragmentHomeBinding
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private var isReceiverRegistered = false // Flag para verificar si el receptor está registrado


    private val webSocketReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Obtener las instrucciones y el mensaje del Intent
            val instruction = intent.getStringExtra("instruccion")
            val message = intent.getStringExtra("message")

            // Asegurarse de que el mensaje no sea nulo
            message?.let {
                // Procesar las instrucciones recibidas
                if (instruction != null) {
                    when {
                        instruction.contains("#T#") -> {
                            // Procesar la cadena con formato #a#b#c#
                            procesarTCadena(it)
                        }

                    }
                }
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navegar de vuelta al HomeFragment usando NavController
        val navController = findNavController()

        // Inicializar el ViewModel correctamente dentro de onViewCreated
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Registrar el receptor solo una vez y marcarlo como registrado
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(webSocketReceiver, IntentFilter("WebSocketMessage"))
            isReceiverRegistered = true
        }
        // Obtener referencias a los TextViews
        val textViewTotalDispensado = view.findViewById<TextView>(R.id.text_total_cambio)
        val textViewTotalAlmacen = view.findViewById<TextView>(R.id.text_total_almacen)
        val btnModificarCambio = view.findViewById<ImageButton>(R.id.button_modificar_cambio)
        val btnDispensarCambio = view.findViewById<ImageButton>(R.id.button_dispense_cambio)

        // Observar el total dispensado y actualizar la UI
        viewModel.totalDispensado.observe(viewLifecycleOwner) { total ->
            // Actualizar UI, por ejemplo:
            textViewTotalDispensado.text = String.format(Locale.getDefault(),"Total Almacén: %.2f €", total)
        }

        // Observar el total del almacén y actualizar la UI
        viewModel.totalAlmacen.observe(viewLifecycleOwner) { total ->
            // Actualizar UI, por ejemplo:
            textViewTotalAlmacen.text = String.format(Locale.getDefault(),"Total Almacén: %.2f €", total)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            sendInstructionToWebSocket("#T#")
        }, 1000) // Espera 1 segundo antes de enviar la instrucción

        btnDispensarCambio.setOnClickListener {
            // Navegar al Fragmento de "Dispensar Cambio"
            navController.navigate(R.id.nav_dispense_coins)
        }

        btnModificarCambio.setOnClickListener {
            // Navegar al Fragmento de "Modificar Cambio"
            navController.navigate(R.id.nav_modified_coins)
        }
    }

    // Método para enviar la instrucción al WebSocketService
    private fun sendInstructionToWebSocket(instruction: String) {
        // Crea un Intent para enviar la instrucción al WebSocketService
        val intent = Intent(requireContext(), WebSocketService::class.java)
        intent.putExtra("action", "SEND_INSTRUCTION") // Acción que el servicio va a manejar
        intent.putExtra("instruction", instruction) // Instrucción que se va a enviar
        requireContext().startService(intent) // Enviar la instrucción al servicio
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    private fun procesarTCadena(message: String) {
        // Dividir la cadena por "#" para extraer los elementos a, b, c
        val parts = message.split("#")

        if (parts.size >= 4) {
            val errorCode = parts[1]  // a: Código de error (puedes manejar esto según sea necesario)
            if (errorCode.startsWith("#ER")) return
            val totalRecicladores = parts[2].toDoubleOrNull() // b: Total en los recicladores
            val totalAlmacen = parts[3].toDoubleOrNull() // c: Total en el almacén (stacker)

            // Verificar que los valores no sean nulos antes de actualizar el ViewModel
            totalRecicladores?.let {
                viewModel.setTotalDispensado(it/100) // Actualiza total dispensado
            }

            totalAlmacen?.let {
                viewModel.setTotalAlmacen(it/100) // Actualiza total en almacén
            }
        } else {
            Log.e("WebSocketReceiver", "Formato de cadena inválido: $message")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Solo anular el registro si el receptor está registrado
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(webSocketReceiver)
            isReceiverRegistered = false
        }
        _binding = null
    }
}