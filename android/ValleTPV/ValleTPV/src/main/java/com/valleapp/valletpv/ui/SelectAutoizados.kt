package com.valleapp.valletpv.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.screens.SelectoresPase
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.theme.ColorTheme

//Creamos un composable para elegir el pase de camareros.
//Contiene un dialogo contenido PaseCamarerosScreen y un boto para salir.
@Composable
fun PaseCamarerosDialog(vModel: CamarerosModel, showDialog: Boolean, onClick: () -> Unit) {

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
              onClick()
            },
            title = {
                Text(text = "Pase de camareros")
            },
            text = {
                SelectoresPase(
                    vModel = vModel,
                )
            },
            confirmButton = {
                BotonSimple(
                    text = "Salir",
                    color = ColorTheme.Primary,
                    modifier = Modifier
                        .height(70.dp)
                        .width(250.dp)
                ) {
                    onClick()
                }
            }
        )
    }
}
