package com.valleapp.comandas.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


import com.valleapp.comandas.interfaces.IBaseDatos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by valle on 13/10/14.
 */
public class DBSecciones extends SQLiteOpenHelper implements IBaseDatos {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DBSecciones(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS secciones (ID INTEGER PRIMARY KEY, " +
                "nombre TEXT, " +
                "icono TEXT, es_promocion TEXT, descuento TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS secciones");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }



    @SuppressLint("Range")
    public JSONArray getAll()
    {
        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM secciones ORDER BY orden DESC" , null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject obj = new JSONObject();
                obj.put("nombre", res.getString(res.getColumnIndex("nombre")));
                obj.put("ID", res.getString(res.getColumnIndex("ID")));
                obj.put("icono", res.getString(res.getColumnIndex("icono")));
                obj.put("es_promocion", res.getString(res.getColumnIndex("es_promocion")));
                obj.put("descuento", res.getString(res.getColumnIndex("descuento")));
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return ls;
    }


    @Override
    public void resetFlag(int id) {

    }

    @Override
    public JSONArray filter(String cWhere) {
        return null;
    }

    @Override
    public void rellenarTabla(JSONArray datos) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM secciones");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
        // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<datos.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                ContentValues values = new ContentValues();
                values.put("ID", datos.getJSONObject(i).getInt("id"));
                values.put("nombre", datos.getJSONObject(i).getString("nombre"));
                values.put("icono", datos.getJSONObject(i).getString("icono"));
                values.put("es_promocion", datos.getJSONObject(i).getString("es_promocion"));
                values.put("descuento", datos.getJSONObject(i).getString("descuento"));
                db.insert("secciones", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }
}
