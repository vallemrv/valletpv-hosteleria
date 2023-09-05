package com.valleapp.valletpv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valleapp.valletpv.models.CamarerosModel
import com.valleapp.valletpv.screens.SelectoresPase
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.Styles

//Creamos un composable para elegir el pase de camareros.
//Contiene un dialogo contenido PaseCamarerosScreen y un boto para salir.
@Composable
fun PaseCamarerosDialog(vModel: CamarerosModel, showDialog: Boolean, onClick: () -> Unit) {

    if (showDialog) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .alpha(0.9f)
                    .fillMaxSize()
            ) {}
            Card(
                elevation = CardDefaults.elevatedCardElevation(8.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(.9f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Pase de camareros", style = Styles.TextTitulos)
                    Box(modifier = Modifier.weight(0.85f)) {
                        SelectoresPase(vModel = vModel)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .weight(0.15f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        BotonSimple(
                            text = "Salir",
                            color = ColorTheme.Primary
                        ) {
                            onClick()
                        }
                    }
                }
            }
        }
    }
}

