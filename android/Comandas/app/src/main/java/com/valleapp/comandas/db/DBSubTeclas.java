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
public class DBSubTeclas extends DBBase  {


    public DBSubTeclas(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS subteclas (ID INTEGER PRIMARY KEY, " +
                "Nombre TEXT, Incremento DOUBLE, " +
                "IDTecla INTEGER, " +
                "descripcion_r TEXT, descripcion_t TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS subteclas");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void rellenarTabla(JSONArray datos){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try {
           db.execSQL("DELETE FROM subteclas");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
       // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<datos.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                 ContentValues values = new ContentValues();
                 values.put("ID", datos.getJSONObject(i).getInt("id"));
                 values.put("Nombre", datos.getJSONObject(i).getString("nombre"));
                 values.put("descripcion_t", datos.getJSONObject(i).getString("descripcion_t"));
                 values.put("descripcion_r", datos.getJSONObject(i).getString("descripcion_r"));
                 values.put("Incremento", datos.getJSONObject(i).getString("incremento"));
                 values.put("IDTecla", datos.getJSONObject(i).getString("tecla"));
                 db.insert("subteclas", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }

    @SuppressLint("Range")
    public JSONArray getAll(String id)
    {
        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM subteclas WHERE IDTecla="+id , null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject obj = new JSONObject();
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("Incremento", res.getString(res.getColumnIndex("Incremento")));
                obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
                obj.put("descripcion_r", res.getString(res.getColumnIndex("descripcion_r")));
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return ls;
    }





}
