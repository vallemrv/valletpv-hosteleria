package com.valleapp.valletpv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.valleapp.valletpv.ui.Preferencias

class ValleTPV : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                Preferencias()
            }
        }

}


