package com.valleapp.valletpv.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.valleapp.valletpv.interfaces.IBaseDatos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by valle on 13/10/14.
 */
public class DbCamareros  extends SQLiteOpenHelper implements IBaseDatos {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DbCamareros(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS camareros (ID INTEGER PRIMARY KEY, nombre TEXT, " +
                                                          "pass_field TEXT, " +
                                                          "autorizado TEXT, " +
                                                          "permisos TEXT, flag TEXT default '' )");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS camareros");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void rellenarTabla(JSONArray camareros){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM camareros");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
       // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i < camareros.length(); i++){
            // Create a new map of values, where column names are the keys
            try {
                 ContentValues values = new ContentValues();
                 values.put("ID", camareros.getJSONObject(i).getInt("ID"));
                 values.put("pass_field", camareros.getJSONObject(i).getString("Pass"));
                 values.put("autorizado", camareros.getJSONObject(i).getString("autorizado"));
                 values.put("nombre", camareros.getJSONObject(i).getString("Nombre") + " " + camareros.getJSONObject(i).getString("Apellidos"));
                 values.put("permisos", camareros.getJSONObject(i).getString("permisos"));
                 db.insert("camareros", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }

    public JSONArray getAll()
    {
        return filter(null);
    }

    @SuppressLint("Range")
    public JSONArray filter(String cWhere){
        JSONArray lscam = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "";
        if (cWhere != null){
            q = " Where " + cWhere;
        }
        Cursor res =  db.rawQuery( "select * from camareros" + q, null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject cam = new JSONObject();
                cam.put("Nombre", res.getString(res.getColumnIndex("nombre")));
                cam.put("ID", res.getString(res.getColumnIndex("ID")));
                cam.put("Pass", res.getString(res.getColumnIndex("pass_field")));
                cam.put("autorizado", res.getString(res.getColumnIndex("autorizado")));
                cam.put("permisos", res.getString(res.getColumnIndex("permisos")));
                lscam.put(cam);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();
        db.close();
        return lscam;
    }



    public void setAutorizado(int id, Boolean a){
        String autorizado = a ? "1" : "0";
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("autorizado", autorizado);
        cv.put("flag", "modificado");
        db.update("camareros",  cv, "ID="+id, null);
        db.close();
    }

    public void resetFlag(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("flag", "");
        db.update("camareros",  cv, "ID="+id, null);
        db.close();
    }


    public ArrayList<JSONObject> getAutorizados(Boolean a)
    {
        String autorizado = a ? "1" : "0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from camareros WHERE autorizado = '"+ autorizado + "'", null );
        ArrayList<JSONObject>  lscam = cargarRegistros(res);
        db.close();
        return lscam;
    }

    @SuppressLint("Range")
    private ArrayList<JSONObject> cargarRegistros(Cursor res) {

        ArrayList<JSONObject>  lscam = new ArrayList<>();
        res.moveToFirst();

        while(!res.isAfterLast()){
            try{
                JSONObject cam = new JSONObject();
                cam.put("Nombre", res.getString(res.getColumnIndex("nombre")));
                cam.put("ID", res.getString(res.getColumnIndex("ID")));
                cam.put("Pass", res.getString(res.getColumnIndex("pass_field")));
                cam.put("autorizado", res.getString(res.getColumnIndex("autorizado")));
                cam.put("permisos", res.getString(res.getColumnIndex("permisos")));
                lscam.add(cam);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();
        return lscam;
    }



    public void addCamNuevo(String n, String a) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put("nombre", n + " " + a);
            v.put("pass_field", "");
            v.put("permisos", "");
            v.put("autorizado", "1");
            db.insert("camareros", null, v);
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<JSONObject> getConPermiso(String permiso) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from camareros where permisos  LIKE '%"+permiso+"%' AND autorizado = '1'", null );
        ArrayList<JSONObject>  lscam = cargarRegistros(res);
        db.close();
        return lscam;
    }


}
