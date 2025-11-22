package com.valleapp.vallecash.ui.modificarcambio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.valleapp.vallecash.tools.WebSocketService
import com.valleapp.valletpv.R
import com.valleapp.valletpv.databinding.FragmentModificarCambioBinding
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


class ModificarCambioFragment : Fragment() {

    // Obtener el ViewModel
    private val viewModel: DenominationViewModel by activityViewModels()
    private var _binding: FragmentModificarCambioBinding? = null
    private val binding get() = _binding!!
    private var serverURL = ""
    private var uid = ""
    private var isAceptar = false

    private val webSocketReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val instruction = intent.getStringExtra("instruccion")
            val message = intent.getStringExtra("message")

            message?.let {
                if (instruction != null) {
                    if (instruction.contains("#Y#")) {
                        viewModel.processResponseY(it)  // Llamamos al ViewModel para procesar la respuesta
                        if(isAceptar){
                            Handler(Looper.getMainLooper()).postDelayed({
                                sendInstructionToWebSocket("#Y#")
                            }, 1000)
                            }

                    }else if (instruction.contains("#U#")){
                        viewModel.processResponseU(it)
                        viewModel.updateTotales(serverURL, uid)
                    }else if (instruction.contains("#A#")){
                        sendInstructionToWebSocket("#Y#")
                    }else if (instruction.contains("#J#")){
                        procesarJCadena(it)
                        viewModel.updateTotales(serverURL, uid)
                    }
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModificarCambioBinding.inflate(inflater, container, false)
        // Configuración del callback para manejar el botón "Atrás"
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Aquí puedes realizar alguna acción antes de volver atrás
                    activity?.supportFragmentManager?.popBackStack()  // Volver al fragment anterior
                }
            }
        )

        // Añadir MenuProvider para desactivar el menú
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // No inflar el menú en SettingsFragment
                menu.clear() // Elimina cualquier menú si lo hubiera
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false // No manejar ninguna acción de menú aquí
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return binding.root
    }

    // Método para enviar la instrucción al WebSocketService
    private fun sendInstructionToWebSocket(instruction: String) {
         // Crea un Intent para enviar la instrucción al WebSocketService
        val intent = Intent(requireContext(), WebSocketService::class.java)
        intent.putExtra("action", "SEND_INSTRUCTION") // Acción que el servicio va a manejar
        intent.putExtra("instruction", instruction) // Instrucción que se va a enviar
        requireContext().startService(intent) // Enviar la instrucción al servicio
        viewModel.getTotales(serverURL, uid)
    }


    private fun procesarJCadena(cadena: String) {
        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val b = partes[2] // Importe recibido

            // Actualizar el total admitido en el ViewModel
            var nuevoAdmitido = b.toDoubleOrNull() ?: 0.0
            if (nuevoAdmitido > 0) nuevoAdmitido /= 100
            val admitido = viewModel.totalAdmitido.value ?: 0.0
            if (nuevoAdmitido != admitido) {
                viewModel.setAdmitido(nuevoAdmitido)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serverURL = arguments?.getString("server").toString()

        // Cargar uid desde preferencias
        val preferencias = cargarPreferencias()
        uid = preferencias?.optString("uid", "") ?: ""

        // Registrar el BroadcastReceiver
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(webSocketReceiver, IntentFilter("WebSocketMessage"))


        // Configurar el RecyclerView
        binding.recyclerViewDenominations.layoutManager = LinearLayoutManager(requireContext())

        // Observar las denominaciones en el ViewModel
        viewModel.denominations.observe(viewLifecycleOwner) { denominations ->
            val adapter = DenominationAdapter(denominations) { denomination, amount ->
                viewModel.updateAmount(denomination, amount) // Actualizar las cantidades en el ViewModel
            }
            binding.recyclerViewDenominations.adapter = adapter
        }

        // Observar los cambios en el resultado de la actualización
        viewModel.updateResult.observe(viewLifecycleOwner) { _ ->
            if(isAceptar) return@observe
            sendInstructionToWebSocket("#Y#")
        }


        // Observar los cambios  en la variable ocupaco para mostrar o no el loading
        viewModel.totalRecicladores.observe(viewLifecycleOwner) { total ->
           if(total<=0){
               binding.totalTextView.text = ""
           }else{
               binding.totalTextView.text = String.format(Locale.getDefault(),"Total: %.2f €", total)
           }
        }


        // Observar los cambios  en la variable ocupaco para mostrar o no el loading
        viewModel.totalDispensado.observe(viewLifecycleOwner) { dispensado ->
            if (dispensado <= 0) {
                binding.devolucionTextView.text = ""
            } else {
                binding.devolucionTextView.text =
                    String.format(Locale.getDefault(),"Devolucion: %.2f €", dispensado)
            }
        }

        viewModel.totalAdmitido.observe(viewLifecycleOwner) { admitido ->
            if (admitido <= 0) {
                binding.devolucionTextView.text = ""
            } else {
                binding.devolucionTextView.text =
                    String.format(Locale.getDefault(),"Admitido: %.2f €", admitido)
            }

        }


        // Enviar la instrucción al WebSocketService cuando se muestra el Fragment
        sendInstructionToWebSocket("#Y#")

        // Configurar el botón "Dispensar seleccionado"
        binding.buttonDispenseSelected.setOnClickListener {
            if (isAceptar) return@setOnClickListener
            val command = viewModel.generateDispenseCommand(0, 0, 0)  // Ejemplo con algunos parámetros
            if (command.isNotEmpty()) sendInstructionToWebSocket(command)
        }

        binding.buttonInsertMoney.setOnClickListener {
            if(isAceptar){
                isAceptar = false
                binding.buttonInsertMoney.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.insertar_camibo_mano)
                )
                sendInstructionToWebSocket("#J#")
            }else{
                isAceptar = true
                binding.buttonInsertMoney.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.cancelar)
                )
                sendInstructionToWebSocket("#A#2#")
            }

        }

        // Configurar el botón "Refrescar cambio"
        binding.buttonRefresh.setOnClickListener {
            if (isAceptar) return@setOnClickListener
            // Enviar la instrucción al WebSocketService cuando se muestra el Fragment
            sendInstructionToWebSocket("#Y#")

        }
        binding.btnNavegarFondoCaja.setOnClickListener {
            val bundle = Bundle().apply { putString("server", serverURL) }
            findNavController().navigate(R.id.nav_fondo_caja, bundle)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Desregistrar el BroadcastReceiver cuando se destruya la vista
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(webSocketReceiver)
    }

    private fun cargarPreferencias(): JSONObject? {
        val json = JSON()
        return try {
            json.deserializar("settings.dat", requireContext())
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }
}
