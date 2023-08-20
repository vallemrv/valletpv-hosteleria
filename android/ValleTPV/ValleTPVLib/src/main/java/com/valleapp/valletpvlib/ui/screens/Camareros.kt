package com.valleapp.valletpvlib.ui.screens

import android.app.Application
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.db.IBaseEntity
import com.valleapp.valletpvlib.models.BindServiceModel
import com.valleapp.valletpvlib.ui.ToastComposable
import com.valleapp.valletpvlib.ui.ValleGrid
import com.valleapp.valletpvlib.ui.ValleTopBar


@Composable
fun CamarerosGrid(
    navController: NavHostController
) {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val context = LocalContext.current
    val app = context.applicationContext as Application
    val bindServiceModel: BindServiceModel = viewModel(initializer = { BindServiceModel(app) })

    // Imaginando que bindServiceModel.mService tiene un tipo de retorno nullable
    val mService = bindServiceModel.mService
    val db: CamareroDao? = mService?.getDB("camareros") as? CamareroDao
    var count by remember { mutableIntStateOf(0) }
    var showSnakbar by remember { mutableStateOf(false) }

    // Si db es no nulo, obtén la lista de camareros autorizados, si no, usa una lista vacía
    val listaCamareros by db?.getAutorizados(autorizado = true)?.observeAsState(initial = listOf())
        ?: remember { mutableStateOf(listOf()) }

    DisposableEffect(Unit) {
        bindServiceModel.bindService()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (count < 2) {
                    count++
                    showSnakbar = true
                } else {
                    count = 0
                    navController?.popBackStack()
                }
            }
        }

        dispatcher?.addCallback(callback)

        onDispose {
            bindServiceModel.unbindService()
            callback.remove()
        }
    }

    Scaffold(
        topBar = {
            ValleTopBar(title = "Camareros",
                backAction = {
                    if (count < 2) {
                        count++
                        showSnakbar = true
                    } else {
                        count = 0
                        navController?.popBackStack()
                    }
                })
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                ValleGrid(columns = 5, botones = listaCamareros ){
                    val info = (it as IBaseEntity).getInfoField()
                    navController.navigate("mesas/${info.tag}")
                }
                ToastComposable(
                    message = "Presione nuevamente para salir ${3 - count}",
                    show = showSnakbar,
                    timeout = 2000,
                    fontSize = 20.sp,
                    onHide = {
                        showSnakbar = false
                    }
                )
            }
        }
    )
}

