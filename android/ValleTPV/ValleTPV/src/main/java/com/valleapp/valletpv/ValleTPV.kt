package com.valleapp.valletpv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.valleapp.valletpv.routers.Navegador
import com.valleapp.valletpv.ui.theme.ValleTheme
import com.valleapp.valletpvlib.tools.ServiceCom

class ValleTPV : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

              setContent {
                  ValleTheme {
                      Surface {
                          Navegador()
                      }
                  }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, ServiceCom::class.java)
        stopService(intent)
    }
}


