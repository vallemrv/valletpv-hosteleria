package com.valleapp.valletpv.screens

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.valleapp.valletpvlib.ui.ToastComposable
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.screens.CamarerosGrid

@Composable
fun CamarerosScreen(navController: NavController) {

    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    var count by remember { mutableIntStateOf(0) }
    var showSnakbar by remember { mutableStateOf(false) }

    fun contarParaSalir() {
        if (count < 2) {
            count++
            showSnakbar = true
        } else {
            count = 0
            navController.popBackStack()
        }

    }

    // Escucha cuando el Composable entra en el estado RESUMED
    DisposableEffect(Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                contarParaSalir()
            }
        }
        dispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    Scaffold(
        topBar = {
            ValleTopBar("Camareros", backAction = {
                contarParaSalir()
            })
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                CamarerosGrid(navController = navController, column = 4)
                ToastComposable(
                    message = "Queda ${3-count} pulsacines para salir",
                    show = showSnakbar,
                    timeout = 1000
                ){
                    showSnakbar = false
                }
            }
        }
    )
}

