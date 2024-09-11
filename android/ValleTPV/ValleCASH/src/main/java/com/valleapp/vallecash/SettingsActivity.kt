package com.valleapp.vallecash

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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
