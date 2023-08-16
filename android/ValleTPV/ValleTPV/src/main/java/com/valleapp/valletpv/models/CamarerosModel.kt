package com.valleapp.valletpv.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.valleapp.valletpv.R
import com.valleapp.valletpv.tools.createServiceConnection
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom

class CamarerosModel(cx: Context) : ViewModel() {

    var mService: ServiceCom? by mutableStateOf(null)
    var mBound by mutableStateOf(false)
    var connection: ServiceConnection? by mutableStateOf(null)

    var showDialog: MutableState<Boolean> by mutableStateOf(false)
    var nombre: MutableState<String> by mutableStateOf("")
    var apellido: MutableState<String> by mutableStateOf("")
    var context: Context by mutableStateOf(cx)

    fun bindService() {
        createServiceConnection().let {
            mBound = it.mBound
            connection = it.connection
            mService = it.mService
        }
    }

    fun unbindService() {
        if (mBound) {
            mService?.let {
                context.unbindService(connection!!)
                mBound = false
            }
        }
    }

    fun add_camrarero() {
        TODO("Not yet implemented")
    }



}