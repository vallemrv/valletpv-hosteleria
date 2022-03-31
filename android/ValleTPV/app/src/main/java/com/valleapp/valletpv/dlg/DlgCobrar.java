package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.valleapp.valletpv.Interfaces.IControlador;
import com.valleapp.valletpv.R;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by valle on 19/10/14.
 */
public class DlgCobrar extends Dialog{

    JSONArray lineas;
    IControlador controlador;
    String strEntrega = "";
    Double totalCobro = 0.00;
    Double entrega = 0.00;
    TextView lblEntrega;
    TextView lblCambio;
    TextView lbltotal;

    public DlgCobrar(Context context, IControlador controlador) {
        super(context);
        this.controlador = controlador;
        setContentView(R.layout.cobros);
        lbltotal = (TextView) findViewById(R.id.lblPrecio);
        lblEntrega = (TextView) findViewById(R.id.lblEntrega);
        lblCambio = (TextView) findViewById(R.id.lblCambio);
        Button tj = (Button)findViewById(R.id.btnTarjeta);
        ImageButton ef = (ImageButton)findViewById(R.id.btnEfectivo);
        ImageButton s = (ImageButton)findViewById(R.id.btnSalir);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickSalir(view);
            }
        });
        ef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickEfectivo(view);
            }
        });

        tj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickTarjeta(view);
            }
        });
    }

    public void setDatos(JSONArray lineas, Double totalCobro){
        this.lineas = lineas;
        lbltotal.setText(String.format("%01.2f €", totalCobro));
        lblEntrega.setText(String.format("%01.2f €", totalCobro));
        this.totalCobro = totalCobro;
        this.entrega = totalCobro;
        this.strEntrega = "";
        LinearLayout pne = (LinearLayout)findViewById(R.id.pneBotonera);
        ArrayList<View> touchables = pne.getTouchables();

        for (View v : touchables){
            if (v instanceof Button){
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickEntrega(view);
                    }
                });
            }
        }
    }


    public void clickEfectivo(View v){
        if(entrega>=totalCobro) {
            controlador.cobrar(lineas,totalCobro,entrega);
            this.cancel();
        }
    }

    public void clickTarjeta(View v){
        Log.i("DLGCobrar", String.valueOf(entrega));
        if (entrega == totalCobro) {
            controlador.cobrar(lineas, totalCobro, 0.00);
            this.cancel();
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
        super.onStop();
        controlador.setEstadoAutoFinish(true, false);
    }
}
