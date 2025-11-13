package com.valleapp.vallecom.adaptadores

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R
import org.json.JSONException
import org.json.JSONArray
import org.json.JSONObject

class AdaptadorBuscarPedidos(
    private val context: Context,
    private val values: JSONArray
) : RecyclerView.Adapter<AdaptadorBuscarPedidos.PedidosViewHolder>() {

    inner class PedidosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreTextView: TextView = view.findViewById(R.id.lblNombre)
        val cantidadTextView: TextView = view.findViewById(R.id.lblCantidad)
        val borrarButton: ImageButton = view.findViewById(R.id.btnBorrarPedido)
        val lineaArtLayout: LinearLayout = view.findViewById(R.id.btnReenviarLinea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidosViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.linea_pedido_externo, parent, false)
        return PedidosViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidosViewHolder, position: Int) {
        try {
            val item: JSONObject = values.getJSONObject(position)
            holder.nombreTextView.text = buildString {
                append(item.getString("Descripcion"))
                append(" - ")
                append(item.getString("nomMesa"))
            }
            holder.cantidadTextView.text = item.getString("Can")

            if (item.getString("servido") != "0") {
                holder.borrarButton.visibility = View.GONE
            } else {
                holder.borrarButton.visibility = View.VISIBLE
                holder.borrarButton.tag = item
            }

            // Guarda el item en el tag de lineaArtLayout
            holder.lineaArtLayout.tag = item
        } catch (e: JSONException) {
            Log.e("AdaptadorBuscarPedidos", "Error al enlazar datos: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        return values.length()
    }


}
