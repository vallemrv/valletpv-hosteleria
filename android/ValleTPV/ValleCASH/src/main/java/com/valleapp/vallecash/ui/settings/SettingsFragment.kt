package com.valleapp.vallecash.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.valleapp.vallecash.R
import com.valleapp.vallecash.databinding.FragmentSettingsBinding
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

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

        val serverAddressInput = binding.root.findViewById<EditText>(R.id.server_address_input)
        val saveButton = binding.root.findViewById<Button>(R.id.save_button)
        val exitButton = binding.root.findViewById<Button>(R.id.exit_button)

        // Cargar preferencias al iniciar
        val preferencias = cargarPreferencias()
        if (preferencias != null) {
            try {
                if (preferencias.has("URL")) {
                    serverAddressInput.setText(preferencias.getString("URL"))
                }
            } catch (e: JSONException) {
                Log.e("SETTINGS_ERR", e.toString())
            }
        }

        // Guardar preferencias al hacer clic en "Guardar"
        saveButton.setOnClickListener {
            val serverAddress = serverAddressInput.text.toString()
            if (serverAddress.isNotEmpty()) {
                val obj = JSONObject()
                try {
                    obj.put("URL", serverAddress)
                    guardarPreferencias(obj)

                    Toast.makeText(context, "Dirección guardada: $serverAddress", Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    Log.e("SETTINGS_ERR", e.toString())
                }
            } else {
                Toast.makeText(context, "Por favor, introduce una dirección válida", Toast.LENGTH_SHORT).show()
            }
        }

        // Salir del fragmento al hacer clic en "Salir"
        exitButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()  // Ejecutar acción de "atrás"
        }

        return binding.root
    }

    private fun cargarPreferencias(): JSONObject? {
        val json = JSON()
        return try {
            json.deserializar("settings.dat", context)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    private fun guardarPreferencias(obj: JSONObject) {
        val json = JSON()
        json.serializar("settings.dat", obj, context)
    }
}
