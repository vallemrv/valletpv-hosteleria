package com.valleapp.valletpv

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tu.paquete.CustomToast
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class PreferenciasTPV : AppCompatActivity() {

    private val customToast = CustomToast(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias_tpv)

        val cx: Context = this

        // Referencias a los elementos de la interfaz
        val txtUrl = findViewById<EditText>(R.id.txtUrl)
                val txtCashlogyUrl = findViewById<EditText>(R.id.txtCashlogyConnector)
                val txtTPVPCUrl = findViewById<EditText>(R.id.txtTPVPCServer)  // Nuevo campo para IP TPVPC
                val chkUseCashlogy = findViewById<CheckBox>(R.id.chkUseCashlogy)
                val chkUseTPVPC = findViewById<CheckBox>(R.id.chkUseTPVPC)  // Nuevo CheckBox para usar TPVPC
                val btn = findViewById<Button>(R.id.btn_aceptar_preferencias)

                // Cargar las preferencias desde el archivo
                val obj = cargarPreferencias()

        // Si el objeto JSON no es nulo, cargamos las preferencias
        if (obj != null) {
            try {
                // Cargar URL del servidor
                if (obj.has("URL")) {
                    txtUrl.setText(obj.getString("URL"))
                }

                // Cargar URL de Cashlogy
                if (obj.has("URL_Cashlogy")) {
                    txtCashlogyUrl.setText(obj.getString("URL_Cashlogy"))
                }

                // Cargar si se usa Cashlogy o no
                if (obj.has("usaCashlogy")) {
                    chkUseCashlogy.isChecked = obj.getBoolean("usaCashlogy")
                }

                // Cargar si se usa TPVPC o no (nuevo)
                if (obj.has("usaTPVPC")) {
                    chkUseTPVPC.isChecked = obj.getBoolean("usaTPVPC")
                }

                // Cargar la IP del servidor TPVPC (nuevo)
                if (obj.has("IP_TPVPC")) {
                    txtTPVPCUrl.setText(obj.getString("IP_TPVPC"))
                }

            } catch (e: JSONException) {
                Log.e("PREFERENCIAS_ERR", e.toString())
            }
        }

        // Configurar el evento del botón para guardar las preferencias
        btn.setOnClickListener {
            val url = txtUrl.text.toString()
            val urlCashlogy = txtCashlogyUrl.text.toString()
            val urlTPVPC = txtTPVPCUrl.text.toString()  // Obtener IP del servidor TPVPC
            val usarCashlogy = chkUseCashlogy.isChecked
            val usarTPVPC = chkUseTPVPC.isChecked  // Obtener valor de "Usar TPVPC"

            val obj1 = JSONObject()
            try {
                // Guardar las preferencias en el objeto JSON
                obj1.put("URL", url)
                obj1.put("URL_Cashlogy", urlCashlogy)
                obj1.put("IP_TPVPC", urlTPVPC)  // Guardar la IP del servidor TPVPC
                obj1.put("usaCashlogy", usarCashlogy)
                obj1.put("usaTPVPC", usarTPVPC)  // Guardar si se usa TPVPC

                // Serializar y guardar el JSON en un archivo
                val json = JSON()
                json.serializar("preferencias.dat", obj1, cx)

                // Mostrar un mensaje de confirmación
                customToast.showBottom("Datos guardados correctamente", Toast.LENGTH_SHORT)

                // Finalizar la actividad
                finish()

            } catch (e: JSONException) {
                Log.e("PREFERENCIAS_ERR", e.toString())
            }
        }
        val btnSalir = findViewById<ImageButton>(R.id.btn_salir)
                btnSalir.setOnClickListener { finish() }
    }

    private fun cargarPreferencias(): JSONObject? {
            val json = JSON()
    try {
        return json.deserializar("preferencias.dat", this)
    } catch (e: JSONException) {
        Log.e("PREFERENCIAS_ERR", e.toString())
    }
    return null
    }
}