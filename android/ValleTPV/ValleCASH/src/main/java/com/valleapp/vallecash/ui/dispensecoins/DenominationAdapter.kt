import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecash.R

data class Denomination(val name: String, val value: Double)

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
        holder.bind(denomination)

        holder.amountEditText.setOnFocusChangeListener { _, _ ->
            val enteredAmount = holder.amountEditText.text.toString().toIntOrNull() ?: 0
            onAmountChanged(denomination, enteredAmount) // Notificar al ViewModel del cambio
        }
    }

    override fun getItemCount(): Int = denominations.size

    class DenominationViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_denomination_name)
        val amountEditText: EditText = view.findViewById(R.id.edit_amount)

        fun bind(denomination: Denomination) {
            nameTextView.text = denomination.name
            amountEditText.setText("0")
        }
    }
}
