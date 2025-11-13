package com.valleapp.vallecom.adaptadores

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdaptadorComanda(
    fa: FragmentActivity,
    private val comanda: Fragment,
    private val teclados: Fragment
) : FragmentStateAdapter(fa) {

    private val tabTitles = listOf("Teclados", "Comandas")

    override fun getItemCount(): Int {
        return tabTitles.size // Debería ser 2 si siempre son "Mesas" y "Pedidos"
    }


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> comanda
            0 -> teclados
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    fun getPageTitle(position: Int): CharSequence {
        return tabTitles[position]
    }
}
