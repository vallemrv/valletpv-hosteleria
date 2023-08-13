package com.valleapp.valletpv.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaseCamareros() {
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
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(16.dp)
            )
            // Aquí puedes usar una LazyColumn en lugar de ListView
            LazyColumn(){
                item{

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
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(16.dp)
            )
            // Aquí puedes usar una LazyColumn en lugar de ListView
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
                    .padding(10.dp)
                    .clickable { /* Acción para el botón salir */ }
            )
            Spacer(modifier = Modifier.height(3.dp))
            Image(
                imageVector = Icons.Default.Check,
                contentDescription = "Aceptar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(10.dp)
                    .clickable { /* Acción para el botón aceptar */ }
            )

        }

        FloatingActionButton(onClick = { /*TODO*/  },
            modifier = Modifier.size(70.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Camarero",
            )
        }
    }
}



@Preview(showBackground = true, widthDp = 1024)
@Composable
fun PreviewCamarerosLayout() {
    PaseCamareros()
}
