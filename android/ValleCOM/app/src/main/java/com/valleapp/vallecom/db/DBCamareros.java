package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.valleapp.vallecom.interfaces.IBaseDatos;
import com.valleapp.vallecom.interfaces.IBaseSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by valle on 13/10/14.
 */
public class DBCamareros extends SQLiteOpenHelper implements IBaseDatos, IBaseSocket {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";

    public DBCamareros(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE IF NOT EXISTS camareros (ID INTEGER PRIMARY KEY, nombre TEXT, activo TEXT," +
                                                          "pass_field TEXT, " +
                                                          "autorizado TEXT, " +
                                                          "permisos TEXT)");
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
                 values.put("ID", camareros.getJSONObject(i).getInt("id"));
                 values.put("activo", camareros.getJSONObject(i).getString("activo"));
                 values.put("pass_field", camareros.getJSONObject(i).getString("pass_field"));
                 values.put("autorizado", camareros.getJSONObject(i).getString("autorizado"));
                 values.put("nombre", camareros.getJSONObject(i).getString("nombre") + " " + camareros.getJSONObject(i).getString("apellidos"));
                 values.put("permisos", camareros.getJSONObject(i).getString("permisos"));
                 db.insert("camareros", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void inicializar() {
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
    }

    public JSONArray getAll()
    {
        return filter(null);
    }

    @SuppressLint("Range")
    public JSONArray filter(String cWhere){
        JSONArray lscam = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        String q = " WHERE activo='1' ";
        if (cWhere != null){
            q = q +" and "+ cWhere;
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

        return lscam;
    }


    public ArrayList<JSONObject> getAutorizados(Boolean a)
    {
        String autorizado = a ? "1" : "0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from camareros WHERE activo='1' and autorizado = '"+ autorizado + "'", null );
        ArrayList<JSONObject>  lscam = cargarRegistros(res);

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

        return lscam;
    }


    public void insert(JSONObject obj) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues v = new ContentValues();
            v.put("ID", obj.getInt("id"));
            v.put("activo", obj.getString("activo"));
            v.put("pass_field", obj.getString("pass_field"));
            v.put("autorizado", obj.getString("autorizado"));
            v.put("nombre", obj.getString("nombre") + " " + obj.getString("apellidos"));
            v.put("permisos", obj.getString("permisos"));
            db.insert("camareros", null, v);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void update(JSONObject obj) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues v = new ContentValues();
            v.put("pass_field", obj.getString("pass_field"));
            v.put("autorizado", obj.getString("autorizado"));
            v.put("nombre", obj.getString("nombre") + " " + obj.getString("apellidos"));
            v.put("permisos", obj.getString("permisos"));
            db.update("camareros", v, "ID=?", new String[]{obj.getString("id")});
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void rm(JSONObject obj) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
           db.delete("camareros", "ID=?", new String[]{obj.getString("id")});
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public boolean is_autorizado(JSONObject cam) throws JSONException {
        SQLiteDatabase db = getReadableDatabase();
        int count = 0;
        try {
            Cursor mCount = db.rawQuery("select count(*) from camareros WHERE ID=" + cam.getString("ID") + " AND autorizado = '1'", null);
            mCount.moveToFirst();
            count = mCount.getInt(0);
        }catch (Exception e){
            e.printStackTrace();
        }

        return count > 0;
    }

    public void showDatos(String cWhere){
        SQLiteDatabase db = getReadableDatabase();
        String w = "";
        if (cWhere != null){
            w = " WHERE "+cWhere;
        }
        Cursor res= db.rawQuery("select * from camareros "+ w, null);
        res.moveToFirst();
        while (!res.isAfterLast()){
            String dta = "";
            for (int i=0; i< res.getColumnCount(); i++) {
                dta += res.getColumnName(i)+ "="+ res.getString(i) + " - ";
            }
            Log.i("SHOWDATA", dta);
            res.moveToNext();
        }

    }
}
