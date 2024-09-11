package com.valleapp.vallecash

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.valleapp.vallecash.databinding.ActivityValleCashBinding
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class ValleCASH : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityValleCashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityValleCashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarValleCash.toolbar)

        binding.appBarValleCash.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_valle_cash)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        // Cargar las preferencias al iniciar la actividad
        val preferencias = cargarPreferencias()

        if (preferencias != null && preferencias.has("URL")) {
            val serverUrl = preferencias.getString("URL")

            // Iniciar el servicio WebSocket con la URL cargada
            iniciarWebSocketService(serverUrl)
        } else {
            // Si no hay preferencias, lanzar la SettingsActivity para configurar la URL
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }


    private fun cargarPreferencias(): JSONObject? {
        val json = JSON()
        return try {
            json.deserializar("settings.dat", this)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    private fun iniciarWebSocketService(serverUrl: String) {
        val intent = Intent(this, WebSocketService::class.java)
        intent.putExtra("SERVER_URL", serverUrl) // Pasar la URL al servicio si lo necesitas
        startService(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.valle_c_a_s_h, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_valle_cash)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Abrir SettingsActivity
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}