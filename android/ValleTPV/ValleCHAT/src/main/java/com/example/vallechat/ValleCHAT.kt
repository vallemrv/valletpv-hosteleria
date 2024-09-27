package com.example.vallechat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.vallechat.ui.chat.ChatFragment

class ValleCHAT : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_valle_chat)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Cargar el fragmento principal
        if (savedInstanceState == null) {
            loadFragment(ChatFragment())
        }

    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
