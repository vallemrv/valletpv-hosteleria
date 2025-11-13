package com.valleapp.vallecom.adaptadores

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R
import org.json.JSONException
import org.json.JSONObject

class AdaptadorEmpresas(
    private val context: Context,
    private val values: List<JSONObject>,
    private val onEmpresaClick: (JSONObject) -> Unit,
    private val onBorrarClick: (Int) -> Unit,
    private val onEditarClick: (Int, JSONObject) -> Unit
) : RecyclerView.Adapter<AdaptadorEmpresas.EmpresasViewHolder>() {

    class EmpresasViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreTextView: TextView = view.findViewById(R.id.nombre_empresa_label)
        val iconoActivo: ImageView = view.findViewById(R.id.icon_empresa_activa)
        val botonBorrar: ImageButton = view.findViewById(R.id.boton_borrar_empresa)
        val botonEditar: ImageButton = view.findViewById(R.id.boton_editar_empresa)
        val itemEmpresa: LinearLayout = view.findViewById(R.id.item_empresa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresasViewHolder {
        return try {
            val view = LayoutInflater.from(context).inflate(R.layout.item_empresa, parent, false)
            EmpresasViewHolder(view)
        } catch (e: Exception) {
            Log.e("AdaptadorEmpresas", "Error inflating view: ${e.message}", e)
            Log.e("AdaptadorEmpresas", "Parent: ${parent.javaClass.name}, ViewType: $viewType")
            throw e // Re-lanzar la excepción para identificar el problema
        }
    }

    override fun onBindViewHolder(holder: EmpresasViewHolder, position: Int) {
        try {
            val empresa = values[position]
            holder.nombreTextView.text = empresa.getString("nombre")

            if (empresa.getBoolean("activo")) {
                holder.iconoActivo.visibility = View.VISIBLE
            } else {
                holder.iconoActivo.visibility = View.GONE
            }

            holder.botonBorrar.tag = position
            holder.botonBorrar.setOnClickListener {
                onBorrarClick(position)
            }

            holder.botonEditar.tag = position
            holder.botonEditar.setOnClickListener {
                onEditarClick(position, empresa)
            }

            holder.itemEmpresa.tag = empresa
            holder.itemEmpresa.setOnClickListener {
                onEmpresaClick(empresa)
            }
        } catch (e: JSONException) {
            Log.e("AdaptadorEmpresas", "Error al enlazar datos: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }
}
