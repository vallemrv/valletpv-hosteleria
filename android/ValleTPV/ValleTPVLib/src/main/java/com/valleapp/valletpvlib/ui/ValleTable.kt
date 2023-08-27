package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.db.AccionMesa
import com.valleapp.valletpvlib.db.Camarero
import com.valleapp.valletpvlib.db.Mesa

@Composable
fun ValleGridSimple(
    columns: Int,
    items: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(4.dp),
    ) {
        items()
    }
}

@Composable
fun FixedGrid(
    items: List<Any>, columnCount: Int, onClick: (Any?) -> Unit
) {
    // Calculamos cuántas filas necesitaremos
    val rowCount = items.size / columnCount + if (items.size % columnCount != 0) 1 else 0

    Column(
        modifier = Modifier.fillMaxHeight(), // O cualquier otro modificador que necesites
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (rowIndex in 0 until rowCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (columnIndex in 0 until columnCount) {
                    val itemIndex = rowIndex * columnCount + columnIndex
                    if (itemIndex < items.size) {
                        val item = items[itemIndex]
                        // Asumiendo que tu "item" es una String, solo para ilustrar:
                        Box(
                            modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                        ) {
                            BotonSimple(text = item.toString(), modifier = Modifier.fillMaxSize()) {
                                onClick(item)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f)) // Llena el espacio si no hay más items
                    }
                }
            }
        }
    }
}


@Composable
fun TableroCamareros(
    columns: Int, camareros: List<Camarero>, onItemClick: (Camarero) -> Unit
) {
    ValleGridSimple(columns) {
        items(camareros) { item ->
            BotonSimple(text = item.toString(), tag = item) {
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
) {
    ValleGridSimple(columns = columns) {
        items(mesas) { item ->
            BotonMesa(mesa = item, onButtonClick = { mesa ->
                onItemClick(mesa)
            }, onAccionClick = { mesa, accionMesa ->
                onAccionClick(mesa, accionMesa)
            })
        }
    }
}


@Composable
fun TecladoNumerico(
    columns: Int = 3, onItemClick: (Int) -> Unit
) {
    FixedGrid(
        items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
        columnCount = columns ){
        onItemClick(it as Int)
    }
}

@Preview
@Composable
fun TecladoNumericoPreview() {
    TecladoNumerico {
        println("Click en tecla $it")
    }
}