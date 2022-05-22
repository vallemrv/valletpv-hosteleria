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
public class DBReceptores extends DBBase {

    public DBReceptores(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS receptores (ID INTEGER PRIMARY KEY, nombre TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS receptores");
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
        try{
            db.execSQL("DELETE FROM camareros");
        }catch (SQLiteException e){
            this.onCreate(db);
        }
        // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i < datos.length(); i++){
            // Create a new map of values, where column names are the keys
            try {
                ContentValues values = new ContentValues();
                values.put("ID", datos.getJSONObject(i).getInt("id"));
                values.put("nombre", datos.getJSONObject(i).getString("nombre"));
                db.insert("receptores", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }



    @SuppressLint("Range")
    public ArrayList<JSONObject> getAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from receptores WHERE nombre NOT LIKE '%Nulo%' ", null );
        res.moveToFirst();
        ArrayList<JSONObject>  ls = new ArrayList<>();
        res.moveToFirst();

        while(!res.isAfterLast()){
            try{
                JSONObject receptor = new JSONObject();
                receptor.put("nombre", res.getString(res.getColumnIndex("nombre")));
                receptor.put("ID", res.getString(res.getColumnIndex("ID")));
                ls.add(receptor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            res.moveToNext();
        }
     ;
        return ls;
    }


}
