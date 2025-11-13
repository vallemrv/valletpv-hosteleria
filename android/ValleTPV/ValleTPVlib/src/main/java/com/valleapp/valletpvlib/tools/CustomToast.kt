package com.tu.paquete // Ajusta el paquete según tu proyecto

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import com.valleapp.valletpvlib.R

class CustomToast(private val context: Context) {

    // Mostrar toast en el centro
    fun showCenter(texto: String, tiempo: Int = Toast.LENGTH_LONG) {
        showToast(texto, Gravity.CENTER, 0, 0, tiempo)
    }

    // Mostrar toast en la parte inferior
    fun showBottom(texto: String, tiempo: Int = Toast.LENGTH_LONG) {
        showToast(texto, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100, tiempo)
    }

    // Mostrar toast en la parte superior
    fun showTop(texto: String, tiempo: Int = Toast.LENGTH_LONG) {
        showToast(texto, Gravity.TOP, 0, 80, tiempo)
    }

    // Método privado que hace el trabajo pesado
    private fun showToast(
        texto: String,
        gravity: Int,
        xOffset: Int,
        yOffset: Int,
        tiempo: Int,
        @LayoutRes layoutId: Int = R.layout.texto_toast_simple // Layout por defecto
    ) {
        val toast = Toast(context)
        val toastView: View = LayoutInflater.from(context).inflate(layoutId, null)
        val textView: TextView = toastView.findViewById(R.id.txt_label)
        textView.text = texto
        toast.view = toastView
        toast.duration = tiempo
        toast.setGravity(gravity, xOffset, yOffset)
        toast.show()
    }
}