package com.valleapp.vallecom.activitys


import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.valleapp.vallecom.adaptadores.AdaptadorRefill
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBCuenta

class Refill : ActivityBase() {

    private val dbCuenta = DBCuenta(this)
    private lateinit var idMesa: String

    private fun mostrarLista() {
        val ls = findViewById<ListView>(R.id.listaPedidosRefill)
        val lista = dbCuenta.getPedidosChoices(idMesa)
        val adaptador = AdaptadorRefill(this, lista)
        ls.adapter = adaptador.toListAdapter() // Convierte AdaptadorRefill a un adaptador compatible con ListView
        ls.setOnItemClickListener { _, _, i, _ ->
            try {
                val it = intent
                it.putExtra("IDPedido", lista[i].getString("IDPedido"))
                setResult(RESULT_OK, it)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refill)
        idMesa = intent.extras?.getString("id_mesa") ?: ""
        Log.d("Refill", "onCreate: idMesa = $idMesa")
    }

    override fun onResume() {
        super.onResume()
        mostrarLista()
        Log.d("Refill", "onResume: Lista mostrada")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Refill", "onDestroy: Activity destruida")
    }
}

