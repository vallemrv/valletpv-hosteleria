package com.valleapp.vallecash

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.valleapp.vallecash.databinding.ActivitySettingsBinding
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout con View Binding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar desde app_bar_valle_cash
        setSupportActionBar(binding.root.findViewById(R.id.toolbar))

        // Cambiar el título a "Configuraciones"
        supportActionBar?.title = "Configuraciones"

        // Habilitar el botón de "Attrs"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val serverAddressInput = findViewById<EditText>(R.id.server_address_input)
        val saveButton = findViewById<Button>(R.id.save_button)
        val exitButton = findViewById<Button>(R.id.exit_button)

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

                    Toast.makeText(this, "Dirección guardada: $serverAddress", Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    Log.e("SETTINGS_ERR", e.toString())
                }
            } else {
                Toast.makeText(this, "Por favor, introduce una dirección válida", Toast.LENGTH_SHORT).show()
            }
        }

        // Salir de la actividad al hacer clic en "Salir"
        exitButton.setOnClickListener {
            finish()
        }
    }

    // Esto te permitirá manejar la acción del botón "Atrás" de la barra de herramientas
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Regresar a la actividad anterior
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cargarPreferencias(): JSONObject? {
        val json = JSON()
        return try {
            json.deserializar("settings.dat", this)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    private fun guardarPreferencias(obj: JSONObject) {
        val json = JSON()
        json.serializar("settings.dat", obj, this)
    }
}