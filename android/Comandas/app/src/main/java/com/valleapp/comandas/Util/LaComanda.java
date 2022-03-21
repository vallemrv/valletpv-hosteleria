package com.valleapp.comandas.Util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.valleapp.comandas.IUtil.IComanda;
import com.valleapp.comandas.R;

/**
 * Created by valle on 12/09/14.
 */
public class LaComanda extends Fragment {
    // Store instance variables
    ListView listaPedidos;
    TextView Can_art;
    EditText mensaje;
    IComanda controlador;

     public LaComanda(){}

    @SuppressLint("ValidFragment")
    public LaComanda(IComanda ctr){
         this.controlador = ctr;
     }
    // Store instance variables based on arguments passed

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controlador.CargarNota();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lacomanda, container, false);
        listaPedidos = view.findViewById(R.id.linear_layout);
        Can_art = view.findViewById(R.id.numArt);
        mensaje = view.findViewById(R.id.mensaje);
        return view;
    }

    public void setCantidad(String can){
        Can_art.setText("Hay "+ can +" articulos");
    }

    public ListView getLista(){
        return listaPedidos;
    }

    public String getMensaje(){
         return mensaje.getText().toString();
    }


}
