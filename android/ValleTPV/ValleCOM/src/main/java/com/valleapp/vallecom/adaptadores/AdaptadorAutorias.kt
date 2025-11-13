package com.valleapp.vallecom.adaptadores

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecom.activitys.Autorias
import com.valleapp.valletpv.R
import org.json.JSONObject

class AdaptadorAutorias(
    private val context: Context,
    private val values: List<JSONObject>
) : RecyclerView.Adapter<AdaptadorAutorias.AutoriasViewHolder>() {

    inner class AutoriasViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mensajeTextView: TextView = view.findViewById(R.id.txt_mensaje_autoria)
        val autorizarButton: ImageButton = view.findViewById(R.id.btn_autorizar)
        val denegarButton: ImageButton = view.findViewById(R.id.btn_denegar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoriasViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.linea_autoria, parent, false)
        return AutoriasViewHolder(view)
    }

    override fun onBindViewHolder(holder: AutoriasViewHolder, position: Int) {
        try {
            val item = values[position]
            holder.mensajeTextView.text = item.getString("mensaje")

            if (item.getString("tipo") != "informacion") {
                holder.autorizarButton.tag = item
                holder.autorizarButton.visibility = View.VISIBLE
                holder.autorizarButton.setOnClickListener { view ->
                    // Lógica para manejar el clic del botón autorizar
                    (context as? Autorias)?.sendAutorizacion(view)
                }
            } else {
                holder.autorizarButton.visibility = View.GONE
            }

            holder.denegarButton.tag = item
            holder.denegarButton.setOnClickListener { view ->
                // Lógica para manejar el clic del botón denegar
                (context as? Autorias)?.sendCancelacion(view)
            }
        } catch (e: Exception) {
            Log.e("AdaptadorAutorias", "Error al enlazar datos: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }
}
