package com.valleapp.vallecom.utilidades;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.valleapp.vallecom.interfaces.INota;

/**
 * Created by valle on 18/09/14.
 */
public class Nota {

    private List<JSONObject> comanda = new ArrayList<>();

    private int num= 0;

    String nombre;
    Context cx;
    INota controlador;
    JSONObject artSel = null;


    public Nota(JSONObject mesa, Context cx, INota ctr){
        try {
          this.nombre = mesa.getString("Nombre");
         } catch (Exception e) {
            e.printStackTrace();
        }
        this.cx = cx; this.controlador = ctr;
        this.cargarComanda();
    }


    public int getNum() {
        return num;
    }

    public String getArt(JSONObject art){
        this.artSel = art;
        return this.artSel.toString();
    }


    public List<JSONObject> getLineas() {
        return this.comanda;
    }

    public void rmArt(JSONObject art){
        try {
           this.num--;
           comanda.remove(art);
           this.guardarComanda();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void addArt(JSONObject art, int can){
        try {
            this.num+=can;
            for(int i = 0;i<can;i++){
                art = new JSONObject(art.toString());
                art.put("Can", 1);
                comanda.add(art);
            }
            this.guardarComanda();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void cargarComanda(){
        JSON json = new JSON();
        try {
            JSONObject cm = json.deserializar(nombre+".dat", cx);
            comanda = new ArrayList<>();
            if(cm==null)  num = 0;
            else{
                JSONArray l = new JSONArray(cm.get("lineas").toString());
                this.num = cm.getInt("num");
                for(int i=0;i<l.length();i++){
                    JSONObject art = l.getJSONObject(i);
                    comanda.add(art);
                  }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void guardarComanda(){
        JSON json = new JSON();
        try {
            JSONObject cm = new JSONObject();
            cm.put("num", this.num);
            cm.put("lineas", this.getLineas().toString());
            json.serializar(nombre + ".dat",cm, cx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        controlador.rellenarComanda();
    }


    public void eliminarComanda(){
        this.comanda = new ArrayList<>();
        this.num = 0;
        cx.deleteFile(nombre + ".dat");
        controlador.rellenarComanda();
     }

    public void addSug( String sug) {
        try{
            String nombre = this.artSel.getString("Descripcion")+" "+sug;
            this.artSel.put("Descripcion", nombre);
            this.guardarComanda();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
