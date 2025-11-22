package com.valleapp.vallecash.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.valleapp.valletpv.R
import com.valleapp.valletpv.databinding.FragmentSettingsBinding

import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


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
        val aliasInput = binding.root.findViewById<EditText>(R.id.alias_input)
        val saveButton = binding.root.findViewById<Button>(R.id.save_button)

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

        // Cargar preferencias al iniciar
        val preferencias = cargarPreferencias()
        var savedUrl = ""
        if (preferencias != null) {
            try {
                if (preferencias.has("URL")) {
                    val url = preferencias.getString("URL")
                    serverAddressInput.setText(url)
                    savedUrl = url
                }
                if (preferencias.has("alias")) {
                    aliasInput.setText(preferencias.getString("alias"))
                }
                if (preferencias.has("uid")) {
                    saveButton.isEnabled = false
                }
            } catch (e: JSONException) {
                Log.e("SETTINGS_ERR", e.toString())
            }
        }

        // Añadir TextWatcher para habilitar el botón si cambia la URL
        serverAddressInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val currentNormalizedUrl = normalizeApiUrl(s.toString())
                if (currentNormalizedUrl != savedUrl) {
                    saveButton.isEnabled = true
                } else if (preferencias?.has("uid") == true) {
                    saveButton.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Guardar preferencias al hacer clic en "Guardar"
        saveButton.setOnClickListener {
            val rawAddress = serverAddressInput.text.toString().trim()
            val alias = aliasInput.text.toString().trim()

            if (rawAddress.isEmpty() || alias.isEmpty()) {
                Toast.makeText(requireContext(), "Introduce URL y alias válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiBase = normalizeApiUrl(rawAddress)
            val ctx = requireContext() // Capturar el contexto antes del Thread

            // Ejecutar en hilo de fondo
            Thread {
                try {
                    // 1. Health check
                    val healthUrl = joinUrl(apiBase, "health")
                    println("Health URL: $healthUrl")
                    val healthResp = postRequest(healthUrl, mapOf())
                    val healthJson = JSONObject(healthResp)
                    val success = healthJson.optBoolean("success", false)

                    if (!success) {
                        activity?.runOnUiThread {
                            Toast.makeText(ctx, "Health check falló en el servidor", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }

                    // 2. Crear UID en servidor
                    val createUrl = joinUrl(apiBase, "dispositivo/create_uid")
                    val createResp = postRequest(createUrl, mapOf("alias" to alias))
                    val createJson = JSONObject(createResp)
                    val uid = createJson.optString("uid", "")

                    if (uid.isEmpty()) {
                        activity?.runOnUiThread {
                            Toast.makeText(ctx, "No se obtuvo UID del servidor", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }

                    // 3. Guardar URL, alias y uid
                    val obj = JSONObject()
                    obj.put("URL", apiBase)
                    obj.put("alias", alias)
                    obj.put("uid", uid)
                    guardarPreferencias(obj)

                    activity?.runOnUiThread {
                        Toast.makeText(ctx, "Configuración guardada. UID: $uid", Toast.LENGTH_SHORT).show()
                        saveButton.isEnabled = false
                        savedUrl = apiBase
                    }
                } catch (e: Exception) {
                    Log.e("SETTINGS_ERR", "Error al conectar: ${e.message}", e)
                    activity?.runOnUiThread {
                        Toast.makeText(ctx, "Error al conectar con el servidor: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
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

    private fun normalizeApiUrl(input: String): String {
        var s = input.trim()
        // quitar slash final
        while (s.endsWith("/")) s = s.dropLast(1)

        // Determinar el protocolo a usar y quitar el protocolo de la cadena
        val protocol = when {
            s.startsWith("https://") -> {
                s = s.substringAfter("https://")
                "https://"
            }
            s.startsWith("http://") -> {
                s = s.substringAfter("http://")
                "http://"
            }
            else -> "http://" // Por defecto usar http:// si no hay protocolo
        }

        // añadir /api si falta
        if (!s.endsWith("/api")) {
            s = "$s/api"
        }

        // devolver con el protocolo correcto
        return "$protocol$s"
    }

    private fun joinUrl(base: String, path: String): String {
        var b = base
        if (b.endsWith("/")) b = b.dropLast(1)
        var p = path
        if (p.startsWith("/")) p = p.drop(1)
        return "$b/$p"
    }

    // POST simple application/x-www-form-urlencoded
    private fun postRequest(urlString: String, params: Map<String, String>): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")

        // Añadir uid automáticamente si está guardado y no viene en params
        val finalParams = HashMap(params)
        try {
            val prefs = cargarPreferencias()
            if (prefs != null && prefs.has("uid")) {
                val uidVal = prefs.getString("uid")
                if (!uidVal.isNullOrEmpty() && !finalParams.containsKey("uid")) {
                    finalParams["uid"] = uidVal
                }
            }
        } catch (_: Exception) {
            // ignorar y continuar sin uid
        }

        val postData = finalParams.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }

        BufferedOutputStream(conn.outputStream).use { out ->
            out.write(postData.toByteArray(Charsets.UTF_8))
            out.flush()
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        conn.disconnect()
        return sb.toString()
    }
}
