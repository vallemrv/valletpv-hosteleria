
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
import com.valleapp.valletpvlib.tools.ServerConfig
import com.valleapp.valletpvlib.tools.ServiceCom

class MainModel(app: Application): AndroidViewModel(app) {
    var serverConfig: ServerConfig by mutableStateOf(ServerConfig())
    var mService: ServiceCom? by mutableStateOf(null)
    var isAunthValid by mutableStateOf(true)
}


class ValleAplicacion : Application() {


    private var mBound = false
    private var connection: ServiceConnection? = null
    lateinit var mainModel: MainModel


    override fun onCreate() {
        super.onCreate()
        if (connection == null) {
            connection = object : ServiceConnection {
                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    val binder = service as ServiceCom.LocalBinder
                    mService = binder.getService()
                    mBound = true

                }

                override fun onServiceDisconnected(arg0: ComponentName) {
                    println("Servicio desenlazado")
                    mBound = false
                }
            }
        }
        if (!mBound) {
            Intent(this, ServiceCom::class.java).also { intent ->
                this.bindService(
                    intent,
                    connection as ServiceConnection,
                    Context.BIND_AUTO_CREATE
                )
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        if (mBound) {
            mService?.let {
                this.unbindService(connection!!)
                mBound = false
            }
            println("Servicio desenlazado")
        }
    }
}