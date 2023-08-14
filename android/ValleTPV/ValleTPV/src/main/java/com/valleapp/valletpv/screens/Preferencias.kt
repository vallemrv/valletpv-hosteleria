package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.valleapp.valletpv.models.PreferenciasModel
import com.valleapp.valletpv.ui.theme.Pink00
import com.valleapp.valletpvlib.ui.ValleTopBar


@Composable
fun Preferencias(navController: NavController? = null) {
    Scaffold(
        topBar = {
            ValleTopBar(
                title = "Preferencias",
                backAction = {
                    navController?.popBackStack()
                },
            )
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                PreferenciasScreen()
            }
        }
    )

}

@Composable
fun PreferenciasScreen() {

    var vModel = PreferenciasModel(LocalContext.current)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    modifier = Modifier
                        .weight(0.9f),
                    value = vModel.url, // Variable que guarda el valor del TextField
                    onValueChange = { vModel.url = it }, // Función que se llama al cambiar el valor
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    textStyle = TextStyle(fontSize = 40.sp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "URL del servidor",
                            fontSize = 40.sp
                        )
                    },


                    )
                Spacer(modifier = Modifier.width(10.dp))
                ExtendedFloatingActionButton(
                    onClick = { vModel.onOkClick() },
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxSize(),
                    containerColor = Pink00,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check, contentDescription = "Guardar",
                        Modifier.fillMaxSize(),
                        tint = Color.Black
                    )
                }
            }

            if (vModel.isCardVisible) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Código", fontSize = 24.sp
                        )

                        TextField(
                            value = vModel.codigo, // Variable que guarda el valor del TextField
                            onValueChange = {
                                if (it.length <= 6) vModel.codigo = it
                            }, // Función que se llama al cambiar el valor
                            modifier = Modifier
                                .fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 100.sp, textAlign = TextAlign.Center),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = {
                                Text(
                                    text = "Código aqui",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )


                        Spacer(modifier = Modifier.height(10.dp))

                        ExtendedFloatingActionButton(
                            onClick = { vModel.onValidarClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            containerColor = Pink00,
                        ) {
                            Text(text = "Validar Código", color = Color.Black, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(
    showBackground = true,
    widthDp = 600,

    )
fun PreferenciasPreview() {
    Preferencias(null)
}
