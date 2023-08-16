package com.valleapp.valletpv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.ui.theme.Pink00
@Composable
fun AddCamarero(model: CamarerosModel) {
    if (model.showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                model.showDialog.value = false
            },
            title = {
                Text(text = "Agregar camarero")
            },
            text = {

                Column {
                    TextField(
                        value = model.nombre.value,
                        onValueChange = { newValue -> model.nombre.value = newValue },
                        label = { Text("Nombre") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = model.apellido.value,
                        onValueChange = { newValue -> model.apellido.value = newValue },
                        label = { Text("Apellido") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Aquí puedes manejar la acción al presionar el botón confirmar
                    model.showDialog.value = false
                    model.add_camrarero()
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Pink00)) {
                    Text("Confirmar", color = Color.Black)
                }
            }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val showDialog = remember { mutableStateOf(true) }
    AddCamarero(CamarerosModel(LocalContext.current))
}
