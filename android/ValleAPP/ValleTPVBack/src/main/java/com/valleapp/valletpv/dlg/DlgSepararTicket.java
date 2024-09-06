package com.valleapp.valletpv.dlg;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.valleapp.valletpv.R;
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSepararTicket;
import com.valleapp.valletpv.interfaces.IControladorCuenta;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by valle on 19/10/14.
 */
public class DlgSepararTicket extends Dialog{

    IControladorCuenta controlador;
    Double totalCobro = 0.00;
    ListView lstArt;

    ArrayList<JSONObject> lineasTicket = new ArrayList<>();
    ArrayList<JSONObject> separados = new ArrayList<>();




    public DlgSepararTicket(Context context, IControladorCuenta controlador ) {
        super(context);
        this.controlador = controlador;
        setContentView(R.layout.separarticket);

        final TextView tot = findViewById(R.id.lblTotalCobro);
        ImageButton ok = findViewById(R.id.btn_guardar_preferencias);
        ImageButton s = findViewById(R.id.btn_salir_monedas);

        final ListView lstCobros = findViewById(R.id.lstCobros);

        lstArt = findViewById(R.id.lstArticulos);

         ok.setOnClickListener(view -> clickCobrarSeparados(view));

         s.setOnClickListener(view -> clickSalirSeparados());

        tot.setText(String.format("Total cobro %01.2f €", totalCobro));

        lstArt.setOnItemClickListener((adapterView, view, i, l) -> {
            try {
                JSONObject art = (JSONObject)view.getTag();
                int can = art.getInt("Can");
                int canCobro = art.getInt("CanCobro")+1;
                if(canCobro<=can) {
                    totalCobro += art.getDouble("Precio");
                    tot.setText(String.format("Total cobro %01.2f €", totalCobro));
                    art.put("CanCobro", canCobro);
                    if (can==canCobro) lineasTicket.remove(art);
                    if (canCobro == 1) separados.add(art);
                    lstCobros.setAdapter(new AdaptadorSepararTicket(getContext(), separados, true));
                    lstArt.setAdapter(new AdaptadorSepararTicket(getContext(), lineasTicket, false));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });



        lstCobros.setOnItemClickListener((adapterView, view, i, l) -> {
        try {
            JSONObject art = (JSONObject)view.getTag();
            int can = art.getInt("Can");
            int canCobro = art.getInt("CanCobro")-1;
            totalCobro -= art.getDouble("Precio");
            tot.setText(String.format("Total cobro %01.2f €", totalCobro));
            art.put("CanCobro", canCobro);
            if (can > canCobro){
                if (!lineasTicket.contains(art))
                    lineasTicket.add(art);
            }
            if (canCobro == 0) separados.remove(art);
            lstCobros.setAdapter(new AdaptadorSepararTicket(getContext(), separados, true));
            lstArt.setAdapter(new AdaptadorSepararTicket(getContext(), lineasTicket, false));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        });

    }

    public void setLineasTicket(List<JSONObject> lsart) throws JSONException {
        lineasTicket = (ArrayList<JSONObject>) lsart;
        lstArt.setAdapter(new AdaptadorSepararTicket(getContext(), lineasTicket, false));
     }


    public void clickCobrarSeparados(View v){
        JSONArray arts = new JSONArray();
        for(int i=0;i<separados.size();i++){
            try {
                JSONObject art = separados.get(i);
                art.put("Can", art.getString("CanCobro"));
                arts.put(separados.get(i));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        cancel();
        controlador.mostrarCobrar(arts, totalCobro);
    }

    public void clickSalirSeparados(){
        cancel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        controlador.setEstadoAutoFinish(true, false);
    }
}
