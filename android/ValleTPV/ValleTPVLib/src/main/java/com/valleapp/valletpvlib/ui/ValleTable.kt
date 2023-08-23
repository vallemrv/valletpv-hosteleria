package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.Mesa


//Creamos una tabla Grid compose para contener los botones de la calculadora
//parametros: filas, columnas, lista de botones y una funcion que se ejecuta al pulsar un boton
@Composable
fun ValleGridSimple(
    columns: Int,
    items: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(4.dp)
    ) {
        items()
    }
}

@Composable
fun TableroCamareros(
    columns: Int,
    camareros: List<Camarero>,
    onItemClick: (Camarero) -> Unit
) {
    ValleGridSimple(columns){
        items(camareros) { item ->
            BotonSimple(text =item.toString() , tag= item){
                onItemClick(it as Camarero)
            }
        }
    }
}

@Composable
fun TableroMesas(
    columns: Int,
    mesas: List<Mesa>,
    onItemClick: (Mesa) -> Unit,
    onAccionClick: (Mesa, AccionMesa) -> Unit
){
    ValleGridSimple(columns = columns){
        items(mesas) { item ->
            BotonMesa(mesa = item, onButtonClick = { mesa->
                onItemClick(mesa)
            }, onAccionClick = { mesa, accionMesa ->
                onAccionClick(mesa, accionMesa)
            })
        }
    }
}


@Composable
fun TecladoNumerico(
    columns: Int,
    onItemClick: (Int) -> Unit
) {
    ValleGridSimple(columns){
        items((0..9).toList()) { item ->
            BotonSimple(text = item.toString(), tag= item){
                onItemClick(it as Int)
            }
        }
    }
}

