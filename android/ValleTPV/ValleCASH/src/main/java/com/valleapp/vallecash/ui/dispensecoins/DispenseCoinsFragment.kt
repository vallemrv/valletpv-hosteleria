package com.valleapp.vallecash.ui.dispensecoins

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecash.R
import com.valleapp.vallecash.WebSocketService
import com.valleapp.vallecash.databinding.FragmentDispenseCoinsBinding
import java.util.Locale


class DispenseCoinsFragment : Fragment() {

    // Obtener el ViewModel
    private val viewModel: DenominationViewModel by activityViewModels()
    private var _binding: FragmentDispenseCoinsBinding? = null
    private val binding get() = _binding!!
    private var serverURL = ""

    private val webSocketReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val instruction = intent.getStringExtra("instruccion")
            val message = intent.getStringExtra("message")

            message?.let {
                if (instruction != null) {
                    if (instruction.contains("#Y#")) {
                        viewModel.processResponseY(it)  // Llamamos al ViewModel para procesar la respuesta
                    }else if (instruction.contains("#U#")){
                        val total = procesarWRLevel(it)
                        viewModel.setTotalDispensao(total)
                        viewModel.updateTotales(serverURL)
                    }
                }
                viewModel.setOcupado(false)
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDispenseCoinsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Método para enviar la instrucción al WebSocketService
    private fun sendInstructionToWebSocket(instruction: String) {
        viewModel.setOcupado(true)
        // Crea un Intent para enviar la instrucción al WebSocketService
        val intent = Intent(requireContext(), WebSocketService::class.java)
        intent.putExtra("action", "SEND_INSTRUCTION") // Acción que el servicio va a manejar
        intent.putExtra("instruction", instruction) // Instrucción que se va a enviar
        requireContext().startService(intent) // Enviar la instrucción al servicio
        viewModel.getTotales(serverURL)
    }

    // Función para procesar la cadena #WR:LEVEL# y calcular el total en euros
    private fun procesarWRLevel(message: String): Double {
        // Limpiar la cadena eliminando el prefijo y sufijo
        val cleanMessage = message.substringAfter("#WR:LEVEL#").substringBefore("#")

        // Dividir las partes de denominación:cantidad
        val denominacionesYCantidades = cleanMessage.split(",", ";").map {
            it.split(":").let { (denominacion, cantidad) ->
                denominacion.toInt() to cantidad.toInt()
            }
        }

        // Calcular el total en céntimos
        val totalEnCentimos = denominacionesYCantidades
            .filter { it.second > 0 } // Filtrar donde la cantidad sea mayor que 0
            .sumOf { it.first * it.second } // Sumar denominación * cantidad

        // Convertir a euros
        return totalEnCentimos / 100.0
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serverURL = arguments?.getString("server").toString()

        // Registrar el BroadcastReceiver
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(webSocketReceiver, IntentFilter("WebSocketMessage"))


        /// Acceder al RecyclerView usando view.findViewById()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_denominations)

        // Acceder a los botones usando view.findViewById()
        val buttonDispenseSelected = view.findViewById<Button>(R.id.button_dispense_selected)
        val buttonRefresh = view.findViewById<Button>(R.id.button_refresh)
        val buttonExit = view.findViewById<Button>(R.id.button_exit)


        // Configurar el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observar las denominaciones en el ViewModel
        viewModel.denominations.observe(viewLifecycleOwner, Observer { denominations ->
            val adapter = DenominationAdapter(denominations) { denomination, amount ->
                viewModel.updateAmount(denomination, amount) // Actualizar las cantidades en el ViewModel
            }
            recyclerView.adapter = adapter
        })

        // Observar los cambios en el resultado de la actualización
        viewModel.updateResult.observe(viewLifecycleOwner) { success ->
           sendInstructionToWebSocket("#Y#")
        }

        // Inicializa el ProgressBar
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)

        // Observar los cambios  en la variable ocupaco para mostrar o no el loading
        viewModel.ocupado.observe(viewLifecycleOwner) { isOcupado ->
            if (isOcupado) {
                progressBar.visibility = View.VISIBLE // Muestra el ProgressBar
                buttonRefresh.isEnabled = false
                buttonExit.isEnabled = false
                buttonDispenseSelected.isEnabled = false
            } else {
                progressBar.visibility = View.GONE // Oculta el ProgressBar
                buttonRefresh.isEnabled = true
                buttonExit.isEnabled = true
                buttonDispenseSelected.isEnabled = true
            }
        }

        val text_recicladores_view = view.findViewById<TextView>(R.id.total_text_view)
        // Observar los cambios  en la variable ocupaco para mostrar o no el loading
        viewModel.totalRecicladores.observe(viewLifecycleOwner) { total ->
           if(total<=0){
               text_recicladores_view.setText("")
           }else{
               text_recicladores_view.setText(String.format(Locale.getDefault(),"Total: %.2f €", total))
           }
        }

        val text_dipensado_view = view.findViewById<TextView>(R.id.devolucion_text_view)

        // Observar los cambios  en la variable ocupaco para mostrar o no el loading
        viewModel.totalDispensado.observe(viewLifecycleOwner) { dispensado ->
            if (dispensado <= 0) {
                text_dipensado_view.setText("")
            } else {
                text_dipensado_view.setText(String.format(Locale.getDefault(),"Devolucion: %.2f €", dispensado))
            }
        }


        // Enviar la instrucción al WebSocketService cuando se muestra el Fragment
        sendInstructionToWebSocket("#Y#")

        // Configurar el botón "Dispensar seleccionado"
        buttonDispenseSelected.setOnClickListener {
            val command = viewModel.generateDispenseCommand(0, 0, 0)  // Ejemplo con algunos parámetros
            if (command.isNotEmpty()) sendInstructionToWebSocket(command)
        }

        // Configurar el botón "Refrescar cambio"
        buttonRefresh.setOnClickListener {
            // Enviar la instrucción al WebSocketService cuando se muestra el Fragment
            sendInstructionToWebSocket("#Y#")

        }

        // Configurar el botón "Salir"
        buttonExit.setOnClickListener {
            // Navegar de vuelta al HomeFragment usando NavController
            val navController = findNavController()
            navController.popBackStack()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Desregistrar el BroadcastReceiver cuando se destruya la vista
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(webSocketReceiver)
    }
}
