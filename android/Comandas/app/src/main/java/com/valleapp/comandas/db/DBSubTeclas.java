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
public class DbSubTeclas extends SQLiteOpenHelper implements IBaseDatos {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DbSubTeclas(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS subteclas (ID INTEGER PRIMARY KEY, Nombre TEXT, Incremento DOUBLE, IDTecla INTEGER)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS subteclas");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    @Override
    public void resetFlag(int id) {

    }

    @Override
    public JSONArray filter(String cWhere) {
        return null;
    }

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
                 values.put("Incremento", datos.getJSONObject(i).getString("incremento"));
                 values.put("IDTecla", datos.getJSONObject(i).getString("idtecla"));
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
        while(res.isAfterLast() == false){
            try{
                JSONObject obj = new JSONObject();
                obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                obj.put("Incremento", res.getString(res.getColumnIndex("Incremento")));
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
