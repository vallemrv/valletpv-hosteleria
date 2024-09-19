package com.valleapp.vallecash.ui.changecambio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
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
    private var isRetrievingAlmacen = false
    private var isReceiverRegistered = false
    private var isCanceling = false


    private val handler = Handler(Looper.getMainLooper())

    private val sendQCommand = object : Runnable {
        override fun run() {
            sendInstructionToWebSocket("#Q#")
            handler.postDelayed(this, 1000)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChangeCambioBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChangeCambioViewModel::class.java)
        serverURL = arguments?.getString("server").toString()

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

        // Configurar los observadores de LiveData
        setupObservers()

        // Configurar los listeners de los botones
        setupListeners()


        // Registrar el receptor solo una vez y marcarlo como registrado
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(webSocketReceiver, IntentFilter("WebSocketMessage"))
            isReceiverRegistered = true
        }

        viewModel.getTotales(serverURL)

        // Enviar instrucción #T# después de 1 segundo
        Handler(Looper.getMainLooper()).postDelayed({
            sendInstructionToWebSocket("#T#")
        }, 1000)

        return binding.root
    }

    private fun setupObservers() {
        viewModel.fondoDeCaja.observe(viewLifecycleOwner) { cambio ->
            binding.edittextCambio.setText(String.format(Locale.getDefault(),"%.2f", cambio))
        }

        viewModel.totalAlmacenes.observe(viewLifecycleOwner) { almacenes ->
            binding.textTotalAlmacen.text =  String.format(Locale.getDefault(),  "Total Almacen: %.2f €", almacenes)
        }

        viewModel.totalRecicladores.observe(viewLifecycleOwner) { _ ->
            binding.textTotalCambio.text = String.format(Locale.getDefault(), "Total cambio: %.2f €",
                viewModel.getTotalRecicladores())
        }
        viewModel.totalAdmitido.observe(viewLifecycleOwner) { _ ->
            binding.textTotalCambio.text = String.format(Locale.getDefault(), "Total cambio: %.2f €",
                viewModel.getTotalRecicladores())
        }

        viewModel.totalUltimoStacker.observe(viewLifecycleOwner) { totalStacker ->
            binding.textAlmacenAnterior.text = String.format(Locale.getDefault(), "Ultimo almacenado: %.2f €", totalStacker)
        }

        viewModel.cambioReal.observe(viewLifecycleOwner) { camionReal ->
            binding.textCambioReal.text = String.format(Locale.getDefault(), "Ultimo cambio: %.2f €", camionReal)
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            binding.textMessageBox.text = message
        }

    }

    private fun setupListeners() {
        binding.buttonAceptarCambo.setOnClickListener {
            if (!isAcceptingChange) {
                // Iniciar admisión de cambio
                sendInstructionToWebSocket("#A#2#")
                isAcceptingChange = true
                // Cambiar el ícono a una 'X' de cancelar
                binding.buttonAceptarCambo.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.cancelar)
                )
            } else {
                // Detener el envío de #Q#
                handler.removeCallbacks(sendQCommand)

                isAcceptingChange = false
                // Restaurar el ícono original
                binding.buttonAceptarCambo.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.insertar_camibo_mano)
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    sendInstructionToWebSocket("#J#")
                }, 200)
            }

    }

        binding.buttonChangeCambio.setOnClickListener {
            if (!isRetrievingAlmacen) {
                val newCambio = binding.edittextCambio.text.toString().toDoubleOrNull()
                if (newCambio != null && newCambio != viewModel.cambioReal.value) {
                    viewModel.fondoDeCaja.value = newCambio
                    // Actualizar el servidor con el nuevo valor de cambio
                    viewModel.updateTotales(serverURL)
                } else {
                    // Mostrar mensaje de error
                    viewModel.message.postValue("No se ha producido ningún cambio. Accion cancelada.")
                }
            }
        }

        binding.butttonRetirarStacker.setOnClickListener {
            if (!isRetrievingAlmacen && !isAcceptingChange) {
                // Iniciar retiro de almacén
                sendInstructionToWebSocket("#S#2#")
                isRetrievingAlmacen = true
                // Cambiar el ícono a cancelar
                binding.butttonRetirarStacker.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.cancelar)
                )
                // Mostrar mensaje en el TextView
                viewModel.message.postValue("La aplicación está bloqueada hasta que se retire el almacén o se pulse cancelar.")
                // Bloquear otros botones
                disableButtons(binding.butttonRetirarStacker, true)
            } else if (isRetrievingAlmacen) {

                // Enviar comando de cancelar
                sendInstructionToWebSocket("#!#")
                isRetrievingAlmacen = false
                isCanceling = true
                // Restaurar el ícono original
                binding.butttonRetirarStacker.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.retirar_almacen)
                )
                // Limpiar mensaje
                viewModel.message.postValue("")
                // Desbloquear botones
                disableButtons(null, false)
            }
        }
    }

    private fun disableButtons(exceptButton: View?, disable: Boolean) {
        binding.buttonAceptarCambo.isEnabled = !disable
        binding.buttonChangeCambio.isEnabled = !disable

        // Añade más botones si es necesario
        if (exceptButton != null){
          exceptButton.isEnabled = true
        }
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
            println("Mensaje recibido: $message")
            println("Instrucción recibida: $instruction")
            message?.let {
                when {
                    instruction?.contains("#T#") == true -> {
                        procesarTCadena(it)
                    }
                    instruction?.contains("#Q#") == true -> {
                        procesarQCadena(it)
                    }
                    instruction?.contains("#A#2#") == true -> {
                        handler.post(sendQCommand)
                    }
                    instruction?.contains("#J#") == true -> {
                        procesarJCadena(it)
                        viewModel.updateTotales(serverURL)
                        handler.removeCallbacks(sendQCommand)
                    }
                    instruction?.contains("#S#2#") == true -> {
                        if(!isCanceling) {
                            isRetrievingAlmacen = false
                            isCanceling = false
                            viewModel.message.postValue("El almacén se ha retirado correctamente.")
                            binding.butttonRetirarStacker.setImageDrawable(
                                ContextCompat.getDrawable(requireContext(), R.drawable.retirar_almacen)
                            )
                            procesarSCadena(it)
                        }else{
                            viewModel.message.postValue("Retirada de almacen cancelado.")
                        }
                        println("Retirado: $it")
                     }
                    instruction?.contains("#!#") == true -> {
                        println("Cancelado: $it")
                        viewModel.message.postValue("Retirada de almacen cancelado.")
                    }
                    // Añadir más casos según sea necesario
                    else -> {}
                }
            }
        }
    }

    private fun procesarTCadena(cadena: String) {
        // Suponiendo que la cadena es del formato #a#b#c#
        val partes = cadena.split("#")
        if (partes.size >= 4) {
            val b = partes[2] // Total Cambio
            val c = partes[3] // Total Almacén

            // Actualizar los LiveData en el ViewModel
            var reciclarodres = b.toDoubleOrNull() ?: 0.0
            var almacenes = c.toDoubleOrNull() ?: 0.0
            if (reciclarodres > 0) reciclarodres /= 100
            if (almacenes > 0) almacenes /= 100
            viewModel.totalRecicladores.postValue(reciclarodres)
            viewModel.totalAlmacenes.postValue(almacenes)
        }
    }

    private fun procesarSCadena(cadena: String) {
        // Suponiendo que la cadena es del formato #a#b#
        Log.d("ProcesarSCadena", "Cadena recibida: $cadena")

        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val b = partes[2] // Importe recibido
            // Actualizar el total admitido en el ViewModel
            var admitido = b.toDoubleOrNull() ?: 0.0
            if (admitido > 0) admitido /= 100
            viewModel.totalRetiradoStacker.postValue(admitido)
        }
    }

    private fun procesarQCadena(cadena: String) {
        // Suponiendo que la cadena es del formato #a#b#
        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val b = partes[2] // Importe recibido

            // Actualizar el total admitido en el ViewModel
            var nuevoAdmitido = b.toDoubleOrNull() ?: 0.0
            if (nuevoAdmitido > 0) nuevoAdmitido /= 100
            var admitido = viewModel.totalAdmitido.value ?: 0.0
            if (nuevoAdmitido != admitido) {
                viewModel.totalAdmitido.postValue(nuevoAdmitido)
            }
        }

    }

    private fun procesarJCadena(cadena: String) {
        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val b = partes[2] // Importe recibido

            // Actualizar el total admitido en el ViewModel
            var nuevoAdmitido = b.toDoubleOrNull() ?: 0.0
            if (nuevoAdmitido > 0) nuevoAdmitido /= 100
            var admitido = viewModel.totalAdmitido.value ?: 0.0
            if (nuevoAdmitido != admitido) {
                viewModel.totalAdmitido.postValue(nuevoAdmitido)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(webSocketReceiver)
            isReceiverRegistered = false
        }
    }
}