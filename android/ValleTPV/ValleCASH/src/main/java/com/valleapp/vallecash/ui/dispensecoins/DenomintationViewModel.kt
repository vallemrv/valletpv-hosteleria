import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DenominationViewModel : ViewModel() {

    // Lista de denominaciones
    private val _denominations = MutableLiveData<List<Denomination>>().apply {
        value = listOf(
            Denomination("Moneda de 0.01 €", 0.01),
            Denomination("Moneda de 0.02 €", 0.02),
            Denomination("Moneda de 0.05 €", 0.05),
            Denomination("Moneda de 0.10 €", 0.10),
            Denomination("Moneda de 0.20 €", 0.20),
            Denomination("Moneda de 0.50 €", 0.50),
            Denomination("Moneda de 1 €", 1.00),
            Denomination("Moneda de 2 €", 2.00),
            Denomination("Billete de 5 €", 5.00),
            Denomination("Billete de 10 €", 10.00),
            Denomination("Billete de 20 €", 20.00)
        )
    }
    val denominations: LiveData<List<Denomination>> = _denominations

    // Cantidades ingresadas
    private val _amounts = MutableLiveData<MutableMap<Double, Int>>().apply {
        value = mutableMapOf()
    }
    val amounts: LiveData<MutableMap<Double, Int>> = _amounts

    // Actualizar la cantidad de una denominación
    fun updateAmount(value: Double, amount: Int) {
        _amounts.value?.set(value, amount)
    }
}
