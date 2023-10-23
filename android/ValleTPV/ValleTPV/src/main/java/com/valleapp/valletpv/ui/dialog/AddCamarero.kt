package com.valleapp.valletpv.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.theme.ColorTheme

@Composable
fun AddCamareroDialog(vModel: CamarerosModel, onClick: (Camarero) -> Unit) {

    val camarero = Camarero()

    if (vModel.showDialog) {
        AlertDialog(
            onDismissRequest = {
                vModel.showDialog = false
            },
            title = {
                Text(text = "Agregar camarero")
            },
            text = {

                Column {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = camarero.nombre,
                        onValueChange = { newValue -> camarero.nombre = newValue },
                        label = { Text("Nombre") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = camarero.apellidos,
                        onValueChange = { newValue -> camarero.apellidos = newValue },
                        label = { Text("Apellido") }
                    )
                }
            },
            confirmButton = {
                BotonSimple(
                    text = "Confirmar",
                    color = ColorTheme.Primary,
                    modifier = Modifier
                        .height(70.dp)
                        .width(250.dp)
                ) {
                    vModel.showDialog = false
                    onClick(camarero)
                }
            },
            dismissButton = {
                BotonSimple(
                    text = "Cancelar",
                    color = ColorTheme.Secundary,
                    modifier = Modifier
                        .height(70.dp)
                        .width(250.dp)
                ) {
                    vModel.showDialog = false
                }
            }
        )
    }
}


