package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.valleapp.valletpv.interfaces.IControladorCuenta;
import com.valleapp.valletpv.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by valle on 19/10/14.
 */
public class DlgCobrar extends Dialog{

    JSONArray lineas;
    IControladorCuenta controlador;
    String strEntrega = "";
    Double totalCobro = 0.00;
    Double entrega = 0.00;
    TextView lblEntrega;
    TextView lblCambio;
    TextView lbltotal;
    boolean usaCashlogy;

    public DlgCobrar(Context context, IControladorCuenta controlador, boolean usaCashlogy) {
        super(context);
        this.usaCashlogy = usaCashlogy;
        this.controlador = controlador;
        setContentView(R.layout.cobros);
        lbltotal = findViewById(R.id.lblPrecio);
        lblEntrega =  findViewById(R.id.lblEntrega);
        lblCambio = findViewById(R.id.lblCambio);
        ImageButton tj = findViewById(R.id.btnTarjeta);
        ImageButton ef = findViewById(R.id.btnEfectivo);
        ImageButton s = findViewById(R.id.btn_salir_monedas);
        LinearLayout pne = findViewById(R.id.pneBotonera);

        // Ocultar el LinearLayout si usaCashlogy es true
        if (usaCashlogy) {
            pne.setVisibility(View.GONE);
        }

        s.setOnClickListener(this::clickSalir);
        ef.setOnClickListener(this::clickEfectivo);
        tj.setOnClickListener(this::clickTarjeta);
    }

    public void setDatos(JSONArray lineas, Double totalCobro){
        this.lineas = lineas;
        lbltotal.setText(String.format("%01.2f €", totalCobro));
        lblEntrega.setText(String.format("%01.2f €", totalCobro));
        this.totalCobro = totalCobro;
        this.entrega = totalCobro;
        this.strEntrega = "";
        LinearLayout pne = findViewById(R.id.pneBotonera);
        ArrayList<View> touchables = pne.getTouchables();

        for (View v : touchables){
            if (v instanceof Button){
                v.setOnClickListener(view -> clickEntrega(view));
            }
        }
    }


    public void clickEfectivo(View v){
        if (usaCashlogy){
            controlador.cobrarConCashlogy(lineas, totalCobro);
            clickSalir(v);
            return;
        }
        if(entrega>=totalCobro) {
            clickSalir(v);
            controlador.cobrar(lineas, totalCobro, entrega);
        }
    }

    public void clickTarjeta(View v){
        if (Objects.equals(entrega, totalCobro)) {
            clickSalir(v);
            controlador.cobrar(lineas, totalCobro, 0.00);
        }
    }

    public void clickSalir(View v){
       cancel();
    }

    public void clickEntrega(View v){
        String caracter = v.getTag().toString();
        if(caracter.equals("C")){
            entrega = totalCobro; strEntrega="";
            lblEntrega.setText(String.format("%01.2f €", totalCobro));
            lblCambio.setText("0.00 €");
        }else{
            try {
                strEntrega+=caracter;
                entrega = Double.parseDouble(strEntrega);
                lblEntrega.setText(String.format("%01.2f €", entrega));
                if(entrega>totalCobro)  lblCambio.setText(String.format("%01.2f €", entrega-totalCobro));
            }catch (Exception e){
                entrega = totalCobro; strEntrega= "";
                lblEntrega.setText(String.format("%01.2f €", totalCobro));
                lblCambio.setText("0.00 €");
            }
        }

    }

    @Override
    protected void onStop() {
        controlador.setEstadoAutoFinish(true, false);
        super.onStop();
    }
}
