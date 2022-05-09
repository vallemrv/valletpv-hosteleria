package com.valleapp.comandas.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by valle on 13/10/14.
 */
public class DBCamareros extends DBBase {

    public DBCamareros(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS camareros (ID INTEGER PRIMARY KEY, nombre TEXT, " +
                                                          "pass_field TEXT, " +
                                                          "autorizado TEXT, " +
                                                          "permisos TEXT, flag TEXT default '' )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS camareros");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public JSONArray getAll()
    {
        return filter(null);
    }

    @Override
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
                values.put("ID", camareros.getJSONObject(i).getInt("id"));
                values.put("pass_field", camareros.getJSONObject(i).getString("pass_field"));
                values.put("autorizado", camareros.getJSONObject(i).getString("autorizado"));
                values.put("nombre", camareros.getJSONObject(i).getString("nombre") + " " + camareros.getJSONObject(i).getString("apellidos"));
                values.put("permisos", camareros.getJSONObject(i).getString("permisos"));
                db.insert("camareros", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }


    @Override
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


}
