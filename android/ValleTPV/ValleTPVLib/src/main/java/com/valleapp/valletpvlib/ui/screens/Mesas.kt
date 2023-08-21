package com.valleapp.valletpvlib.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.valleapp.valletpvlib.db.IBaseEntity
import com.valleapp.valletpvlib.db.MesasDao
import com.valleapp.valletpvlib.db.ZonasDao
import com.valleapp.valletpvlib.models.BindServiceModel
import com.valleapp.valletpvlib.routers.RoutersBase
import com.valleapp.valletpvlib.ui.BotonSimple
import com.valleapp.valletpvlib.ui.TipoBoton
import com.valleapp.valletpvlib.ui.ToastComposable
import com.valleapp.valletpvlib.ui.ValleGrid

class MesasModel: ViewModel(){
    var idZona: Long by mutableLongStateOf(0)
}

@Composable
fun MesasGrid(navController: NavController, camId: Long) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val bindServiceModel: BindServiceModel = viewModel(initializer = { BindServiceModel(app) })
    val model: MesasModel = viewModel()


    // Imaginando que bindServiceModel.mService tiene un tipo de retorno nullable
    val mService = bindServiceModel.mService
    val db: MesasDao? = mService?.getDB("mesas") as? MesasDao
    val dbZona: ZonasDao? = mService?.getDB("zonas") as? ZonasDao

    var showSnakbar by remember { mutableStateOf(false) }

    val listaZonas by dbZona?.getListaLive()?.observeAsState(initial = listOf())
        ?: remember { mutableStateOf(listOf()) }


    val zona = listaZonas.firstOrNull()
    if (zona != null && model.idZona <= 0) {
        model.idZona = zona.id
    }


    val listaMesas by db?.getAllByZona(model.idZona)?.observeAsState(initial = listOf())
        ?: remember { mutableStateOf(listOf()) }



    DisposableEffect(Unit) {
        bindServiceModel.bindService()
        onDispose {
            bindServiceModel.unbindService()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .height(100.dp)
        ) {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
                LazyRow {
                    items(listaZonas) {
                        val info = (it as IBaseEntity).getInfoField()
                        BotonSimple(obj = info, onButtonClick = { infR, _ ->
                            model.idZona = infR.tag as Long
                        })
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight()
        ) {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp))
            {
                ValleGrid(columns = 5, botones = listaMesas, tipo = TipoBoton.MESA) { info, _ ->
                    navController.navigate(
                        RoutersBase.Cuenta.route
                            .replace("{camId}", camId.toString())
                            .replace("{mesaId}", info.tag.toString())
                    )
                }
            }
        }

        ToastComposable(message = "", show = showSnakbar, timeout = 2000) {
            showSnakbar = false
        }
    }

}
