package com.valleapp.vallecom.adaptadores


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecom.interfaces.IComanda
import com.valleapp.valletpv.R
import org.json.JSONException
import org.json.JSONObject

class AdaptadorPedidos(
    private val controler: IComanda,
    private val values: List<JSONObject>
) : RecyclerView.Adapter<AdaptadorPedidos.PedidosViewHolder>() {

    inner class PedidosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descripcionTextView: TextView = view.findViewById(R.id.lblDescripcion)
        val borrarButton: ImageButton = view.findViewById(R.id.btnBorrar)
        val lineaArtLayout: RelativeLayout = view.findViewById(R.id.btnArticuloPedido)
        val agregarButton: ImageButton = view.findViewById(R.id.btnAgregar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidosViewHolder {
        val view = LayoutInflater.from(controler.getContexto()).inflate(R.layout.item_pedido, parent, false)
        return PedidosViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidosViewHolder, position: Int) {
        try {
            val item = values[position]
            holder.descripcionTextView.text = item.getString("Descripcion")
            holder.borrarButton.tag = item
            holder.lineaArtLayout.tag = item
            holder.agregarButton.tag = item
            holder.borrarButton.setOnClickListener { it -> controler.borrarLinea(it) }
            holder.agregarButton.setOnClickListener { it -> controler.agregarLinea(it) }
        } catch (e: JSONException) {
            Log.e("AdaptadorPedidos", "Error al enlazar datos: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }


}
