package com.valleapp.valletpv.cashlogyActivitis;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.tools.ServiceCOM;
import com.valleapp.valletpvlib.cashlogymanager.PaymentAction;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.Locale;

public class CobroCashlogyActivity extends Activity {

     double totalMesa;
     String lineas;
     ServiceCOM myServicio;
     PaymentAction paymentAction;

     TextView tvTotalCobro;
     TextView tvTotalIngresado;
     TextView tvCambio;
     ImageButton btnCobrar;
     ImageButton btnCancelar;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServiceCOM.MyBinder) iBinder).getService();
            iniciarCobro();  // Iniciar el proceso de cobro después de conectar el servicio
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobro_cashlogy);

        // Recoger datos de la Intent
        totalMesa = getIntent().getDoubleExtra("totalMesa", 0.0);
        lineas = getIntent().getStringExtra("lineas");


        // Inicializar las vistas
        tvTotalCobro = findViewById(R.id.tvTotalCobro);
        tvTotalIngresado = findViewById(R.id.tvTotalIngresado);
        tvCambio = findViewById(R.id.tvCambio);
        btnCobrar = findViewById(R.id.btnAceptar);
        btnCancelar = findViewById(R.id.btnSalir);

        // Configurar la UI con los datos iniciales
        tvTotalCobro.setText(String.format(Locale.UK, "%01.2f €", totalMesa));
        tvTotalIngresado.setText(String.format(Locale.UK," %01.2f €", 0.0));
        tvCambio.setText(String.format(Locale.UK,"%01.2f €", 0.0));

        // Conectar al ServicioCom
        Intent intent = new Intent(this, ServiceCOM.class);
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE);

        // Configurar los botones
        btnCobrar.setOnClickListener(this::finalizarCobroParcial);
        btnCancelar.setOnClickListener(view -> {
            if(paymentAction.cancelarCobro()){
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        } ); // Cancelar y cerrar la actividad
    }

    private void iniciarCobro() {
        // Obtener PaymentAction a través de myServicio
        if (myServicio != null) {
            paymentAction = myServicio.cashLogyPayment(totalMesa, new Handler(message -> {
                Bundle data = message.getData();

                if (data != null) {
                    String key = data.getString("key", "");
                    String value = data.getString("value", "");

                    switch (key) {
                          case "CASHLOGY_IMPORTE_ADMITIDO":
                            // Actualizar el TextView para el importe admitido
                            double importeAdmitido = Double.parseDouble(value);
                            tvTotalIngresado.setText(String.format(Locale.UK,"%01.2f €", (importeAdmitido)));

                            // Calcular y actualizar el cambio
                            double cambio = importeAdmitido - totalMesa;
                            if (cambio <= 0) cambio =0;
                            tvCambio.setText(String.format(Locale.UK,"%01.2f €", cambio));
                            break;


                        case "CASHLOGY_COBRO_COMPLETADO":
                            Toast.makeText(CobroCashlogyActivity.this, value, Toast.LENGTH_SHORT).show();
                            finalizarCobro();
                            break;

                        default:
                            Log.d("CASHLOGY", "Clave no reconocida: " + key);
                            break;
                    }
                }

                return true;
            }));

        }
    }


    public void finalizarCobro(){
        Intent resultData = new Intent();
        double totalIngresado = Double.parseDouble(tvTotalIngresado.getText().toString().replace("€", "").trim());
        double cambio = Double.parseDouble(tvCambio.getText().toString().replace("€", "").trim());

        // Añadir los datos al Intent
        resultData.putExtra("totalIngresado", totalIngresado);
        resultData.putExtra("cambio", cambio);

        // Añadir los datos que recibiste al iniciar la actividad
        resultData.putExtra("totalMesa", totalMesa);
        resultData.putExtra("lineas", lineas);  // Convertir el JSONArray a String


        // Establecer el resultado de la actividad
        setResult(Activity.RESULT_OK, resultData);
        finish();
    }

    // Método para manejar la acción de finalizar el cobro parcial
    public void finalizarCobroParcial(View view) {
        if (paymentAction != null) {
            if (paymentAction.sePuedeCobrar()) {
                paymentAction.cobrar();  // Finalizar el proceso de cobro utilizando PaymentAction
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConexion); // Desvincular el servicio cuando la actividad se destruya
    }
}
