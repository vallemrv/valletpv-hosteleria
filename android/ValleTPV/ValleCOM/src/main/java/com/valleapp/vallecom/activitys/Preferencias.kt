package com.valleapp.vallecom.activitys

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecom.adaptadores.AdaptadorEmpresas
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Preferencias : ActivityBase() {

    private lateinit var lista: JSONArray
    private  var empresaActiva: JSONObject? = null
    private var crearEmpresaDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)
        Log.d("Preferencias", "onCreate iniciado")

        empresaActiva = cargarEmpresaActiva()
        val obj = cargarListado()
        rellenarLista(obj)

        val fabAddEmpresa = findViewById<View>(R.id.fab_add_empresa)
        fabAddEmpresa.setOnClickListener {
            clickAddEmpresa(null)
        }
    }

    private fun rellenarLista(obj: JSONObject?) {
        if (obj != null) {
            try {
                lista = obj.getJSONArray("lista")
                val lempresa = ArrayList<JSONObject>()
                for (i in 0 until lista.length()) {
                    val empresa = lista.getJSONObject(i)
                    if (empresaActiva != null && empresaActiva?.getString("URL") == empresa.getString("URL")) {
                        empresa.put("activo", true)
                    } else {
                        empresa.put("activo", false)
                    }
                    lempresa.add(lista.getJSONObject(i))
                }

                val recyclerView = findViewById<RecyclerView>(R.id.lstEmpresa)
                recyclerView.layoutManager = LinearLayoutManager(cx)
                recyclerView.adapter = AdaptadorEmpresas(cx, lempresa,
                    onEmpresaClick = { empresa -> clickSelEmpresa(empresa) },
                    onBorrarClick = { position -> borrarEmpresa(position) },
                    onEditarClick = { position, empresa -> clickAddEmpresa(empresa) }
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun borrarEmpresa(position: Int) {
        try {
            val json = JSON()
            lista.remove(position)
            val obj = JSONObject()
            obj.put("lista", lista)
            json.serializar("lista_empresas.dat", obj, cx)
            rellenarLista(obj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clickSelEmpresa(empresa: JSONObject) {
        try {
            val url = empresa.getString("URL")
            val nombre = empresa.getString("nombre")
            val uid = empresa.getString("uid")
            val alias = empresa.optString("alias", "") // Obtener alias si existe
            val json = JSON()
            if (empresaActiva == null) empresaActiva = JSONObject()
            empresaActiva?.put("URL", url)
            empresaActiva?.put("nombre", nombre)
            empresaActiva?.put("uid", uid)
            empresaActiva?.put("alias", alias) // Guardar alias
            json.serializar("preferencias.dat", empresaActiva, cx)
            val toast = Toast.makeText(applicationContext, "Cambios guardados con exito", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            toast.show()
            val obj = JSONObject()
            obj.put("lista", lista)
            rellenarLista(obj)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun clickAddEmpresa(empresaEditar: JSONObject?) {
        crearEmpresaDialog = Dialog(cx)
        crearEmpresaDialog?.setTitle(if (empresaEditar == null) "Crear empresa" else "Editar empresa")
        crearEmpresaDialog?.setContentView(R.layout.dialog_crear_empresa)

        val txtUrl = crearEmpresaDialog?.findViewById<EditText>(R.id.txt_URL)
        val txtNombre = crearEmpresaDialog?.findViewById<EditText>(R.id.txt_nombre_empresa)
        val txtAlias = crearEmpresaDialog?.findViewById<EditText>(R.id.txt_alias)
        val btnAgregar = crearEmpresaDialog?.findViewById<Button>(R.id.add_empresa)

        // Si estamos editando, prellenar los campos
        if (empresaEditar != null) {
            try {
                txtNombre?.setText(empresaEditar.getString("nombre"))
                txtUrl?.setText(empresaEditar.getString("URL"))
                txtAlias?.setText(empresaEditar.optString("alias", ""))
                btnAgregar?.text = "Actualizar"
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        btnAgregar?.setOnClickListener {
            val urlText = txtUrl?.text.toString().trim()
            val nombreText = txtNombre?.text.toString().trim()
            val aliasText = txtAlias?.text.toString().trim()

            if (urlText.isNotEmpty() && nombreText.isNotEmpty() && aliasText.isNotEmpty()) {
                validarYCrearEmpresa(urlText, nombreText, aliasText, empresaEditar)
            } else {
                Toast.makeText(cx, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        crearEmpresaDialog?.show()
    }

    private fun validarYCrearEmpresa(url: String, nombre: String, alias: String, empresaEditar: JSONObject?) {
        // Normalizar URL agregando /api si no existe
        val urlNormalizada = normalizarUrl(url)

        // Paso 1: Validar conexión con /health
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val bundle = msg.data
                val response = bundle.getString("RESPONSE")
                val op = bundle.getString("op")
                val codigoEstado = bundle.getInt("codigoEstado")
                val connectionStatus = bundle.getString("connectionStatus")

                if (op == "validarHealth") {
                    if (connectionStatus == "server_response" && codigoEstado == 200) {
                        try {
                            val jsonResponse = JSONObject(response ?: "{}")
                            val success = jsonResponse.optBoolean("success", false)

                            if (success) {
                                // Conexión exitosa, proceder a crear/actualizar empresa
                                crearEmpresaConUid(urlNormalizada, nombre, alias, empresaEditar)
                            } else {
                                mostrarErrorYReabrirDialog(urlNormalizada, nombre, alias,
                                    "El servidor no respondió correctamente. Verifique la URL.", empresaEditar)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            mostrarErrorYReabrirDialog(urlNormalizada, nombre, alias,
                                "Error al validar la conexión con el servidor.", empresaEditar)
                        }
                    } else {
                        mostrarErrorYReabrirDialog(urlNormalizada, nombre, alias,
                            "No se pudo conectar con el servidor. Verifique la URL e intente nuevamente.", empresaEditar)
                    }
                }
            }
        }

        Toast.makeText(cx, "Validando conexión con el servidor...", Toast.LENGTH_SHORT).show()
        HTTPRequest("$urlNormalizada/health", ContentValues(), "validarHealth", handler)
    }

    private fun crearEmpresaConUid(url: String, nombre: String, alias: String, empresaEditar: JSONObject?) {
        val params = ContentValues()
        params.put("alias", alias)

        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val bundle = msg.data
                val response = bundle.getString("RESPONSE")
                val op = bundle.getString("op")
                val codigoEstado = bundle.getInt("codigoEstado")
                val connectionStatus = bundle.getString("connectionStatus")

                if (op == "crearUid") {
                    if (connectionStatus == "server_response" && codigoEstado == 200) {
                        try {
                            val jsonResponse = JSONObject(response ?: "")
                            val uid = jsonResponse.getString("uid")

                            if (empresaEditar != null) {
                                // Actualizar empresa existente
                                actualizarEmpresa(empresaEditar, url, nombre, alias, uid)
                            } else {
                                // Crear nueva empresa
                                guardarEmpresaYActivar(url, nombre, alias, uid)
                            }

                            crearEmpresaDialog?.dismiss()
                            crearEmpresaDialog = null
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            mostrarErrorYReabrirDialog(url, nombre, alias,
                                "Error al procesar la respuesta del servidor.", empresaEditar)
                        }
                    } else {
                        mostrarErrorYReabrirDialog(url, nombre, alias,
                            "Error al crear el UID. Código: $codigoEstado", empresaEditar)
                    }
                }
            }
        }

        Toast.makeText(cx, "Creando identificador único...", Toast.LENGTH_SHORT).show()
        HTTPRequest("$url/dispositivo/create_uid", params, "crearUid", handler)
    }

    private fun guardarEmpresaYActivar(url: String, nombre: String, alias: String, uid: String) {
        try {
            val obj = JSONObject()
            val json = JSON()
            obj.put("URL", url)
            obj.put("nombre", nombre)
            obj.put("uid", uid)
            obj.put("alias", alias)

            if (!::lista.isInitialized) {
                lista = JSONArray()
            }
            lista.put(obj)

            val objLista = JSONObject()
            objLista.put("lista", lista)
            json.serializar("lista_empresas.dat", objLista, cx)
            rellenarLista(objLista)

            // Activar la empresa
            val empresaActiva = JSONObject()
            empresaActiva.put("URL", url)
            empresaActiva.put("nombre", nombre)
            empresaActiva.put("uid", uid)
            empresaActiva.put("alias", alias)
            json.serializar("preferencias.dat", empresaActiva, cx)

            val toast = Toast.makeText(applicationContext, "Empresa creada y activada con éxito", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            toast.show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(cx, "Error al guardar la empresa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarEmpresa(empresaEditar: JSONObject, url: String, nombre: String, alias: String, uid: String) {
        try {
            // Buscar y actualizar la empresa en la lista
            for (i in 0 until lista.length()) {
                val empresa = lista.getJSONObject(i)
                if (empresa.getString("uid") == empresaEditar.getString("uid")) {
                    empresa.put("URL", url)
                    empresa.put("nombre", nombre)
                    empresa.put("alias", alias)
                    empresa.put("uid", uid)
                    break
                }
            }

            // Guardar la lista actualizada
            val objLista = JSONObject()
            objLista.put("lista", lista)
            val json = JSON()
            json.serializar("lista_empresas.dat", objLista, cx)

            // Si la empresa editada es la activa, actualizar preferencias
            if (empresaActiva?.optString("uid") == empresaEditar.getString("uid")) {
                empresaActiva?.put("URL", url)
                empresaActiva?.put("nombre", nombre)
                empresaActiva?.put("alias", alias)
                empresaActiva?.put("uid", uid)
                json.serializar("preferencias.dat", empresaActiva, cx)
            }

            rellenarLista(objLista)

            val toast = Toast.makeText(applicationContext, "Empresa actualizada con éxito", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            toast.show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(cx, "Error al actualizar la empresa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarErrorYReabrirDialog(url: String, nombre: String, alias: String, mensajeError: String, empresaEditar: JSONObject?) {
        Toast.makeText(cx, mensajeError, Toast.LENGTH_LONG).show()

        // Si el diálogo ya está visible, no lo recreamos.
        if (crearEmpresaDialog?.isShowing == true) {
            return
        }

        // Reabrir el diálogo con los datos anteriores
        crearEmpresaDialog = Dialog(cx)
        crearEmpresaDialog?.setTitle(if (empresaEditar == null) "Crear empresa" else "Editar empresa")
        crearEmpresaDialog?.setContentView(R.layout.dialog_crear_empresa)

        val txtUrl = crearEmpresaDialog?.findViewById<EditText>(R.id.txt_URL)
        val txtNombre = crearEmpresaDialog?.findViewById<EditText>(R.id.txt_nombre_empresa)
        val txtAlias = crearEmpresaDialog?.findViewById<EditText>(R.id.txt_alias)
        val btnAgregar = crearEmpresaDialog?.findViewById<Button>(R.id.add_empresa)

        txtUrl?.setText(url)
        txtNombre?.setText(nombre)
        txtAlias?.setText(alias)

        if (empresaEditar != null) {
            btnAgregar?.text = "Actualizar"
        }

        btnAgregar?.setOnClickListener {
            val newUrl = txtUrl?.text.toString().trim()
            val newNombre = txtNombre?.text.toString().trim()
            val newAlias = txtAlias?.text.toString().trim()

            if (newUrl.isNotEmpty() && newNombre.isNotEmpty() && newAlias.isNotEmpty()) {
                validarYCrearEmpresa(newUrl, newNombre, newAlias, empresaEditar)
            } else {
                Toast.makeText(cx, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        crearEmpresaDialog?.show()
    }

    private fun normalizarUrl(url: String): String {
        // Eliminar barras finales
        var urlNormalizada = url.trimEnd('/')

        // Si la URL no termina con /api, agregarla
        if (!urlNormalizada.endsWith("/api")) {
            urlNormalizada = "$urlNormalizada/api"
        }

        return urlNormalizada
    }

    private fun migrarUrlsEnLista(objLista: JSONObject): Boolean {
        var cambiosRealizados = false
        try {
            val lista = objLista.getJSONArray("lista")
            for (i in 0 until lista.length()) {
                val empresa = lista.getJSONObject(i)
                val urlActual = empresa.getString("URL")
                val urlNormalizada = normalizarUrl(urlActual)

                if (urlActual != urlNormalizada) {
                    empresa.put("URL", urlNormalizada)
                    cambiosRealizados = true
                    Log.d("Preferencias", "URL migrada: $urlActual -> $urlNormalizada")
                }
            }
        } catch (e: Exception) {
            Log.e("Preferencias", "Error al migrar URLs en lista", e)
        }
        return cambiosRealizados
    }

    private fun cargarListado(): JSONObject? {
        val json = JSON()
        return try {
            val objLista = json.deserializar("lista_empresas.dat", this)

            // Migrar URLs si es necesario
            if (objLista != null && migrarUrlsEnLista(objLista)) {
                json.serializar("lista_empresas.dat", objLista, this)
                Log.d("Preferencias", "Lista de empresas migrada y guardada")
            }

            objLista
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    private fun cargarEmpresaActiva(): JSONObject? {
        val json = JSON()
        return try {
            val empresaActiva = json.deserializar("preferencias.dat", this)

            // Migrar URL si es necesario
            if (empresaActiva != null) {
                val urlActual = empresaActiva.optString("URL", "")
                if (urlActual.isNotEmpty()) {
                    val urlNormalizada = normalizarUrl(urlActual)
                    if (urlActual != urlNormalizada) {
                        empresaActiva.put("URL", urlNormalizada)
                        json.serializar("preferencias.dat", empresaActiva, this)
                        Log.d("Preferencias", "Empresa activa migrada: $urlActual -> $urlNormalizada")
                    }
                }
            }

            empresaActiva
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }


    override fun onPause() {
        finish()
        super.onPause()
    }
}