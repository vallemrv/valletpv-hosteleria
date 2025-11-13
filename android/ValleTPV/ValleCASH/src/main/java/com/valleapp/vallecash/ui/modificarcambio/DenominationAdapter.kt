package com.valleapp.vallecash.ui.modificarcambio

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R

data class Denomination(val name: String, val quantity: Int, var dispenseAmount: Int)

class DenominationAdapter(
    private val denominations: List<Denomination>,
    private val onAmountChanged: (Denomination, Int) -> Unit
) : RecyclerView.Adapter<DenominationAdapter.DenominationViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenominationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_denomination, parent, false)
        return DenominationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DenominationViewHolder, position: Int) {
        val denomination = denominations[position]

        // Limpiar el TextWatcher para evitar disparos múltiples
        holder.amountEditText.removeTextChangedListener(holder.textWatcher)

        // Asignar el valor actual del modelo al EditText
        val amountStr = if (denomination.dispenseAmount > 0) denomination.dispenseAmount.toString() else ""

        // Solo actualiza si el valor es diferente para evitar bucles
        if (holder.amountEditText.text.toString() != amountStr) {
            holder.amountEditText.setText(amountStr)
        }

        // Crear y asignar un nuevo TextWatcher
        holder.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val enteredAmount = s?.toString()?.toIntOrNull() ?: 0
                if (enteredAmount > denomination.quantity) {
                    denomination.dispenseAmount = 0
                    holder.amountEditText.setText("")
                } else {
                    if (denomination.dispenseAmount != enteredAmount) {
                        denomination.dispenseAmount = enteredAmount
                        onAmountChanged(denomination, enteredAmount)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // Volver a añadir el TextWatcher después de actualizar el valor
        holder.amountEditText.addTextChangedListener(holder.textWatcher)

        // Vincular los otros datos al ViewHolder
        holder.bind(denomination)
    }

    override fun getItemCount(): Int = denominations.size



    class DenominationViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        // Columnas del diseño
        private val nameTextView: TextView = view.findViewById(R.id.text_denomination_name)
        private val quantityTextView: TextView = view.findViewById(R.id.text_quantity_available)
        val amountEditText: EditText = view.findViewById(R.id.edit_amount)
        var textWatcher: TextWatcher? = null

        // Método para vincular los datos al ViewHolder
        fun bind(denomination: Denomination) {
            // Columna 1: Nombre de la denominación (Moneda/Billete)
            nameTextView.text = buildString {
                append(denomination.name)
                append("  €")
            }

            // Columna 2: Cantidad disponible
            quantityTextView.text = "${denomination.quantity}"

            // Columna 3: Valor inicial del EditText (puedes inicializar en 0 o un valor guardado)
            val amountStr = if (denomination.dispenseAmount > 0) denomination.dispenseAmount.toString() else ""
            amountEditText.setText(amountStr)
        }
    }
}
