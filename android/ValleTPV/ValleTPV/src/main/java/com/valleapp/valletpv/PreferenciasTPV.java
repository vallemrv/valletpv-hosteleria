package com.valleapp.valletpv;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.valleapp.valletpvlib.tools.JSON;

import org.json.JSONException;
import org.json.JSONObject;


public class PreferenciasTPV extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias_tpv);

        final Context cx = this;

        // Referencias a los elementos de la interfaz
        final EditText txtUrl = findViewById(R.id.txtUrl);
        final EditText txtCashlogyUrl = findViewById(R.id.txtCashlogyConnector);
        final EditText txtTPVPCUrl = findViewById(R.id.txtTPVPCServer);  // Nuevo campo para IP TPVPC
        final CheckBox chkUseCashlogy = findViewById(R.id.chkUseCashlogy);
        final CheckBox chkUseTPVPC = findViewById(R.id.chkUseTPVPC);  // Nuevo CheckBox para usar TPVPC
        Button btn = findViewById(R.id.btn_aceptar_preferencias);

        // Cargar las preferencias desde el archivo
        JSONObject obj = cargarPreferencias();

        // Si el objeto JSON no es nulo, cargamos las preferencias
        if (obj != null) {
            try {
                // Cargar URL del servidor
                if (obj.has("URL")) {
                    txtUrl.setText(obj.getString("URL"));
                }

                // Cargar URL de Cashlogy
                if (obj.has("URL_Cashlogy")) {
                    txtCashlogyUrl.setText(obj.getString("URL_Cashlogy"));
                }

                // Cargar si se usa Cashlogy o no
                if (obj.has("usaCashlogy")) {
                    chkUseCashlogy.setChecked(obj.getBoolean("usaCashlogy"));
                }

                // Cargar si se usa TPVPC o no (nuevo)
                if (obj.has("usaTPVPC")) {
                    chkUseTPVPC.setChecked(obj.getBoolean("usaTPVPC"));
                }

                // Cargar la IP del servidor TPVPC (nuevo)
                if (obj.has("IP_TPVPC")) {
                    txtTPVPCUrl.setText(obj.getString("IP_TPVPC"));
                }

            } catch (JSONException e) {
                Log.e("PREFERENCIAS_ERR", e.toString());
            }
        }

        // Configurar el evento del botón para guardar las preferencias
        btn.setOnClickListener(view -> {
            String url = txtUrl.getText().toString();
            String urlCashlogy = txtCashlogyUrl.getText().toString();
            String urlTPVPC = txtTPVPCUrl.getText().toString();  // Obtener IP del servidor TPVPC
            boolean usarCashlogy = chkUseCashlogy.isChecked();
            boolean usarTPVPC = chkUseTPVPC.isChecked();  // Obtener valor de "Usar TPVPC"

            JSONObject obj1 = new JSONObject();
            try {
                // Guardar las preferencias en el objeto JSON
                obj1.put("URL", url);
                obj1.put("URL_Cashlogy", urlCashlogy);
                obj1.put("IP_TPVPC", urlTPVPC);  // Guardar la IP del servidor TPVPC
                obj1.put("usaCashlogy", usarCashlogy);
                obj1.put("usaTPVPC", usarTPVPC);  // Guardar si se usa TPVPC

                // Serializar y guardar el JSON en un archivo
                JSON json = new JSON();
                json.serializar("preferencias.dat", obj1, cx);

                // Mostrar un mensaje de confirmación
                Toast.makeText(getApplicationContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show();

                // Finalizar la actividad
                finish();

            } catch (JSONException e) {
                Log.e("PREFERENCIAS_ERR", e.toString());
            }
        });
        ImageButton btnSalir = findViewById(R.id.btn_salir);
        btnSalir.setOnClickListener(view -> finish());
    }

    private JSONObject cargarPreferencias() {
        JSON json = new JSON();
        try {
            return json.deserializar("preferencias.dat", this);
        } catch (JSONException e) {
            Log.e("PREFERENCIAS_ERR", e.toString());
        }
        return null;
    }



}
