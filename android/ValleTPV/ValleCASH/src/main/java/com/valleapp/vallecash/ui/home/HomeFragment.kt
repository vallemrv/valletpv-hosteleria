package com.valleapp.vallecash.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.valleapp.vallecash.ValleCASH
import com.valleapp.vallecash.tools.CommandSender
import com.valleapp.valletpv.R
import com.valleapp.valletpv.databinding.FragmentHomeBinding
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private var isReceiverRegistered = false
    private lateinit var commandSender: CommandSender



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
                        instruction.contains("sincronizar") -> {
                            commandSender.sendInstructionToWebSocket("#T#")
                        }

                    }
                }
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el ViewModel correctamente dentro de onViewCreated
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        commandSender = CommandSender(requireContext(), 10000, "#T#")
        // Registrar el receptor solo una vez y marcarlo como registrado
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(webSocketReceiver, IntentFilter("WebSocketMessage"))
            isReceiverRegistered = true
        }
        // Obtener referencias a los TextViews
        val textViewTotalRecicladores = view.findViewById<TextView>(R.id.text_total_recicladores)
        val textViewTotalAlmacen = view.findViewById<TextView>(R.id.text_total_stacker)
        
        // Obtener referencia al botón de salir y configurar su listener
        val buttonSalir = view.findViewById<Button>(R.id.button_salir_app)
        buttonSalir.setOnClickListener {
            // Llamar al método de salir de la actividad principal
            (requireActivity() as? ValleCASH)?.salirAplicacion()
        }

        // Observar el total dispensado y actualizar la UI
        viewModel.totalRecicladores.observe(viewLifecycleOwner) { total ->
            // Actualizar UI, por ejemplo:
            textViewTotalRecicladores.text = String.format(Locale.getDefault(),"Total Cambio: %.2f €", total)
        }

        // Observar el total del almacén y actualizar la UI
        viewModel.totalAlmacen.observe(viewLifecycleOwner) { total ->
            // Actualizar UI, por ejemplo:
            textViewTotalAlmacen.text = String.format(Locale.getDefault(),"Total Almacén: %.2f €", total)
        }
        commandSender.startSendingCommands()

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
                viewModel.setTotalRecicladores(it/100) // Actualiza total dispensado
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
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(webSocketReceiver)
            isReceiverRegistered = false
        }
        _binding = null
        commandSender.stopSendingCommands()
    }
}