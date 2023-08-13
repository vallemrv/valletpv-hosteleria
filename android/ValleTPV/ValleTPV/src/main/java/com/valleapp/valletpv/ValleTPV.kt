package com.valleapp.valletpv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import com.valleapp.valletpv.models.PreferenciasModel
import com.valleapp.valletpv.ui.Preferencias

class ValleTPV : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                Preferencias(PreferenciasModel(this))
            }
        }

}


