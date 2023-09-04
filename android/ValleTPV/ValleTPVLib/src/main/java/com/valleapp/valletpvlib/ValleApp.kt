
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom

class MainModel(private val app: Application): AndroidViewModel(app) {
    var serverConfig: ServerConfig by mutableStateOf(ServerConfig())
    var mService: ServiceCom? by mutableStateOf(null)
    var isAunthValid by mutableStateOf(true)

    var isPreferenciasCargadas by mutableStateOf(false)


    fun invalidateAuth() {
        isAunthValid = false
        println("Auth invalido")
    }

    fun cargarPreferencias() {
        serverConfig = ServerConfig()
        if (!isPreferenciasCargadas) {
            JSON.deserializar("preferencias.dat", app.applicationContext)?.let {
                if (it.has("url")) {
                     serverConfig.url = it.getString("url")
                }
                if (it.has("codigo")) {
                    serverConfig.codigo = it.getString("codigo")
                }
                if (it.has("UID")) {
                    serverConfig.uid = it.getString("UID")
                }
                isPreferenciasCargadas = !serverConfig.isEmpty()
            }
        }
    }

    fun guardarPreferencias() {
        serverConfig.toJson().let {
            if (it != null) {
                JSON.serializar("preferencias.dat", it, app.applicationContext)
            }
        }
    }


    fun carg
}


class ValleAplicacion : Application() {


    private var mBound = false
    private var connection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as ServiceCom.LocalBinder
                mService = binder.getService()
                mainModel.mService = mService
                mBound = true
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                println("Servicio desenlazado")
                mBound = false
            }
        }

    private var mService: ServiceCom? = null

    val mainModel: MainModel = MainModel(this)


    override fun onCreate() {
        super.onCreate()
        if (!mBound) {
            Intent(this, ServiceCom::class.java).also { intent ->
                this.bindService(
                    intent,
                    connection,
                    Context.BIND_AUTO_CREATE
                )
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        if (mBound) {
            mService?.let {
                this.unbindService(connection)
                mBound = false
            }
            println("Servicio desenlazado")
        }
    }
}