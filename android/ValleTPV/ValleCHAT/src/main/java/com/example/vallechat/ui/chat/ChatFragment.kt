package com.example.vallechat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.vallechat.R
import com.example.vallechat.ui.settings.SettingsFragment
import com.valleapp.valletpvlib.tools.JSON
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

class ChatFragment : Fragment() {

    private var isRecording: Boolean = false
    private val RECORD_AUDIO_REQUEST_CODE = 101



    private lateinit var btnRecord: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvTranscript: TextView

    private  var recorder: MediaRecorder? = null

    private var serverUrl: String? = null
    private var uid: String? = null

    @RequiresApi(Build.VERSION_CODES.S)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // El permiso fue concedido, puedes iniciar la grabación
            startRecording()
        } else {
            // El permiso fue denegado
            Log.e("Permisos", "Permiso de grabación de audio denegado")
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Cargar las preferencias antes de cualquier otra acción
        if (!cargarPreferencias()) {
            return view
        }

        // Inicialización de los elementos de la interfaz
        btnRecord = view.findViewById(R.id.btnRecord)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvTranscript = view.findViewById(R.id.tvTranscript)


        // Acciones cuando se presiona el botón
        btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_REQUEST_CODE
                )
            } else {
                startRecording() // Si el permiso ya fue concedido
            }
        }

        // Añadir menú
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_settings -> {
                        loadFragment(SettingsFragment())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        return view
    }





    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording() {
        if (isRecording) {
            stopRecording()
            return
        }

        val musicDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (musicDir != null) {
            val outputFile = File(musicDir, "recorded_audio.3gp")

            recorder = MediaRecorder(requireContext()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    tvStatus.text = "Grabando..."
                    btnRecord.text = "Detener"
                } catch (e: IOException) {
                    Log.e("MediaRecorder", "Error al preparar el MediaRecorder: ${e.message}")
                } catch (e: IllegalStateException) {
                    Log.e("MediaRecorder", "Estado ilegal del MediaRecorder: ${e.message}")
                }
            }
        } else {
            Log.e("MediaRecorder", "El almacenamiento externo no está disponible.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                release()
                val musicDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                if (musicDir != null) {
                    val outputFile = File(musicDir, "recorded_audio.3gp")
                    sendAudioFile(outputFile.absolutePath)
                } else {
                    Log.e("MediaRecorder", "El almacenamiento externo no está disponible.")
                }
            } catch (e: IllegalStateException) {
                Log.e("MediaRecorder", "Error al detener la grabación: ${e.message}")
            }
        }
        recorder = null
        isRecording = false
        tvStatus.text = "Presione para grabar"
        btnRecord.text = "Grabar Pedido"
    }


    fun sendAudioFile(filePath: String) {

        val file = File(filePath)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", file.name, file.asRequestBody("audio/3gp".toMediaTypeOrNull()))
            .addFormDataPart("uid", uid ?: "")
            .build()

        val request = serverUrl?.let {
            Request.Builder()
                .url("http://$it/api/openai/transcribe")
                .post(requestBody)
                .build()
        }

        val client = OkHttpClient()
        if (request != null) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { body ->
                        val transcription = body.string()
                        // Aquí puedes actualizar la UI con la transcripción recibida
                    }
                }
            })
        }
    }

    private fun cargarPreferencias(): Boolean {
        val json = JSON()
        try {
            val sharedPreferences = json.deserializar("settings.dat", context)
            serverUrl = sharedPreferences.getString("URL")
            uid = sharedPreferences.getString("UID")

            return if (serverUrl.isNullOrEmpty() || uid.isNullOrEmpty()) {
                // Si la URL del servidor o el UID no están presentes, carga el fragmento de Settings
                loadFragment(SettingsFragment())
                false
            } else {
                // Si ambos valores están presentes, continúa con el flujo normal
                Log.d("Preferencias", "URL del servidor: $serverUrl, UID: $uid")
                true
            }
        }catch (e: Exception){
            loadFragment(SettingsFragment())
            return false
        }
    }


    private fun showTranscript(transcript: String) {
        tvTranscript.text = transcript
        tvTranscript.visibility = TextView.VISIBLE
        tvStatus.text = "Pedido grabado"
    }

    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
