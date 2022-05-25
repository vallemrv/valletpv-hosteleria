package com.valleapp.valleCOM.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.valleapp.valleCOM.R;
import com.valleapp.valleCOM.adaptadores.AdaptadorRefill;
import com.valleapp.valleCOM.db.DBCuenta;
import com.valleapp.valleCOM.utilidades.ActivityBase;

import org.json.JSONObject;

import java.util.ArrayList;

public class Refill extends ActivityBase {


    DBCuenta dbCuenta = new DBCuenta(this);
    String idMesa;

    public void mostrarLista(){
        ListView ls = findViewById(R.id.listaPedidosRefill);
        ArrayList<JSONObject> lista = dbCuenta.getPedidosChoices(idMesa);
        ls.setAdapter(new AdaptadorRefill(this, lista));
        ls.setOnItemClickListener((adapterView, view, i, l) -> {
            try{
            Intent it = getIntent();
            it.putExtra("IDPedido", lista.get(i).getString("IDPedido"));
            setResult(RESULT_OK, it);
            finish();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill);
        idMesa = getIntent().getExtras().getString("id_mesa");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarLista();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
