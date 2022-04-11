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


/**
 * Created by valle on 13/10/14.
 */
public class DBZonas extends DBBase{

    public DBZonas(Context context) {
        super(context);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS  zonas (ID INTEGER PRIMARY KEY, Nombre TEXT, RGB TEXT, Tarifa INTEGER)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS zonas");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    @SuppressLint("Range")
    public JSONArray getAll()
    {
        JSONArray lista = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from zonas", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject cam = new JSONObject();
                cam.put("Nombre",res.getString(res.getColumnIndex("Nombre")));
                cam.put("ID", res.getString(res.getColumnIndex("ID")));
                cam.put("Tarifa", res.getString(res.getColumnIndex("Tarifa")));
                cam.put("RGB", res.getString(res.getColumnIndex("RGB")));
                lista.put(cam);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return lista;
    }

    @Override
    public void rellenarTabla(JSONArray objs) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM zonas");
        }catch (SQLiteException e){
            this.onCreate(db);
        }

        // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<objs.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                ContentValues values = new ContentValues();
                values.put("ID", objs.getJSONObject(i).getInt("id"));
                values.put("Nombre", objs.getJSONObject(i).getString("nombre"));
                values.put("RGB", objs.getJSONObject(i).getString("rgb"));
                values.put("Tarifa", objs.getJSONObject(i).getString("tarifa"));
                db.insert("zonas", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }
}
