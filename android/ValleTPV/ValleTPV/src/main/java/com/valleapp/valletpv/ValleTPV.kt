package com.valleapp.valletpv;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSelCam;
import com.valleapp.valletpvlib.db.DBCamareros;
import com.valleapp.valletpv.dlg.DlgAddNuevoCamarero;
import com.valleapp.valletpvlib.tools.JSON;
import com.valleapp.valletpv.tools.ServiceCOM;

import org.json.JSONException;
import org.json.JSONObject;

public class ValleTPV extends Activity {

    final Context cx = this;

    private String server = "";

    ServiceCOM myServicio;
    DBCamareros dbCamareros;

    ListView lsnoautorizados;
    ListView lstautorizados;

    private String urlCashlogy = "";
    private boolean usarCashlogy = false;
    private boolean usarTPVPC = false;
    private String ipTPVPC = "";


    private final Handler handleHttp = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle.containsKey("CashlogyMsg")) {
                String toastMessage = bundle.getString("CashlogyMsg");
                if (toastMessage != null) {
                    Toast.makeText(ValleTPV.this, toastMessage, Toast.LENGTH_LONG).show();
                }
            }else {
                rellenarListas();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valletpv);

        ImageButton s = findViewById(R.id.salir);
        lstautorizados = findViewById(R.id.lstautorizados);
        lsnoautorizados = findViewById(R.id.lstnoautorizados);

        s.setOnClickListener(view -> finish());

        ImageButton btnok = findViewById(R.id.aceptar);
        btnok.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), Camareros.class);
            startActivity(i);
        });

        ImageButton btnPref = findViewById(R.id.btn_aceptar_preferencias);
        btnPref.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PreferenciasTPV.class);
            startActivity(i);
        });

        ImageButton btnArqueo = findViewById(R.id.btn_arqueo_caja);
        btnArqueo.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), Arqueo.class);
            startActivity(i);
        });

        lsnoautorizados.setOnItemClickListener((adapterView, view, i, l) -> {
            JSONObject obj = (JSONObject)view.getTag();
            try {
                dbCamareros.setAutorizado(obj.getInt("ID"), true);
                obj.put("autorizado", "1");
                myServicio.autorizarCam(obj);
            } catch (JSONException e) {
                Log.e("VALLETPV_ERR", e.toString());
            }
            rellenarListas();
        });

         findViewById(R.id.btn_add_nuevo_camarero).setOnClickListener(view -> {
             DlgAddNuevoCamarero dlg = new DlgAddNuevoCamarero(cx, myServicio);
             dlg.show();
         });


        lstautorizados.setOnItemClickListener((adapterView, view, i, l) -> {
            JSONObject obj = (JSONObject)view.getTag();
            try {
                obj.put("autorizado", "0");
                dbCamareros.setAutorizado(obj.getInt("ID"), false);
                myServicio.autorizarCam(obj);
            } catch (JSONException e) {
                Log.e("VALLETPV_ERR", e.toString());
            }
            rellenarListas();

        });
    }

    private void rellenarListas(){
        lstautorizados.setAdapter(new AdaptadorSelCam(cx, dbCamareros.getAutorizados(true)));
        lsnoautorizados.setAdapter(new AdaptadorSelCam(cx, dbCamareros.getAutorizados(false)));
    }

    @Override
    protected void onResume() {
        cargarPreferencias();
        if (myServicio != null) {
            rellenarListas();
        }
        if (server != null && !server.isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), ServiceCOM.class);
            intent.putExtra("url", server);
            intent.putExtra("url_cashlogy", urlCashlogy);  // Agregar URL de Cashlogy
            intent.putExtra("usar_cashlogy", usarCashlogy); // Agregar estado del CheckBox
            intent.putExtra("usar_tpvpc", usarTPVPC); // Agregar estado del CheckBox
            intent.putExtra("ip_tpvpc", ipTPVPC); // Agregar IP del servidor TPVPC
            startService(intent);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
        }
        super.onResume();
    }

    private fun iniciarConexionSocket(ip: String, port: Int, totalToCobrar: Double) {
        thread {
            try {
                Log.d("CobroTarjetaActivity", "Iniciando conexión con el servidor TPVPC")

                // Conectar al servidor TPVPC con un timeout de 5 segundos
                socket = Socket()
                val socketAddress = InetSocketAddress(ip, port)
                socket!!.connect(socketAddress, 5000)  // Timeout de 5 segundos

                writer = PrintWriter(socket!!.getOutputStream(), true)

                // Verificar si el socket está conectado
                if (socket!!.isConnected) {
                    cancelado = false

                    // Enviar el total a cobrar
                    val mensajeCobro = "{\"comando\": \"iniciar_cobro\", \"importe\": $totalToCobrar}"
                    writer?.println(mensajeCobro)

                    // Escuchar respuestas del servidor en un hilo separado
                    recibirRespuestasSocket()
                } else {
                    throw Exception("No se pudo conectar al servidor TPVPC")
                }

            } catch (e: Exception) {
                runOnUiThread {
                    cancelado = true
                    Toast.makeText(this, "Error de conexión con el servidor: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun recibirRespuestasSocket() {
        thread {
            try {
                val socketActual = socket ?: throw Exception("El socket no está inicializado")

                val inputStream = socketActual.getInputStream()
                val reader = inputStream.bufferedReader()

                val buffer = CharArray(1024)
                var charsLeidos: Int
                val respuestaCompleta = StringBuilder()

                while (socketActual.isConnected) {
                    try {
                        charsLeidos = reader.read(buffer)
                        if (charsLeidos > 0) {
                            respuestaCompleta.appendRange(buffer, 0, charsLeidos)

                            when {
                                respuestaCompleta.contains("cancelado") -> {
                                    runOnUiThread {
                                        cancelado = true
                                        setResult(RESULT_CANCELED)
                                        finish()
                                    }

                                }
                                respuestaCompleta.contains("denegada") -> {
                                    runOnUiThread {
                                        cancelado = true
                                        tvEstadoCobro.text = "Operación denegada"
                                    }

                                }
                                respuestaCompleta.contains("fallo") -> {
                                    runOnUiThread {
                                        cancelado = true
                                        tvEstadoCobro.text = "Pinpad error de conexión, reinicie el sistema"
                                    }
                                }
                                respuestaCompleta.contains("error") -> {
                                    runOnUiThread {
                                        cancelado = true
                                        tvEstadoCobro.text = "Operación cancelada"
                                    }

                                }
                                respuestaCompleta.contains("esperando") -> {
                                    runOnUiThread {
                                        cancelado = false
                                        tvEstadoCobro.text = String.format(
                                                Locale.getDefault(),
                                                "Esperando tarjeta de crédito %.2f €", totalToCobrar
                                        )
                                    }
                                }
                                respuestaCompleta.contains("iniciando") -> {
                                    runOnUiThread {
                                        cancelado = true
                                        tvEstadoCobro.text = "Iniciando pinpad, por favor espere."
                                    }
                                }
                                respuestaCompleta.contains("iniciado") -> {
                                    runOnUiThread {
                                        cancelado = true
                                        tvEstadoCobro.text = "El pinpad está iniciado, repita la operación."
                                    }
                                }
                                respuestaCompleta.contains("acpetado") -> {
                                    runOnUiThread {
                                        val resultData = Intent()

                                        try {
                                            val res = JSONObject(respuestaCompleta.toString())
                                            resultData.putExtra("recibo", res.getString("recibo"))
                                        } catch (ignored: JSONException) {
                                            resultData.putExtra("recibo", "0")
                                        }

                                        resultData.putExtra("totalIngresado", 0.0)
                                        resultData.putExtra("cambio", 0.0)
                                        resultData.putExtra("totalMesa", totalToCobrar)
                                        resultData.putExtra("lineas", lineas)

                                        setResult(RESULT_OK, resultData)
                                        finish()
                                    }
                                    break
                                }
                            }
                            respuestaCompleta.clear()
                        }
                    } catch (e: SocketTimeoutException) {
                        println("No se recibió respuesta del servidor dentro del tiempo esperado.")
                        break
                    } catch (e: SocketException) {
                        println("Socket cerrado inesperadamente.")
                        runOnUiThread {
                            cancelado = true
                            tvEstadoCobro.text = "Conexión cerrada. Operación cancelada."
                        }
                        break
                    } catch (e: IOException) {
                        println("Error de I/O en la conexión: ${e.message}")
                        runOnUiThread {
                            cancelado = true
                            tvEstadoCobro.text = "Error de conexión. Operación cancelada."
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    cancelado = true
                    Toast.makeText(this@CobroTarjetaActivity, "Error en la conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    
    @Override
    protected void onDestroy() {
        unbindService(mConexion);
        Intent intent = new Intent(cx, ServiceCOM.class);
        stopService(intent);
        super.onDestroy();
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if (pref == null) {
                // Si no hay preferencias guardadas, redirigir a la actividad de preferencias
                Intent intent = new Intent(this, PreferenciasTPV.class);
                startActivity(intent);
            } else {
                // Cargar URL del servidor
                server = pref.getString("URL");

                // Cargar preferencias de Cashlogy
                urlCashlogy = pref.optString("URL_Cashlogy", "");  // Leer URL de Cashlogy
                usarCashlogy = pref.optBoolean("usaCashlogy", false);  // Leer si se usa Cashlogy

                // Cargar preferencias de TPVPC (nuevos elementos)
                usarTPVPC = pref.optBoolean("usaTPVPC", false);  // Leer si se usa TPVPC
                ipTPVPC = pref.optString("IP_TPVPC", "");  // Leer la IP del servidor TPVPC
            }
        } catch (Exception e) {
            Log.e("VALLETPV_ERR", e.toString());
        }
    }



    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServiceCOM.MyBinder)iBinder).getService();
            myServicio.setExHandler("camareros", handleHttp);
            dbCamareros = (DBCamareros) myServicio.getDb("camareros");
            myServicio.executeCaslogy(usarCashlogy, urlCashlogy);
            myServicio.setExHandler("camareros", handleHttp);
            rellenarListas();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };



}