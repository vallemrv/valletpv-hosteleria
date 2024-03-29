package com.valleapp.valletpv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.valleapp.valletpv.routers.Navegador
import com.valleapp.valletpvlib.tools.ServiceCom

class ValleTPV : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                Navegador()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, ServiceCom::class.java)
        stopService(intent)
    }


    override fun onStart() {
        super.onStart()
        val intent = Intent(this, ServiceCom::class.java).apply {
            putExtra("res_id", R.mipmap.ic_launcher)
            putExtra("chanel_id", "valle_tpv_channel")
            putExtra("titulo", "ValleTPV")
            putExtra("texto", "Servicio de comunicación")
        }
        startForegroundService(intent)
    }

}


