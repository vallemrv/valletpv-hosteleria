package com.valleapp.vallecom.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import androidx.fragment.app.Fragment // Use androidx fragment
import com.valleapp.vallecom.interfaces.IPedidos
import com.valleapp.valletpv.R


/**
 * Created by valle on 28/10/14.
 *
 * Translated to Kotlin.
 */
class ListaMesas(
    val controler: IPedidos
) : Fragment() {
    // Store instance variables
    var pneMesas: TableLayout? = null // Make it nullable as it's initialized later
    var pneZonas: LinearLayout? = null
    private var btnSendMensaje: com.getbase.floatingactionbutton.FloatingActionButton? = null


    // Inflate the view for the fragment based on layout XML
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.mesas, container, false)
        pneMesas = view.findViewById(R.id.pneMesas) // Assign the found view to the property
        pneZonas = view.findViewById(R.id.pneZonas)
        btnSendMensaje = view.findViewById(R.id.btn_send_mensaje)
        return view
    }

    /**
     * Adds a view to the TableLayout.
     * @param b The view to add.
     * @param layout Layout parameters for the view.
     */
    fun addView(b: View, layout: ViewGroup.LayoutParams) {
        pneMesas?.addView(b, layout) // Use safe call operator ?. for null check
    }

    /**
     * Clears all views from the TableLayout.
     */
    fun clearTable() {
        pneMesas?.removeAllViews() // Use safe call operator ?. for null check
    }

    /**
     * Cambia el icono del botón de mensajes según los permisos del camarero.
     * @param tieneComandosVoz true si el camarero tiene permisos de comandos de voz
     */
    fun cambiarIconoMensajes(tieneComandosVoz: Boolean) {
        println("BtnSendMensaje: $btnSendMensaje, tieneComandosVoz: $tieneComandosVoz")
        btnSendMensaje?.let { btn ->
            if (tieneComandosVoz) {
                btn.setIcon(android.R.drawable.ic_btn_speak_now) // Icono de micrófono
            } else {
                btn.setIcon(com.valleapp.valletpvlib.R.drawable.send_men) // Icono original de enviar mensaje
            }
        }
    }

    override fun onResume() {
        super.onResume()
        controler.rellenarZonas()
    }
}
