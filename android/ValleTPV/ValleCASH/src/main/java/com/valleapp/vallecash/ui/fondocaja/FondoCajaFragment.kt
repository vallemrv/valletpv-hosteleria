package com.valleapp.vallecash.ui.fondocaja

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.valleapp.vallecash.tools.WebSocketService
import com.valleapp.valletpv.R
import com.valleapp.valletpv.databinding.FragmentFondoCajaBinding
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


class FondoCajaFragment : Fragment() {

    private var _binding: FragmentFondoCajaBinding? = null
    private val binding get() = _binding!!
    private var serverURL = ""
    private var uid = ""

    private lateinit var viewModel: FondoCajaViewModel
    private var isAcceptingChange = false
    private var isRetrievingAlmacen = false
    private var isReceiverRegistered = false
    private var isProcesando = false




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFondoCajaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[FondoCajaViewModel::class.java]
        serverURL = arguments?.getString("server").toString()

        // Cargar uid desde preferencias
        val preferencias = cargarPreferencias()
        uid = preferencias?.optString("uid", "") ?: ""

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

        viewModel.getTotales(serverURL, uid)

        // Enviar instrucción #T# después de 1 segundo
        Handler(Looper.getMainLooper()).postDelayed({
            sendInstructionToWebSocket("#T#")
        }, 200)

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

    private fun sendQCommand() {
        Handler(Looper.getMainLooper()).postDelayed({
            sendInstructionToWebSocket("#Q#")
        }, 200)
    }

    private fun setupListeners() {
        binding.buttonAceptarCambo.setOnClickListener {

            if (!isAcceptingChange) {
                if (isProcesando) return@setOnClickListener
                isProcesando = true
                // Iniciar admisión de cambio
                sendInstructionToWebSocket("#A#2#")
                isAcceptingChange = true
                // Cambiar el ícono a una 'X' de cancelar
                binding.buttonAceptarCambo.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.cancelar)
                )
            } else {
                isAcceptingChange = false
                binding.buttonAceptarCambo.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.insertar_camibo_mano)
                )
            }

        }

        binding.buttonChangeCambio.setOnClickListener {
            if (!isRetrievingAlmacen) {
                val newCambio = binding.edittextCambio.text.toString().replace(",",".").toDoubleOrNull()
                if (newCambio != null && newCambio != viewModel.cambioReal.value) {
                    viewModel.fondoDeCaja.value = newCambio
                    viewModel.updateTotales(serverURL, uid)
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
                    instruction?.contains("#A#") == true -> {
                       sendQCommand()
                    }
                    instruction?.contains("#J#") == true -> {
                        procesarJCadena(it)
                        viewModel.updateTotales(serverURL, uid)
                    }
                    instruction?.contains("#S#") == true -> {
                        procesarSCadena(it)
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
        isRetrievingAlmacen = false
        binding.butttonRetirarStacker.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.retirar_cambio)
        )

        if(cadena.startsWith("#ER") || cadena.startsWith("#WR:CANCEL#")){
            viewModel.message.postValue("Retirada de almacen cancelado.")
            sendInstructionToWebSocket("#J#")
            return
        }
        viewModel.message.postValue("El almacén se ha retirado correctamente.")


        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val b = partes[2] // Importe recibido
            // Actualizar el total admitido en el ViewModel
            var retirado = b.toDoubleOrNull() ?: 0.0
            if (retirado > 0) retirado /= 100
            viewModel.totalRetiradoStacker.value = retirado
            viewModel.updateTotales(serverURL, uid)
            viewModel.getTotales(serverURL, uid)
        }
        sendInstructionToWebSocket("#T#")
        disableButtons(binding.butttonRetirarStacker, false)
    }

    private fun procesarQCadena(cadena: String) {
        // Suponiendo que la cadena es del formato #a#b#
        val partes = cadena.split("#")
        if (partes.size >= 3) {
            val b = partes[2] // Importe recibido

            // Actualizar el total admitido en el ViewModel
            var nuevoAdmitido = b.toDoubleOrNull() ?: 0.0
            if (nuevoAdmitido > 0) nuevoAdmitido /= 100
            val admitido = viewModel.totalAdmitido.value ?: 0.0
            if (nuevoAdmitido != admitido) {
                viewModel.totalAdmitido.postValue(nuevoAdmitido)
            }
        }

        if (isAcceptingChange) {
            sendQCommand()
        }else {
            sendInstructionToWebSocket("#J#")
        }

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
                viewModel.totalAdmitido.postValue(nuevoAdmitido)
                viewModel.updateTotales(serverURL, uid)
                isProcesando = false
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