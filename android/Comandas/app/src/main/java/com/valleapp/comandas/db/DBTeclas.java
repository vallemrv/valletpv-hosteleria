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
public class DbTeclas extends SQLiteOpenHelper implements IBaseDatos {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DbTeclas(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS  teclas (" +
                "ID INTEGER PRIMARY KEY, Nombre TEXT, P1 DOUBLE, P2 DOUBLE, Precio DOUBLE," +
                " RGB TEXT, IDSeccion INTEGER, Tag TEXT, Orden INTEGER, IDSec2 INTEGER, " +
                " descripcion_t TEXT )");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS teclas");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @SuppressLint("Range")
    private JSONArray cargarRegistros(String sql, int tarifa){

        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( sql, null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject obj = new JSONObject();
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("ID", res.getString(res.getColumnIndex("ID")));
                obj.put("RGB", res.getString(res.getColumnIndex("RGB")));
                obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
                if (tarifa == 2)   obj.put("Precio", res.getString(res.getColumnIndex("P2")));
                else  obj.put("Precio", res.getString(res.getColumnIndex("P1")));
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return ls;

    }


    public JSONArray getAll(String id, int tarifa)
    {
        return cargarRegistros("SELECT * FROM teclas WHERE IDSeccion="+id +" OR IDSec2="+id+" ORDER BY Orden DESC", tarifa);
    }


    public JSONArray findLike(String str, String t) {
        return cargarRegistros("SELECT * FROM teclas WHERE Tag LIKE '%"+str+"%' ORDER BY Orden DESC LIMIT 15 ", Integer.parseInt(t));
    }


    @Override
    public void rellenarTabla(JSONArray datos) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM teclas");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
        // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<datos.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                ContentValues values = new ContentValues();
                values.put("ID", datos.getJSONObject(i).getInt("id"));
                values.put("IDSeccion", datos.getJSONObject(i).getInt("IDSeccion"));
                values.put("Nombre", datos.getJSONObject(i).getString("nombre"));
                values.put("P1", datos.getJSONObject(i).getDouble("p1"));
                values.put("P2", datos.getJSONObject(i).getDouble("p2"));
                values.put("Precio", datos.getJSONObject(i).getDouble("Precio"));
                values.put("RGB", datos.getJSONObject(i).getString("RGB"));
                values.put("Tag", datos.getJSONObject(i).getString("tag"));
                values.put("IDSec2", datos.getJSONObject(i).getString("IDSec2"));
                values.put("Orden", datos.getJSONObject(i).getString("orden"));
                values.put("descripcion_t", datos.getJSONObject(i).getString("descripcion_t"));
                db.insert("teclas", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }


    @Override //En esta clase no son necesarios pero hay que implementarlos
    public void resetFlag(int id) {

    }

    @Override
    public JSONArray filter(String cWhere) {
        return null;
    }

}
