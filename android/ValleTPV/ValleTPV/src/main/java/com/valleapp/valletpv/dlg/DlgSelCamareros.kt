package com.valleapp.valletpv.dlg

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ListView
import com.valleapp.valletpv.R
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSelCam
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpv.interfaces.IAutoFinish
import org.json.JSONException
import org.json.JSONObject

class DlgSelCamareros(
    context: Context,
    servicio: ServiceTPV?,
    mostrarAdd: Boolean,
    private val controlador: IAutoFinish
) : Dialog(context) {

    private val lsnoautorizados: ListView
    private val lstautorizados: ListView
    private var noautorizados: ArrayList<JSONObject> = ArrayList()
    private var autorizados: ArrayList<JSONObject> = ArrayList()

    init {
        setContentView(R.layout.dialog_elegir_camareros)

        // Hacer el diálogo fullscreen
        val window: Window? = window
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val s: ImageButton = findViewById(R.id.salir)

        if (!mostrarAdd) findViewById<ImageButton>(R.id.btn_add_nuevo_camarero).visibility = View.GONE

        lstautorizados = findViewById(R.id.lstautorizados)
        lsnoautorizados = findViewById(R.id.lstnoautorizados)

        s.setOnClickListener { cancel() }

        lsnoautorizados.setOnItemClickListener { _, view, _, _ ->
            try {
                val obj = view.tag as JSONObject
                autorizados.add(obj)
                noautorizados.remove(obj)
                obj.put("autorizado", "1")
                servicio?.autorizarCam(obj)
                val db = servicio?.getDb("camareros") as? DBCamareros
                db?.setAutorizado(obj.getInt("ID"), true)
                actualizarListas()
            } catch (e: JSONException) {
                Log.e("CAMAREROS", e.toString())
            }
        }

        lstautorizados.setOnItemClickListener { _, view, _, _ ->
            try {
                val obj = view.tag as JSONObject
                autorizados.remove(obj)
                noautorizados.add(obj)
                obj.put("autorizado", "0")
                servicio?.autorizarCam(obj)
                val db = servicio?.getDb("camareros") as? DBCamareros
                db?.setAutorizado(obj.getInt("ID"), false)
                actualizarListas()
            } catch (e: JSONException) {
                Log.e("CAMAREROS", e.toString())
            }
        }
    }

    fun getBtnOk(): ImageButton {
        return findViewById(R.id.aceptar)
    }

    fun setNoautorizados(ls: ArrayList<JSONObject>) {
        noautorizados = ls
        actualizarListas()
    }

    fun setAutorizados(ls: ArrayList<JSONObject>) {
        autorizados = ls
        actualizarListas()
    }

    private fun actualizarListas() {
        lstautorizados.adapter = AdaptadorSelCam(context, autorizados)
        lsnoautorizados.adapter = AdaptadorSelCam(context, noautorizados)
    }

    override fun onStop() {
        super.onStop()
        controlador.setEstadoAutoFinish(true, false)
    }
}
