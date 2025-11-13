package com.valleapp.valletpv.dlg

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.valleapp.valletpv.R
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSepararTicket
import com.valleapp.valletpv.interfaces.IControladorCuenta
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class DlgSepararTicket(context: Context, private val controlador: IControladorCuenta) : Dialog(context) {

    private var totalCobro = 0.00
    private val lstArt: ListView
    private val lineasTicket = arrayListOf<JSONObject>()
    private val separados = arrayListOf<JSONObject>()

    init {
        setContentView(R.layout.separarticket)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        // Hacer el diálogo fullscreen
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val tot: TextView = findViewById(R.id.lblTotalCobro)
        val ok: ImageButton = findViewById(R.id.btn_guardar_preferencias)
        val s: ImageButton = findViewById(R.id.btn_salir_monedas)
        val lstCobros: ListView = findViewById(R.id.lstCobros)
        lstArt = findViewById(R.id.lstArticulos)

        ok.setOnClickListener { clickCobrarSeparados() }
        s.setOnClickListener { clickSalirSeparados() }

        tot.text = String.format(Locale.getDefault(), "Total cobro %01.2f €", totalCobro)

        lstArt.setOnItemClickListener { _, _, position, _ ->
            try {
                val art = lineasTicket[position] // Get data from list, not view.tag
                val can = art.getInt("Can")
                val canCobro = art.getInt("CanCobro") + 1

                if (canCobro <= can) {
                    totalCobro += art.getDouble("Precio")
                    tot.text = String.format(Locale.getDefault(), "Total cobro %01.2f €", totalCobro)
                    art.put("CanCobro", canCobro)

                    if (can == canCobro) lineasTicket.remove(art)
                    if (canCobro == 1) separados.add(art)

                    lstCobros.adapter = AdaptadorSepararTicket(context, separados, true)
                    lstArt.adapter = AdaptadorSepararTicket(context, lineasTicket, false)
                }
            } catch (e: JSONException) {
                Log.e("DlgSepararTicket", "Error al procesar artículo en lstArt, position $position: ${e.message}", e)
            }
        }

        lstCobros.setOnItemClickListener { _, _, position, _ ->
            try {
                val art = separados[position] // Get data from list, not view.tag
                val can = art.getInt("Can")
                val canCobro = art.getInt("CanCobro") - 1

                totalCobro -= art.getDouble("Precio")
                tot.text = String.format(Locale.getDefault(), "Total cobro %01.2f €", totalCobro)
                art.put("CanCobro", canCobro)

                if (can > canCobro && !lineasTicket.contains(art)) {
                    lineasTicket.add(art)
                }
                if (canCobro == 0) separados.remove(art)

                lstCobros.adapter = AdaptadorSepararTicket(context, separados, true)
                lstArt.adapter = AdaptadorSepararTicket(context, lineasTicket, false)
            } catch (e: JSONException) {
                Log.e("DlgSepararTicket", "Error al procesar artículo en lstCobros, position $position: ${e.message}", e)
            }
        }
    }

    fun setLineasTicket(lsart: List<JSONObject>) {
        lineasTicket.clear()
        lineasTicket.addAll(lsart)
        lstArt.adapter = AdaptadorSepararTicket(context, lineasTicket, false)
    }

    private fun clickCobrarSeparados() {
        val arts = JSONArray()
        for (art in separados) {
            try {
                art.put("Can", art.getString("CanCobro"))
                arts.put(art)
            } catch (e: JSONException) {
                Log.e("DlgSepararTicket", "Error al preparar cobro: ${e.message}", e)
            }
        }
        cancel()
        controlador.mostrarCobrar(arts, totalCobro, true)
    }

    private fun clickSalirSeparados() {
        cancel()
    }

    override fun onStop() {
        super.onStop()
        controlador.setEstadoAutoFinish(true, false)
    }
}