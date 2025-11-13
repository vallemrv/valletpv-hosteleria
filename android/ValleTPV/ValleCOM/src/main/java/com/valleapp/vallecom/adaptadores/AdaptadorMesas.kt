package com.valleapp.vallecom.adaptadores

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity // O AppCompatActivity si tu MesasActivity hereda de ella
import androidx.viewpager2.adapter.FragmentStateAdapter


class AdaptadorMesas(
    fragmentActivity: FragmentActivity,
    private val mesas: Fragment, // Debería ser ListaMesas? o un tipo más específico si es posible
    private val pedidos: Fragment // Debería ser Pedidos? o un tipo más específico si es posible
) : FragmentStateAdapter(fragmentActivity) {

    // Define los títulos de tus pestañas aquí
    // Asegúrate que el orden y el número coincidan con tu lógica en createFragment y getItemCount
    private val tabTitles = arrayOf("MESAS", "PEDIDOS")

    override fun getItemCount(): Int {
        return tabTitles.size // Debería ser 2 si siempre son "Mesas" y "Pedidos"
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> mesas  // Si 'mesas' es null, crea una nueva instancia de ListaMesas
            // Asegúrate que ListaMesas() tiene un constructor vacío o adecuado
            1 -> pedidos    // Si 'pedidos' es null, crea una nueva instancia de Pedidos
            // Asegúrate que Pedidos() tiene un constructor vacío o adecuado
            else -> {
                // Esto no debería pasar si getItemCount() es 2
                // Pero es bueno tener un fallback o lanzar una excepción
                throw IllegalArgumentException("Posición de fragmento inválida: $position")
            }
        }
    }

    /**
     * Método para proporcionar los títulos a TabLayoutMediator.
     */
    fun getPageTitle(position: Int): CharSequence {
        return tabTitles[position]
    }
}
