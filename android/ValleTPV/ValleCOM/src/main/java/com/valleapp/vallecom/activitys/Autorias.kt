package com.valleapp.vallecom.activitys

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.valleapp.vallecom.adaptadores.AdaptadorAutorias
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Autorias : ActivityBase() { // Inherit from your Kotlin ActivityBase

    // Use MutableList for lists that change. Initialize eagerly.
    private var lsObj: MutableList<JSONObject> = mutableListOf()
    private var uid: String = ""

    // server is inherited from ActivityBase, ensure it's accessible (e.g., protected)
    // If server was private in ActivityBase, you'll need a getter or make it protected.
    // Assuming 'server' from ActivityBase is protected var server: String? = null

    // Define a companion object for constants like the TAG
    companion object {
        private const val TAG = "AutoriasActivity" // Class name for TAG is common practice
        private const val EXTRA_MENSAJES = "mensajes"
        private const val EXTRA_URL = "url"
        private const val EXTRA_PETICIONES = "peticiones"
        private const val EXTRA_UID = "uid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autorias)

        // --- Safely retrieve Intent Extras ---
        val intentUrl = intent.getStringExtra(EXTRA_URL)
        uid = intent.getStringExtra(EXTRA_UID).toString()
        if (intentUrl == null) {
            Log.e(TAG, "'$EXTRA_URL' extra is missing in Intent. Finishing activity.")
            finish() // Can't proceed without server URL
            return // Prevent further execution in onCreate
        }
        // Assign to the 'server' property inherited from ActivityBase
        // Make sure 'server' in ActivityBase is mutable (var) and accessible (protected)
        this.server = intentUrl

        val peticionesJsonString = intent.getStringExtra(EXTRA_PETICIONES)
        lsObj = mutableListOf() // Initialize the list (redundant if initialized at declaration)

        if (peticionesJsonString != null) {
            try {
                val jsonArray = JSONArray(peticionesJsonString)
                for (i in 0 until jsonArray.length()) {
                    // Add only valid JSONObjects
                    jsonArray.optJSONObject(i)?.let { jsonObject ->
                            lsObj.add(jsonObject)
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error parsing '$EXTRA_PETICIONES' JSON string: $peticionesJsonString", e)
                // Decide behavior: Continue with empty list? Show error?
            }
        } else {
            Log.w(TAG, "'$EXTRA_PETICIONES' extra is missing or null.")
            // List remains empty, which will be handled by mostrarLista
        }
        // --- End Intent Extra Handling ---

        // Initial display after loading data
        mostrarLista()
    }

    override fun onResume() {
        super.onResume()
        // Optionally refresh or update the list if needed when activity resumes
        // mostrarLista() // Already called at the end of onCreate, might be redundant unless data can change while paused
    }

    // Made private as it seems only used internally
    private fun mostrarLista() {
        try {
            // Cambiar ListView por RecyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.lista_peticiones)

            if (lsObj.isNotEmpty()) {
                // Configurar el RecyclerView con un LayoutManager y el adaptador
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = AdaptadorAutorias(this, lsObj)
                recyclerView.visibility = View.VISIBLE // Asegurar que el RecyclerView sea visible
            } else {
                // Lista vacía, finalizar actividad como en la lógica original
                Log.i(TAG, "List of peticiones is empty, finishing.")
                recyclerView.visibility = View.GONE // Ocultar RecyclerView si no se finaliza inmediatamente
                salirAutorias(null)
            }
        } catch (e: Exception) {
            // Capturar excepciones potenciales durante la configuración del RecyclerView
            Log.e(TAG, "Error updating or displaying the RecyclerView", e)
        }
    }

    // Refactored common logic for handling petition responses
    private fun handlePeticion(v: View, aceptadaValue: String) {
        val jsonObject = v.tag as? JSONObject
        if (jsonObject == null) {
            Log.e(TAG, "View tag is missing or is not a JSONObject.")
            return
        }

        val currentServer = this.server // Use server property from ActivityBase
        if (currentServer == null) {
            Log.e(TAG, "Server URL is null. Cannot send authorization request.")
            return
        }

        try {
            val idPeticion = jsonObject.getString("idpeticion") // Could throw JSONException

            val params = ContentValues().apply {
                put("aceptada", aceptadaValue)
                put("idpeticion", idPeticion)
                put("uid", uid) // Include UID in the request
            }

            // Construct URL using string template
            val url = "$currentServer/autorizaciones/gestionar_peticion"

            // Make the HTTP request (ensure HTTPRequest handles background execution)
            HTTPRequest(url, params, "", null) // Assuming this runs asynchronously

            // Update UI optimistically: remove item and refresh list
            val removed = lsObj.remove(jsonObject)
            if(removed) {
                mostrarLista()
            } else {
                Log.w(TAG, "Failed to remove item from list, maybe it was already removed?")
            }

        } catch (e: JSONException) {
            Log.e(TAG, "Error accessing 'idpeticion' from JSONObject tag: $jsonObject", e)
        } catch (e: Exception) {
            // Catch other potential errors (e.g., network issues if HTTPRequest throws)
            Log.e(TAG, "Error sending petition update (ID: ${jsonObject.optString("idpeticion", "N/A")})", e)
        }
    }

    // Public function likely linked to button onClick in XML
    fun sendAutorizacion(v: View) {
        handlePeticion(v, "1")
    }

    // Public function likely linked to button onClick in XML
    fun sendCancelacion(v: View) {
        handlePeticion(v, "0")
    }

    // Public function likely linked to button onClick in XML or called internally
    // Parameter v can be null if called directly
    fun salirAutorias(v: View?) {
        val resultIntent = Intent()
        // Pass back the current state of the list (or relevant data)
        // Converting the whole list might be heavy, consider passing IDs or summary
        resultIntent.putExtra(EXTRA_MENSAJES, lsObj.toString()) // Pass remaining items as string
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    // onDestroy is often used for cleanup, but empty here
    // override fun onDestroy() {
    //     super.onDestroy()
    // }
}