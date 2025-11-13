package com.valleapp.vallecom.activitys // Ensure this package is correct

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecom.adaptadores.AdaptadorBuscarPedidos
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.tools.Instruccion
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.concurrent.thread

class BuscarPedidos : ActivityBase(), TextWatcher {

    // Companion object for constants
    companion object {
        private const val TAG = "BuscarPedidos"
        private const val EXTRA_URL = "url"
    }

    private lateinit var txtBuscar: TextView

    // *** CORRECCIÓN PRINCIPAL: Usar MutableList ***
    private var lPedidos: JSONArray = JSONArray()

    private var dbCuenta: DBCuenta? = null
    private var isBound = false // Track service binding state

    var filterQuery = "servido = 0"

    // Service Connection
    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            try {
                // Usa el tipo correcto del Binder de tu servicio
                val binder = iBinder as? ServiceCOM.MyBinder // Asumiendo ServicioCom
                if (binder == null) {
                    Log.e(TAG, "Binder is not of expected type ServicioCom.MyBinder")
                    isBound = false // No se pudo enlazar correctamente
                    return
                }
                myServicio = binder.getService()
                isBound = true

                dbCuenta = myServicio?.getDb("lineaspedido") as? DBCuenta
                if (dbCuenta == null) {
                    Log.e(TAG, "Failed to get DBCuenta instance from service.")
                    return
                }

                // Cargar datos iniciales. Asumimos que getAll devuelve List? o MutableList?
                // Usamos toMutableList() para asegurar que sea mutable, si no lo es ya.
                lPedidos = dbCuenta?.getLineasByPedido(filterQuery)!!
                handlerBusqueda.sendEmptyMessage(0) // Update UI

            } catch (e: Exception) {
                Log.e(TAG, "Exception in onServiceConnected", e)
                isBound = false // Error al conectar
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
            dbCuenta = null
            isBound = false
        }
    }

    @SuppressLint("HandlerLeak")
    private val handlerBusqueda = Handler(Looper.getMainLooper()) {
        rellenarLista()
        true // Indicate message was handled
    }

    /**
     * Populates the ListView with the current data in lPedidos.
     */
    private fun rellenarLista() {
        val recyclerView = findViewById<RecyclerView>(R.id.lstPedidosPendientes) // Cambia a RecyclerView
        if (recyclerView == null) {
            return
        }

        // Configura el LayoutManager (cómo se muestran los ítems: vertical, horizontal, grid)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Asigna tu adaptador de RecyclerView
        val adaptador = AdaptadorBuscarPedidos(this, lPedidos)
        recyclerView.adapter = adaptador
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_pedidos)

        server = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }

        val textView = findViewById<TextView>(R.id.textBuscador)
        if (textView == null) {
            finish()
            return
        }
        txtBuscar = textView
        txtBuscar.addTextChangedListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (!isBound) { // Solo enlazar si no está enlazado y server está inicializado
            // Usa el nombre de clase correcto de tu Servicio
            val intent = Intent(applicationContext, ServiceCOM::class.java)
            intent.putExtra(EXTRA_URL, server)
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        } else  {
            // Si ya está enlazado, refrescar datos por si acaso
            dbCuenta?.getLineasByPedido(null)?.let {
                lPedidos = it // Asegurar que sea mutable
                handlerBusqueda.sendEmptyMessage(0)
            }
        }
    }

    override fun onDestroy() {
        if (isBound) {
            try {
                unbindService(mConexion)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Service was not registered?", e)
            }
            isBound = false
            myServicio = null
            dbCuenta = null
        }
        handlerBusqueda.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    /**
     * Handles the click on a "Servido" button associated with a list item.
     */
    fun clickServido(v: View) {
        val clickedJson = v.tag as? JSONObject
        if (clickedJson == null) {
            Log.e(TAG, "clickServido: View tag is null or not a JSONObject.")
            return
        }

        if (myServicio == null || dbCuenta == null) {
            Log.e(TAG, "clickServido: Service or DB not available.")
            return
        }

        try {
            val params = ContentValues().apply {
                put("art", clickedJson.toString())
                // Asegúrate de que "IDZona" es la clave correcta para obtener la zona del JSON
                put("idz", clickedJson.optString("IDZona", ""))
            }

            myServicio?.agregarInstruccion(Instruccion(params, "$server/pedidos/servido"))
            val josnObj = JSONArray()
            josnObj.put(clickedJson) // Asegúrate de que clickedJson es un JSONObject válido
            dbCuenta?.artServido(josnObj)
            clickedJson.put("servido", "1") // Actualiza el estado en el JSON
            rellenarLista() // Actualiza la lista para reflejar el cambio

            
        } catch (e: JSONException) {
            Log.e(TAG, "clickServido: JSONException ocurrió", e)
        } catch (e: Exception) {
            Log.e(TAG, "clickServido: Excepción ocurrió", e)
        }
    }
    // --- TextWatcher Implementation ---

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    // Asegúrate de que 'lPedidos' esté declarado como JSONArray en tu clase:
    // var lPedidos: JSONArray = JSONArray() // Si no lo está, decláralo así en la clase.

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val searchText = s?.toString()?.trim()

        thread {
            try {
                if ((searchText?.length ?: 0) > 0) {
                    // Pequeña pausa para evitar búsquedas excesivas mientras el usuario escribe
                    Thread.sleep(500)
                }

                // Accede al texto actual del EditText de forma segura en el hilo principal
                // y luego lo usas en el hilo de fondo
                val currentText = runCatching {
                    findViewById<TextView>(R.id.textBuscador).text?.toString()?.trim()
                }.getOrNull()

                if (searchText == currentText) { // Verifica si el texto ha cambiado durante la espera
                    if (!searchText.isNullOrBlank()) {
                        val filterSearch = "Descripcion LIKE '%$searchText%'"
                        // Asigna directamente el resultado, que es un JSONArray
                        lPedidos = dbCuenta?.getLineasByPedido(filterSearch) ?: JSONArray()

                    } else {
                        //creamos un flitro donde diga servido = 0

                        // Asigna directamente el resultado, que es un JSONArray
                        lPedidos = dbCuenta?.getLineasByPedido(filterQuery) ?: JSONArray()
                    }
                    handlerBusqueda.sendEmptyMessage(0) // Actualiza la UI con los nuevos datos
                } else {
                    Log.d(TAG, "Búsqueda cancelada, el texto cambió durante el retardo.")
                }

            } catch (e: InterruptedException) {
                Log.w(TAG, "Hilo de búsqueda interrumpido", e)
                Thread.currentThread().interrupt() // Vuelve a establecer la bandera de interrupción
            } catch (e: Exception) {
                Log.e(TAG, "Error en el hilo de búsqueda en segundo plano", e)
                // Cuando es un JSONArray, no usas .clear(). En su lugar, lo reinicias:
                lPedidos = JSONArray() // Reinicia lPedidos a un JSONArray vacío
                handlerBusqueda.sendEmptyMessage(0) // Actualiza la UI (ahora vacía)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {}
    // --- End TextWatcher Implementation ---
}