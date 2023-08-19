package com.valleapp.valletpvlib.ui.screens

import android.app.Application
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.CamareroDao
import com.valleapp.valletpvlib.models.BindServiceModel
import com.valleapp.valletpvlib.ui.ToastComposable
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.theme.Pink00


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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(listaCamareros) { camarero ->
                        CamareroItem(camarero, navController)
                    }
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

@Composable
fun CamareroItem(camarero: Camarero, navController: NavHostController) {
    ExtendedFloatingActionButton(
        onClick = {
            navController.navigate("mesas/${camarero.id}")
        },
        modifier = Modifier
            .padding(8.dp)
            .height(150.dp),
        containerColor = Pink00

    ) {
        Text(
            text = "${camarero.nombre} ${camarero.apellidos}",
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(9.dp)
                .wrapContentSize(Alignment.Center),
            lineHeight = 40.sp,
            color = Color.Black,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}
