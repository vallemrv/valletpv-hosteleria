package com.valleapp.comandas.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by valle on 13/10/14.
 */
public class DbSecciones extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DbSecciones(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS  secciones (ID INTEGER PRIMARY KEY," +
                " Nombre TEXT, Descuento DOUBLE, Icono TEXT, Es_promocion BOOLEAN, Es_aplicable BOOLEAN)");
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


    public void RellenarTabla(JSONArray datos){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM secciones");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
       // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<datos.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                 ContentValues values = new ContentValues();
                 values.put("ID", datos.getJSONObject(i).getInt("ID"));
                 values.put("Nombre", datos.getJSONObject(i).getString("Nombre"));
                 values.put("Icono", datos.getJSONObject(i).getString("Icono"));
                 values.put("Descuento", datos.getJSONObject(i).getDouble("Descuento"));
                 values.put("Es_promocion", datos.getJSONObject(i).getBoolean("Es_promocion"));
                 values.put("Es_aplicable", true);
                 db.insert("secciones", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }


    public void updateRow(JSONObject datos){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        try {
            ContentValues values = new ContentValues();
            values.put("Nombre", datos.getString("Nombre"));
            values.put("Icono", datos.getString("Icono"));
            values.put("Descuento", datos.getDouble("Descuento"));
            values.put("Es_promocion", datos.getBoolean("Es_promocion"));
            values.put("Es_aplicable", datos.getBoolean("Es_aplicable"));
            db.update("secciones", values,  "ID = ?", new String[]{datos.getString("ID")});
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void allToAplicable(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM secciones", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            try{
                JSONObject obj = new JSONObject();
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("ID", res.getInt(res.getColumnIndex("ID")));
                obj.put("Icono", res.getString(res.getColumnIndex("Icono")));
                obj.put("Descuento", Double.parseDouble(res.getString(res.getColumnIndex("Descuento"))));
                obj.put("Es_promocion", res.getInt(res.getColumnIndex("Es_promocion")) > 0 ? true : false);
                obj.put("Es_aplicable", true);
                this.updateRow(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
    }

    public JSONArray getAll()
    {
        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM secciones", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            try{
                JSONObject obj = new JSONObject();
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("ID", res.getInt(res.getColumnIndex("ID")));
                obj.put("Icono", res.getString(res.getColumnIndex("Icono")));
                obj.put("Descuento", Double.parseDouble(res.getString(res.getColumnIndex("Descuento"))));
                obj.put("Es_promocion", res.getInt(res.getColumnIndex("Es_promocion")) > 0 ? true : false);
                obj.put("Es_aplicable", res.getInt(res.getColumnIndex("Es_aplicable")) > 0 ? true : false);
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return ls;
    }

    public JSONObject find(String nombre) throws JSONException {
        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM secciones WHERE Nombre ='"+nombre+"'", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            try{
                JSONObject obj = new JSONObject();
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("ID", res.getInt(res.getColumnIndex("ID")));
                obj.put("Icono", res.getString(res.getColumnIndex("Icono")));
                obj.put("Descuento", Double.parseDouble(res.getString(res.getColumnIndex("Descuento"))));
                obj.put("Es_promocion", res.getInt(res.getColumnIndex("Es_promocion")) > 0 ? true : false);
                obj.put("Es_aplicable", res.getInt(res.getColumnIndex("Es_aplicable")) > 0 ? true : false);
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return ls.getJSONObject(0);
    }



    public int getCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int s = 0;
        Cursor cursor = db.rawQuery("select count(*) from secciones", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
           s = cursor.getInt(0);
        }
        cursor.close();db.close();
        return  s;
    }

    public void Vaciar(){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
           db.execSQL("DELETE FROM secciones");
        }catch (SQLiteException e){
           this.onCreate(db);
        }
        db.close();
     }


}
