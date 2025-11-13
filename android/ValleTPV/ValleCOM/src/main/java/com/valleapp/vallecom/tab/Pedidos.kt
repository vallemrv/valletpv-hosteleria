package com.valleapp.vallecom.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.valleapp.vallecom.interfaces.IPedidos
import com.valleapp.valletpv.R

/**
 * Created by valle on 28/10/14.
 */
class Pedidos(
    val controler: IPedidos
) : Fragment() {

    var contenedor: LinearLayout? = null
    var contenedorReceptores: LinearLayout? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.pedidos, container, false)
        contenedor = v.findViewById(R.id.listasPedidosNoServidos)
        contenedorReceptores = v.findViewById(R.id.pneReceptores)
        return v
    }

    override fun onResume() {
        super.onResume()
        controler.rellenarReceptores()
    }


}