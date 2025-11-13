package com.valleapp.vallecash

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

import com.valleapp.vallecash.tools.WebSocketService
import com.valleapp.valletpv.R
import com.valleapp.valletpv.databinding.ActivityValleCashBinding
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess

class ValleCASH : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityValleCashBinding
    private var serverURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityValleCashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarValleCash.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_valle_cash)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_modificar_cambio -> navController.navigate(R.id.nav_modificar_cambio, Bundle().apply { putString("server", serverURL) })
                R.id.nav_fondo_caja -> navController.navigate(R.id.nav_fondo_caja, Bundle().apply { putString("server", serverURL) })
                R.id.nav_home -> navController.navigate(R.id.nav_home, Bundle().apply { putString("server", serverURL) })
                R.id.nav_mantenimiento -> navController.navigate(R.id.nav_mantenimiento, Bundle().apply { putString("server", serverURL) })
                R.id.nav_salir -> {
                    // Mostrar diálogo de confirmación para salir
                    mostrarDialogoSalir()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val preferencias = cargarPreferencias()
        if (preferencias != null && preferencias.has("URL")) {
            serverURL = preferencias.getString("URL")
            iniciarWebSocketService(serverURL)
        } else {
            navController.navigate(R.id.nav_settings)
        }

        // Mantener la pantalla encendida
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Configurar el callback para el botón atrás
        setupBackPressedCallback()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Si el drawer está abierto, cerrarlo
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Si estamos en el fragmento principal, mostrar diálogo de salir
                    val navController = findNavController(R.id.nav_host_fragment_content_valle_cash)
                    if (navController.currentDestination?.id == R.id.nav_home) {
                        mostrarDialogoSalir()
                    } else {
                        // Para otros fragmentos, permitir navegación normal hacia atrás
                        if (!navController.popBackStack()) {
                            // Si no hay más fragmentos en el stack, mostrar diálogo de salir
                            mostrarDialogoSalir()
                        }
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun cargarPreferencias(): JSONObject? {
        return try {
            JSON().deserializar("settings.dat", this)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    private fun iniciarWebSocketService(serverUrl: String) {
        val intent = Intent(this, WebSocketService::class.java).apply {
            putExtra("SERVER_URL", serverUrl)
        }
        startService(intent)
    }

    private fun detenerWebSocketService() {
        val intent = Intent(this, WebSocketService::class.java).apply {
            putExtra("action", "STOP_SERVICE")
        }
        stopService(intent)
    }

    private fun mostrarDialogoSalir() {
        AlertDialog.Builder(this)
            .setTitle("Salir de ValleCASH")
            .setMessage("¿Está seguro que desea cerrar la aplicación?")
            .setPositiveButton("Sí") { _, _ ->
                cerrarAplicacionCompleta()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cerrarAplicacionCompleta() {
        try {
            // Detener el servicio WebSocket
            detenerWebSocketService()
            
            // Finalizar la actividad
            finishAffinity() // Cierra todas las actividades de la aplicación
            
            // Forzar el cierre del proceso
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        } catch (e: Exception) {
            e.printStackTrace()
            // Si hay algún error, al menos finalizar la actividad
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.valle_c_a_s_h, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_content_valle_cash).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegurarse de detener el servicio al destruir la actividad
        detenerWebSocketService()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_fragment_content_valle_cash).navigate(R.id.nav_settings)
                true
            }
            R.id.nav_salir -> {
                mostrarDialogoSalir()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    // Método público para que el fragment pueda llamar al diálogo de salir
    fun salirAplicacion() {
        mostrarDialogoSalir()
    }
}