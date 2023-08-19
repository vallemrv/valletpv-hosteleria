package com.valleapp.valletpv.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.models.PreferenciasModel
import com.valleapp.valletpv.routers.Routers
import com.valleapp.valletpv.ui.AddCamareroDialog
import com.valleapp.valletpvlib.ExtendIcons
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.tools.ServiceCom
import com.valleapp.valletpvlib.ui.ValleTopBar
import com.valleapp.valletpvlib.ui.theme.Pink00



@Composable
fun PaseCamareros(navController: NavController? = null) {

    val cx = LocalContext.current
    val app = cx.applicationContext as Application
    val preferencias: PreferenciasModel = viewModel(initializer = { PreferenciasModel(app) })
    if (!preferencias.preferenciasCargadas) {
        navController?.navigate(Routers.Preferencias.route)
    }

    val serverConfig = preferencias.serverConfig
    val vModel: CamarerosModel = viewModel(initializer = { CamarerosModel(app, serverConfig) })

    DisposableEffect(Unit) {
        vModel.bindService()

        // Desenlazar el servicio cuando el composable ya no esté activo
        onDispose {
            println("Desenlazando servicio")
            vModel.unbindService()
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vModel.showDialog = true },
                containerColor = Pink00,
                modifier = Modifier
                    .padding(10.dp)
                    .size(80.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                )
            }
        },

        topBar = {
            ValleTopBar(
                title = "Pase de Camareros"
            ) {
                IconButton(onClick = {              }) {
                    Icon(
                        painter = painterResource(id = ExtendIcons.arqueo),
                        contentDescription = "Preferencias",
                        modifier = Modifier
                            .size(80.dp)
                    )
                }
                IconButton(onClick = { navController?.navigate(Routers.Preferencias.route) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Preferencias",
                        modifier = Modifier
                            .size(80.dp)
                    )
                }
            }
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                PaseCamarerosScreen(vModel = vModel, navController = navController)
                AddCamareroDialog(model = vModel)
            }
        }
    )

}


@Composable
fun PaseCamarerosScreen(vModel: CamarerosModel, navController: NavController? = null) {
    val cx = LocalContext.current
    var autorizados: List<Camarero> = mutableListOf()
    var noAutorizados: List<Camarero> = mutableListOf()
    val db = vModel.db
    if (db != null) {
        autorizados = db.getAutorizados(autorizado = true).observeAsState(initial = listOf()).value
        noAutorizados = db.getAutorizados(autorizado = false).observeAsState(initial = listOf()).value
    }


    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Primera columna
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            Text(
                text = "Camareros Libre",
                fontSize = 25.sp,
                modifier = Modifier
                    .background(Pink00)
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(16.dp)
            )
            // Aquí puedes usar una LazyColumn en lugar de ListView
            LazyColumn {
                item {
                }
                items(noAutorizados) { camarero ->
                    Text(text = camarero.nombre + " " + camarero.apellidos,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                vModel.setAutorizado(camarero.id, true)
                            }
                            .padding(5.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Segunda columna
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            Text(
                text = "Camareros Pase",
                fontSize = 25.sp,
                modifier = Modifier
                    .background(Pink00)
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(16.dp)
            )
            // Aquí puedes usar una LazyColumn en lugar de ListView
            LazyColumn() {
                item {
                }
                items(autorizados) { camarero ->
                    Text(text = camarero.nombre + " " + camarero.apellidos,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                vModel.setAutorizado(camarero.id, false)
                            }
                            .padding(5.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Botones y Floating Action Button
        Column(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            Image(

                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Salir",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Pink00)
                    .padding(10.dp)
                    .clickable {
                        vModel.unbindService()

                        val intent = Intent(cx, ServiceCom::class.java)
                        cx.stopService(intent)

                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            cx.startActivity(this)
                        }
                    }
            )
            Spacer(modifier = Modifier.height(3.dp))
            Image(
                imageVector = Icons.Default.Check,
                contentDescription = "Aceptar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Pink00)
                    .padding(10.dp)
                    .clickable {
                        navController?.navigate(RoutersBase.Camareros.route) {
                            popUpTo(RoutersBase.Camareros.route) {
                                inclusive = true
                            }
                        }
                    }
            )

        }


    }
}

