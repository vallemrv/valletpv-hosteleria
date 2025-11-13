package com.valleapp.valletpv.tools

import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.valleapp.valletpv.R
import java.util.Locale

class ToastShowInfoCuenta {

    private val SHORT_TOAST_DURATION: Long = 2000

    private var t: CountDownTimer? = null

    fun show(entrega: Double, cambio: Double, durationInMillis: Long, cx: Context, l: View) {
        val info = l.findViewById<LinearLayout>(R.id.layout_info)
                info.visibility = View.GONE

        val txtCambio = l.findViewById<TextView>(R.id.txt_info_cambio)
                txtCambio.text = String.format(Locale.getDefault(),"%01.2f €", cambio)

        val txtEntrega = l.findViewById<TextView>(R.id.txt_info_entrega)
                txtEntrega.text = String.format(Locale.getDefault(), "%01.2f €", entrega)

        if (t == null) {
            t = object : CountDownTimer(durationInMillis, SHORT_TOAST_DURATION) {
                override fun onFinish() {
                    // Opcional: puedes agregar algo al finalizar
                }

                override fun onTick(millisUntilFinished: Long) {
                    val toast = Toast(cx)
                    toast.view = l
                    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                }
            }
            t?.start()
        }
    }

    fun cancel() {
        t?.cancel()
    }

    // Nueva función para mostrar solo un string en el Toast y ocultar las otras vistas
    fun showMessageOnly(message: String, durationInMillis: Long, cx: Context, l: View) {

        val info = l.findViewById<LinearLayout>(R.id.layout_info)
        info.visibility = View.VISIBLE

        val txtCambio = l.findViewById<LinearLayout>(R.id.layout_cambio)
                txtCambio.visibility = View.GONE  // Ocultar el campo de cambio

        val txtEntrega = l.findViewById<LinearLayout>(R.id.layout_entrega)
                txtEntrega.visibility = View.GONE  // Ocultar el campo de entrega


        val labelInfo = l.findViewById<TextView>(R.id.label_info)
                labelInfo.text = message


        if (t == null) {
            t = object : CountDownTimer(durationInMillis, SHORT_TOAST_DURATION) {
                override fun onFinish() {
                    // Opcional: agregar lógica al finalizar
                }

                override fun onTick(millisUntilFinished: Long) {
                    val toast = Toast(cx)
                    toast.view = l
                    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                }
            }
            t?.start()
        }
    }
}
