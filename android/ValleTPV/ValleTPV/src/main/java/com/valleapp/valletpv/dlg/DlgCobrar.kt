package com.valleapp.valletpv.dlg

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.valleapp.valletpv.R
import com.valleapp.valletpv.interfaces.IControladorCuenta
import org.json.JSONArray
import java.util.Locale

class DlgCobrar(
        context: Context,
        private val controlador: IControladorCuenta,
        private val usaCashlogy: Boolean,
        private val usaTPVPC: Boolean
) : Dialog(context) {

    private var lineas: JSONArray = JSONArray()
    private var totalCobro: Double = 0.00
    private var entrega: Double = 0.00
    private var strEntrega: String = ""
    private val lblEntrega: TextView
    private val lblCambio: TextView
    private val lblTotal: TextView

    init {
        setContentView(R.layout.cobros)

        // Evitar que el diálogo se cierre al tocar fuera
        setCanceledOnTouchOutside(false)

        // Evitar que se cierre con el botón "Atrás"
        setCancelable(false)


        lblTotal = findViewById(R.id.lblPrecio)
        lblEntrega = findViewById(R.id.lblEntrega)
        lblCambio = findViewById(R.id.lblCambio)
        val tj: ImageButton = findViewById(R.id.btnTarjeta)
        val ef: ImageButton = findViewById(R.id.btnEfectivo)
        val s: ImageButton = findViewById(R.id.btn_salir_monedas)
        val pne: LinearLayout = findViewById(R.id.pneBotonera)

        pne.visibility = if (usaCashlogy) View.GONE else View.VISIBLE

        s.setOnClickListener { clickSalir() }
        ef.setOnClickListener { clickEfectivo() }
        tj.setOnClickListener { clickTarjeta() }
    }

    fun setDatos(lineas: JSONArray, totalCobro: Double) {
        this.lineas = lineas
        this.totalCobro = totalCobro
        this.entrega = totalCobro
        this.strEntrega = ""

        lblTotal.text = String.format(Locale.getDefault(), "%01.2f €", totalCobro)
        lblEntrega.text = String.format(Locale.getDefault(), "%01.2f €", totalCobro)

        val pne: LinearLayout = findViewById(R.id.pneBotonera)

        for (i in 0 until pne.childCount) {
            val layout = pne.getChildAt(i)
            if (layout is LinearLayout) {
                for (j in 0 until layout.childCount) {
                    val btn = layout.getChildAt(j)
                    if (btn is Button) {
                        btn.setOnClickListener { clickEntrega(btn) }
                    }
                }
            }
        }
    }

    private fun clickEfectivo() {
        if (usaCashlogy) {
            controlador.cobrarConCashlogy(lineas, totalCobro)
            clickSalir()
            return
        }
        if (entrega >= totalCobro) {
            clickSalir()
            controlador.cobrar(lineas, totalCobro, entrega, "")
        }
    }

    private fun clickTarjeta() {
        if (usaTPVPC) {
            controlador.cobrarConTpvPC(lineas, totalCobro)
            clickSalir()
            return
        }
        if (entrega == totalCobro) {
            clickSalir()
            controlador.cobrar(lineas, totalCobro, 0.00, "")
        }
    }

    private fun clickSalir() {
        cancel()
    }

    private fun clickEntrega(v: View) {
        val caracter = v.tag.toString()
        if (caracter == "C") {
            entrega = totalCobro
            strEntrega = ""
            lblEntrega.text = String.format(Locale.getDefault(), "%01.2f €", totalCobro)
            lblCambio.text = "0.00 €"
        } else {
            try {
                strEntrega += caracter
                entrega = strEntrega.toDouble()
                lblEntrega.text = String.format(Locale.getDefault(), "%01.2f €", entrega)
                if (entrega > totalCobro) {
                    lblCambio.text = String.format(Locale.getDefault(), "%01.2f €", entrega - totalCobro)
                }
            } catch (e: Exception) {
                entrega = totalCobro
                strEntrega = ""
                lblEntrega.text = String.format(Locale.getDefault(), "%01.2f €", totalCobro)
                lblCambio.text = "0.00 €"
            }
        }
    }

    override fun onStop() {
        controlador.setEstadoAutoFinish(true, false)
        super.onStop()
    }
}