package com.valleapp.valletpv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpv.R


@Composable
fun Preferencias() {
    var url by remember {
        mutableStateOf("")
    }

    var codigo by remember {
        mutableStateOf("")
    }
    val pinkColor = colorResource(id = R.color.pink)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier.height(70.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = url, // Variable que guarda el valor del TextField
                onValueChange = { url = it}, // Función que se llama al cambiar el valor
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                textStyle = TextStyle(fontSize = 50.sp),
                placeholder = { Text(text = "URL del servidor") }
            )

            Button(
                onClick = { /* acción del botón aquí */ },
                modifier = Modifier
                    .height(70.dp)
                    .width(70.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = pinkColor)
            ) {
                Text(text = "OK")
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()

                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Código",
                    fontSize = 24.sp
                )

                TextField(
                    value = codigo, // Variable que guarda el valor del TextField
                    onValueChange = { codigo = it}, // Función que se llama al cambiar el valor
                    modifier = Modifier
                        .height(100.dp)
                        .weight(0.9f)
                        .fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 100.sp),
                    placeholder = { Text(text = "Código aqui") }
                )


                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* acción del botón aquí */ },
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

@Composable
@Preview(showBackground = true)
fun PreferenciasPreview() {
    Preferencias()
}
