package com.valleapp.valleCOM.pestañas;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.valleapp.valleCOM.interfaces.IComanda;
import com.valleapp.valleCOM.R;

/**
 * Created by valle on 12/09/14.
 */
public class Comanda extends Fragment {
    // Store instance variables
    ListView listaPedidos;
    TextView Can_art;
    IComanda controlador;

     public Comanda(){}

    @SuppressLint("ValidFragment")
    public Comanda(IComanda ctr){
         this.controlador = ctr;
     }
    // Store instance variables based on arguments passed

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controlador.cargarNota();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comanda, container, false);
        listaPedidos = view.findViewById(R.id.listaPedidoComanda);
        Can_art = view.findViewById(R.id.numArt);
        return view;
    }

    @SuppressLint("SetTextI18n")
    public void setCantidad(String can){
        Can_art.setText("Hay "+ can +" articulos");
    }

    public ListView getLista(){
        return listaPedidos;
    }

}
