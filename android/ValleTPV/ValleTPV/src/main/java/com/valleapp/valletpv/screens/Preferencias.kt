package com.valleapp.valletpv.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpv.R
import com.valleapp.valletpv.models.PreferenciasModel


@Composable
fun Preferencias(vModel: PreferenciasModel) {

    val pinkColor = colorResource(id = R.color.pink)
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

                Button(
                    onClick = { vModel.onOkClick() },
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = pinkColor)
                ) {
                    Text(text = "OK")
                }
            }

            if (vModel.isCardVisible) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 10.dp

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


                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { vModel.onValidarClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = pinkColor)
                        ) {
                            Text(text = "Validar Código")
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
    Preferencias(PreferenciasModel(LocalContext.current))
}
