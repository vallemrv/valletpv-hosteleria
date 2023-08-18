package com.valleapp.valletpv.ui

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.ui.theme.Pink00
import com.valleapp.valletpvlib.tools.ServerConfig

@Composable
fun AddCamareroDialog(model: CamarerosModel) {

    if (model.showDialog) {
        AlertDialog(
            onDismissRequest = {
                model.showDialog = false
            },
            title = {
                Text(text = "Agregar camarero")
            },
            text = {

                Column {
                    TextField(
                        value = model.nombre,
                        onValueChange = { newValue -> model.nombre = newValue },
                        label = { Text("Nombre") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = model.apellido,
                        onValueChange = { newValue -> model.apellido = newValue },
                        label = { Text("Apellido") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Aquí puedes manejar la acción al presionar el botón confirmar
                    model.showDialog = false
                    model.addCamarero()
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Pink00)) {
                    Text("Confirmar", color = Color.Black, fontSize = 30.sp)
                }
            }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val cx = LocalContext.current
    val app = if (isSystemInDarkTheme()) {
        null
    } else {
        cx.applicationContext as Application
    }
    app?.let{
        CamarerosModel(app, ServerConfig())
    }

}
