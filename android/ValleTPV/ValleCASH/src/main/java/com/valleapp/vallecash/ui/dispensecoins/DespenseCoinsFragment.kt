import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.vallecash.R
import com.valleapp.vallecash.databinding.FragmentDispenseCoinsBinding


class DispenseCoinsFragment : Fragment() {

    // Obtener el ViewModel
    private val viewModel: DenominationViewModel by activityViewModels()
    private var _binding: FragmentDispenseCoinsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dispense_coins, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /// Acceder al RecyclerView usando view.findViewById()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_denominations)

        // Configurar el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observar las denominaciones en el ViewModel
        viewModel.denominations.observe(viewLifecycleOwner, Observer { denominations ->
            val adapter = DenominationAdapter(denominations) { denomination, amount ->
                viewModel.updateAmount(denomination.value, amount) // Actualizar las cantidades en el ViewModel
            }
            recyclerView.adapter = adapter
        })

        // Acceder a los botones usando view.findViewById()
        val buttonDispenseSelected = view.findViewById<Button>(R.id.button_dispense_selected)
        val buttonRefresh = view.findViewById<Button>(R.id.button_refresh)
        val buttonExit = view.findViewById<Button>(R.id.button_exit)

        // Configurar el botón "Dispensar seleccionado"
        buttonDispenseSelected.setOnClickListener {
            val amounts = viewModel.amounts.value
            // Aquí manejas la lógica para dispensar las monedas seleccionadas usando amounts
        }

        // Configurar el botón "Refrescar cambio"
        buttonRefresh.setOnClickListener {
            // Aquí actualizas la interfaz o los valores
        }

        // Configurar el botón "Salir"
        buttonExit.setOnClickListener {
            requireActivity().finish()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
