package com.example.vallechat.ui.settings

import android.content.ContentValues
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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.vallechat.R
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject


class SettingsFragment : Fragment() {

    private lateinit var serverAddressInput: EditText
    private lateinit var saveButton: Button
    private lateinit var getUIDButton: Button
    private lateinit var explanationText: TextView
    private var preferencias: JSONObject? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        serverAddressInput = view.findViewById(R.id.server_address_input)
        saveButton = view.findViewById(R.id.save_button)
        getUIDButton = view.findViewById(R.id.get_uid_button)
        explanationText = view.findViewById(R.id.explanation_text)

        // Inicialmente ocultar el botón "Conseguir UID" y la explicación
        getUIDButton.visibility = View.GONE
        explanationText.visibility = View.GONE

        // Habilitar la flecha de back en el toolbar usando MenuProvider
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // No es necesario inflar un menú aquí
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        activity?.onBackPressedDispatcher?.onBackPressed()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Configurar la flecha de retroceso en el toolbar
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // Cargar preferencias al iniciar
        preferencias = cargarPreferencias()
        if (preferencias != null) {
            try {
                if (preferencias!!.has("URL")) {
                    serverAddressInput.setText(preferencias!!.getString("URL"))
                    mostrarBotonUID() // Si ya hay una URL, mostrar el botón UID
                }
                if (preferencias!!.has("UID")) {
                    explanationText.text = "Este dispositivo contiene un UID. Pero puedes conseguir otro si es necesario."
                }else{
                    explanationText.text = "Este dispositivo no contiene un UID. Para utilizar la aplilcacion necesita un UID activo."
                }
            } catch (e: JSONException) {
                Log.e("SETTINGS_ERR", e.toString())
            }
        }

        // Guardar preferencias al hacer clic en "Grabar"
        saveButton.setOnClickListener {
            val serverAddress = serverAddressInput.text.toString()
            if (serverAddress.isNotEmpty()) {
                val obj = JSONObject()
                try {
                    obj.put("URL", serverAddress)
                    guardarPreferencias(obj)

                    Toast.makeText(context, "Dirección guardada: $serverAddress", Toast.LENGTH_SHORT).show()

                    // Mostrar el botón "Conseguir UID" y el texto de explicación
                    mostrarBotonUID()

                } catch (e: JSONException) {
                    Log.e("SETTINGS_ERR", e.toString())
                }
            } else {
                Toast.makeText(context, "Por favor, introduce una dirección válida", Toast.LENGTH_SHORT).show()
            }
        }

        // Obtener UID al hacer clic en "Conseguir UID"
        getUIDButton.setOnClickListener {
            val serverAddress = serverAddressInput.text.toString()
            if (serverAddress.isNotEmpty()) {
                obtenerUID(serverAddress)
            } else {
                Toast.makeText(context, "Por favor, introduce la URL del servidor", Toast.LENGTH_SHORT).show()
            }
        }

        return view
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

    // Función para mostrar el botón "Conseguir UID" y el mensaje explicativo
    private fun mostrarBotonUID() {
        getUIDButton.visibility = View.VISIBLE
        explanationText.visibility = View.VISIBLE
    }

    // Función para obtener el UID del servidor
    private fun obtenerUID(server: String) {

        val url = "$server/api/dispositivos/get_device_uid"
        val p = ContentValues()
        explanationText.text = "Conectando al servidor..."

        val handler = Handler(Looper.getMainLooper()) {
            try {
                val res = it.data.getString("RESPONSE")
                val json = res?.let { it1 -> JSONObject(it1) }
                if (json != null) {
                    if (json.has("uid")) {
                        val uid = json.getString("uid")
                        preferencias?.put("UID", uid)
                        preferencias?.let { it1 -> guardarPreferencias(it1) }
                        explanationText.text = "Conexión exitosa. UID recibido."
                    }
                }
            }catch (e: JSONException){
                Log.e("SETTINGS_ERR", e.toString())
            }

            true
        }

        // Realizar la solicitud HTTP
        HTTPRequest(url, p, "uid", handler)
    }
}