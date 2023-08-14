package com.valleapp.valletpv.screens

import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.routers.Routers
import com.valleapp.valletpv.ui.theme.Pink00
import com.valleapp.valletpvlib.ExtendIcons
import com.valleapp.valletpvlib.tools.ServiceCom
import com.valleapp.valletpvlib.ui.ValleTopBar

@Composable
fun PaseCamareros(navController: NavController? = null) {

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
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
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = ExtendIcons.arqueo),
                        contentDescription = "Preferencias",
                        modifier = Modifier
                            .size(80.dp)
                    )
                }
                IconButton(onClick = { navController?.navigate(Routers.Preferencias.route)}) {
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
                PaseCamarerosScreen(navController)
            }
        }
    )

}


@Composable
fun PaseCamarerosScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val vModel: CamarerosModel by remember {
        mutableStateOf(CamarerosModel(context))
    }
    if (vModel.serverConfig == null) navController?.navigate(Routers.Preferencias.route)


    DisposableEffect(Unit) {

        if (!vModel.mBound) {
            Intent(context, ServiceCom::class.java).also { intent ->
                vModel.connection?.let { context.bindService(intent, it, Context.BIND_AUTO_CREATE) }
            }
        }

        // Desenlazar el servicio cuando el composable ya no esté activo
        onDispose {
            println("Desenlazando servicio")
            if (vModel.mBound) {
                vModel.connection?.let { context.unbindService(it) }
                vModel.mBound = false
            }
        }
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
            LazyColumn() {
                item {

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
                        // Salir de la aplicación
                        if (vModel.mBound) {
                            vModel.connection?.let { context.unbindService(it) }
                            vModel.mBound = false
                        }
                        val intent = Intent(context, ServiceCom::class.java)
                        context.stopService(intent)

                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(this)
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
                    .clickable { /* Acción para el botón aceptar */ }
            )

        }


    }
}


@Preview(
    device = Devices.TABLET
)
@Composable
fun PreviewCamarerosLayout() {
    PaseCamareros()
}
