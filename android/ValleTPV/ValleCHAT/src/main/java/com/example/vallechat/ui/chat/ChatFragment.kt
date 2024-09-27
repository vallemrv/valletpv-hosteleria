package com.example.vallechat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.vallechat.R
import com.example.vallechat.ui.settings.SettingsFragment

class ChatFragment : Fragment() {

    private lateinit var btnRecord: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvTranscript: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Inicialización de los elementos de la interfaz
        btnRecord = view.findViewById(R.id.btnRecord)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvTranscript = view.findViewById(R.id.tvTranscript)

        // Acciones cuando se presiona el botón
        btnRecord.setOnClickListener {
            startRecording()
        }

        // Añadir menú
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_settings -> {
                        loadFragment(SettingsFragment())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return view
    }

    private fun startRecording() {
        tvStatus.text = "Grabando pedido..."
        val simulatedTranscript = "Pedido: 2 cervezas y 1 tapa de jamón"
        showTranscript(simulatedTranscript)
    }

    private fun showTranscript(transcript: String) {
        tvTranscript.text = transcript
        tvTranscript.visibility = TextView.VISIBLE
        tvStatus.text = "Pedido grabado"
    }

    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
