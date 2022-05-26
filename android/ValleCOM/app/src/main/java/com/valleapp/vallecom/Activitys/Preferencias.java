package com.valleapp.vallecom.Activitys;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.valleapp.vallecom.R;
import com.valleapp.vallecom.db.DBTbUpdates;
import com.valleapp.vallecom.utilidades.JSON;
import com.valleapp.vallecom.adaptadores.AdaptadorEmpresas;


public class Preferencias extends Activity {

    Context cx;
    JSONArray lista;
    JSONObject empresa_activa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);
        cx = this;
        empresa_activa = cargarEmpresaActiva();
        JSONObject obj = cargarListado();
        rellenarLista(obj);
    }

    private void rellenarLista(JSONObject obj) {
        if (obj != null){
            try {

                lista = obj.getJSONArray("lista");
                List<JSONObject> lempresa = new ArrayList<>();
                for(int i=0; i < lista.length(); i++){
                    JSONObject empresa = lista.getJSONObject(i);
                    if (empresa_activa != null && empresa_activa.getString("URL").equals(empresa.getString("URL"))){
                        empresa.put("activo" , true);
                    }else {
                        empresa.put("activo", false);
                    }
                    lempresa.add(lista.getJSONObject(i));
                }

                ListView lst = findViewById(R.id.lstEmpresa);
                lst.setAdapter(new AdaptadorEmpresas(cx, lempresa));


            }catch (Exception e){
                e.printStackTrace();
            }

        } else{

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void borrarEmpresa(View v){
        try {
            Integer position = (Integer)v.getTag();
            JSON json = new JSON();
            lista.remove(position);
            JSONObject obj = new JSONObject();
            obj.put("lista", lista);
            json.serializar("lista_empresas.dat", obj, cx);
            rellenarLista(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void clickSelEmpresa(View v){
        try {
            String url = ((JSONObject)v.getTag()).getString("URL");
            String nombre = ((JSONObject)v.getTag()).getString("nombre");
            JSON json = new JSON();
            if (empresa_activa == null) empresa_activa = new JSONObject();
            empresa_activa.put("URL", url);
            empresa_activa.put("nombre", nombre);
            json.serializar("preferencias.dat", empresa_activa, cx);
            Toast toast= Toast.makeText(getApplicationContext(),
                    "Cambios guardados con exito", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 200);
            toast.show();
            JSONObject obj = new JSONObject();
            obj.put("lista", lista);
            rellenarLista(obj);
            DBTbUpdates db = new DBTbUpdates(cx);
            db.vaciar();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public  void clickAddEmpresa(View v){
        final Dialog crearEmpresa = new Dialog(cx);
        crearEmpresa.setTitle("Crear empresa");
        crearEmpresa.setContentView(R.layout.dialog_crear_empresa);
        final TextView url = crearEmpresa.findViewById(R.id.txt_URL);
        final TextView nombre = crearEmpresa.findViewById(R.id.txt_nombre_empresa);

        crearEmpresa.findViewById(R.id.add_empresa).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   try {
                       JSONObject obj = new JSONObject();
                       JSON json = new JSON();
                       obj.put("URL", url.getText().toString());
                       obj.put("nombre", nombre.getText().toString());
                       if(lista == null) lista = new JSONArray();
                       lista.put(obj);
                       obj = new JSONObject();
                       obj.put("lista", lista);
                       json.serializar("lista_empresas.dat", obj, cx);
                       rellenarLista(obj);
                       crearEmpresa.dismiss();
                   }catch (Exception e){
                       e.printStackTrace();
                   }
                }
            }
        );
        crearEmpresa.show();
    }



    private JSONObject cargarListado() {
        JSON json = new JSON();
        try {
            return json.deserializar("lista_empresas.dat", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    private JSONObject cargarEmpresaActiva() {
        JSON json = new JSON();
        try {
            return json.deserializar("preferencias.dat", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }



}
