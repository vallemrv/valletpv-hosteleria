package com.valleapp.valletpv.db;

import android.annotation.SuppressLint;
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
public class DbCamareros  extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public DbCamareros(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS camareros (ID INTEGER PRIMARY KEY, Nombre TEXT, Pass TEXT)");
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


    public void RellenarTabla(JSONArray camareros){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM camareros");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
       // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<camareros.length();i++){
            // Create a new map of values, where column names are the keys
            try {
                 ContentValues values = new ContentValues();
                 values.put("ID", camareros.getJSONObject(i).getInt("ID"));
                 values.put("Pass", camareros.getJSONObject(i).getString("Pass"));
                 values.put("Nombre", camareros.getJSONObject(i).getString("Nombre") + " " + camareros.getJSONObject(i).getString("Apellidos"));
                 db.insert("camareros", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        db.close();
    }

    @SuppressLint("Range")
    public JSONArray getAll()
    {
        JSONArray lscam = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from camareros", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject cam = new JSONObject();
                cam.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
                cam.put("ID", res.getString(res.getColumnIndex("ID")));
                cam.put("Pass", res.getString(res.getColumnIndex("Pass")));
                lscam.put(cam);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }
        res.close();db.close();
        return lscam;
    }


    public void Vaciar(){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM camareros");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
        db.close();
    }
}
