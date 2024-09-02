package com.valleapp.valletpv.cashlogyActivitis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.R;

public class ArqueoCashlogyActivity extends Activity {

    // Declarar los elementos UI
    private TextView tvInformacionSalida;
    private Button btnCerrarCaja;
    private Button btnRetirarCambio;
    private Button btnRetirarTodo;
    private ImageButton btnSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arqueo_cashlogy);

        // Vincular los elementos de la UI
        tvInformacionSalida = findViewById(R.id.tvInformacionSalida);
        btnCerrarCaja = findViewById(R.id.btnCerrarCaja);
        btnRetirarCambio = findViewById(R.id.btnRetirarCambio);
        btnRetirarTodo = findViewById(R.id.btnRetirarTodo);
        btnSalir = findViewById(R.id.btnSalir);

        // Configurar los listeners de los botones
        btnCerrarCaja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción de cerrar caja
                Toast.makeText(ArqueoCashlogyActivity.this, "Cerrar caja", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetirarCambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción de retirar cambio
                Toast.makeText(ArqueoCashlogyActivity.this, "Retirar cambio", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetirarTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción de retirar todos
                Toast.makeText(ArqueoCashlogyActivity.this, "Retirar todos", Toast.LENGTH_SHORT).show();
            }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción de salir de la actividad
                finish();
            }
        });
    }
}
