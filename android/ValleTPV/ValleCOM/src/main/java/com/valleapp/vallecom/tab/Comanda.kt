package com.valleapp.vallecom.tab

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager // ¡Importa esto!
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecom.interfaces.IComanda
import com.valleapp.valletpv.R

class Comanda(private val controlador: IComanda) : Fragment() {

    private var listaPedidos: RecyclerView? = null
    private var canArt: TextView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.comanda, container, false)

        listaPedidos = view.findViewById(R.id.listaPedidoComanda)
        canArt = view.findViewById(R.id.numArt)

        listaPedidos?.layoutManager = LinearLayoutManager(context) // Usa 'context' o 'requireContext()'

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controlador.cargarNota()
    }

    @SuppressLint("SetTextI18n")
    fun setCantidad(can: String) {
        canArt?.text = "Hay $can articulos"
    }

    fun getListaPedidos(): RecyclerView? {
        return listaPedidos
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listaPedidos = null
        canArt = null
    }


}