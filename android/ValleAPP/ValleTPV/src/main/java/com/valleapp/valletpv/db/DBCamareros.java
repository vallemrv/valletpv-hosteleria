package com.valleapp.valletpv.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by valle on 13/10/14.
 */
public class DBCamareros extends DBBase{


    public DBCamareros(Context context) {
        super(context, "camareros");
    }

    public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE IF NOT EXISTS camareros (ID INTEGER PRIMARY KEY, " +
                                                          "nombre TEXT, apellidos TEXT," +
                                                          "activo TEXT," +
                                                          "pass_field TEXT, " +
                                                          "autorizado TEXT, " +
                                                          "permisos TEXT)");

    }


    @Override
    public void inicializar() {
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
    }

    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject cam = new JSONObject();
        try {
            cam.put("nombre", res.getString(res.getColumnIndex("nombre")));
            cam.put("apellidos", res.getString(res.getColumnIndex("apellidos")));
            cam.put("ID", res.getString(res.getColumnIndex("ID")));
            cam.put("pass_field", res.getString(res.getColumnIndex("pass_field")));
            cam.put("autorizado", res.getString(res.getColumnIndex("autorizado")));
            cam.put("permisos", res.getString(res.getColumnIndex("permisos")));
            Log.e("AUTORIZACiON", cam.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return cam;
    }

    @Override
    protected ContentValues caragarValues(JSONObject obj) {
        ContentValues v = new ContentValues();
        try{
            String id = obj.getString("id");
            v.put("ID", id);
            v.put("activo", obj.getString("activo"));
            v.put("pass_field", obj.getString("pass_field"));
            v.put("autorizado", obj.getString("autorizado"));
            v.put("nombre",  obj.getString("nombre"));
            v.put("apellidos",  obj.getString("apellidos"));
            v.put("permisos", obj.getString("permisos"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return v;
    }

    public JSONArray getAll()
    {
        return filter("activo=1");
    }

    public void setAutorizado(int id, Boolean a){
        String autorizado = a ? "1" : "0";
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("autorizado", autorizado);
        db.update("camareros",  cv, "ID="+id, null);
    }

    public ArrayList<JSONObject> getAutorizados(Boolean a)
    {
        String autorizado = a ? "1" : "0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from camareros WHERE activo='1' and autorizado = '"+ autorizado + "'", null );
        return cargarRegistros(res);
    }

    @SuppressLint("Range")
    private ArrayList<JSONObject> cargarRegistros(Cursor res) {

        ArrayList<JSONObject>  lscam = new ArrayList<>();
        res.moveToFirst();
        while(!res.isAfterLast()){
           lscam.add(cursorToJSON(res));
            res.moveToNext();
        }
        return lscam;
    }


    public ArrayList<JSONObject> getConPermiso(String permiso) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from camareros where activo='1' and permisos  LIKE '%"+permiso+"%' ", null );
        return cargarRegistros(res);
    }


    public void addCamNuevo(String n, String a) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues v = new ContentValues();
            v.put("activo", 1);
            v.put("pass_field", "");
            v.put("autorizado", 1);
            v.put("nombre", n );
            v.put("apellidos",  a);
            v.put("permisos", "");
            db.insert("camareros", null, v);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
