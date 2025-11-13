package com.valleapp.vallecom.activitys

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.valleapp.vallecom.adaptadores.AdaptadorSugerencias
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.db.DBSugerencias
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Sugerencias : ActivityBase(), TextWatcher {

    private lateinit var art: JSONObject
    private lateinit var txtSug: TextView
    private val dbSugerencias = DBSugerencias(this)
    private var lsBusqueda = JSONArray()
    private var sugerenciasPadre = JSONArray() // Variable global para guardar sugerencias del padre

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            rellenarSug(lsBusqueda)
        }
    }

    private fun rellenarSug(p: JSONArray) {
        try {
            val lsug = ArrayList<JSONObject>()
            for (i in 0 until p.length()) {
                lsug.add(p.getJSONObject(i))
            }
            val lst = findViewById<ListView>(R.id.lstSugerencias)
            lst.adapter = AdaptadorSugerencias(cx, lsug)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sugerencias)
        txtSug = findViewById(R.id.txtSugerencias)
        txtSug.addTextChangedListener(this)

        server = intent.extras!!.getString("url")!!

        try {
            art = JSONObject(intent.extras!!.getString("art")!!)
            val l = findViewById<TextView>(R.id.title)
            val titulo = "Sugerencia para " + art.getString("Nombre")
            l.text = titulo
            // Obtener los IDs y filtrar las sugerencias
            val parentId = art.optString("IDParentTecla", "-1")
            val idTecla = art.getString("ID")

            val sqlWhereMain = "IDTecla = $idTecla"
            val arrayMain = dbSugerencias.filter(sqlWhereMain)

            if (parentId != "-1") {
                val sqlWhereParent = "IDTecla = $parentId"
                sugerenciasPadre = dbSugerencias.filter(sqlWhereParent) // Guardar sugerencias del padre

                // Crear un nuevo JSONArray para la combinación con padre primero
                val combinedArray = JSONArray()

                // Añadir primero todos los elementos del padre
                for (i in 0 until sugerenciasPadre.length()) {
                    combinedArray.put(sugerenciasPadre.getJSONObject(i))
                }

                // Después añadir todos los elementos del hijo
                for (i in 0 until arrayMain.length()) {
                    combinedArray.put(arrayMain.getJSONObject(i))
                }

                rellenarSug(combinedArray)
            } else {
                rellenarSug(arrayMain)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }



    fun onClickItemSimple(v: View) {
        aceptarSug(v.tag as JSONObject)
    }

    fun aceptarSug(sug: JSONObject) {
        val it = intent
        it.putExtra("art", art.toString())
        it.putExtra("sug", sug.getString("sugerencia"))
        it.putExtra("incremento", sug.getDouble("incremento"))
        setResult(RESULT_OK, it)
        finish()
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

    override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
        if (charSequence.isNotEmpty()) {
            try {
                if (charSequence.toString().contains("\n")) {
                    val p = ContentValues()
                    val sug = charSequence.toString().replace("\n", "")
                    p.put("sug", sug)
                    p.put("idArt", art.getString("ID"))
                    p.put("uid", myServicio?.getUid())
                    HTTPRequest("$server/sugerencias/add", p, "", null)
                    txtSug.visibility = View.GONE
                    val item = JSONObject()
                    item.put("sugerencia", sug)
                    item.put("incremento", 0.0)
                    aceptarSug(item)
                } else if (!charSequence.toString().contains("\n")) {
                    val cWhere = "IDTecla = " + art.getString("ID") + " AND sugerencia LIKE '%" + charSequence + "%'"
                    Thread {
                        try {
                            Thread.sleep(1000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                        // Crear array combinado con padre arriba
                        val combinedBusqueda = JSONArray()

                        // Añadir primero las sugerencias del padre (siempre presentes)
                        for (i in 0 until sugerenciasPadre.length()) {
                            combinedBusqueda.put(sugerenciasPadre.getJSONObject(i))
                        }

                        // Filtrar y añadir sugerencias del hijo que coincidan con la búsqueda
                        val resultadoBusqueda = dbSugerencias.filter(cWhere)
                        for (i in 0 until resultadoBusqueda.length()) {
                            combinedBusqueda.put(resultadoBusqueda.getJSONObject(i))
                        }

                        lsBusqueda = combinedBusqueda
                        handler.sendEmptyMessage(0)
                    }.start()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun afterTextChanged(editable: Editable) {}
}
