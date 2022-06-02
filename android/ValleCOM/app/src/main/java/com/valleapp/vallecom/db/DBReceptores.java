package com.valleapp.vallecom.db;

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
        super(context, "receptores");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS receptores (ID INTEGER PRIMARY KEY, nombre TEXT)");
    }

    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject receptor = new JSONObject();
        try {
            receptor.put("nombre", res.getString(res.getColumnIndex("nombre")));
            receptor.put("ID", res.getString(res.getColumnIndex("ID")));
        }catch (Exception e){
            e.printStackTrace();
        }

        return receptor;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues v = new ContentValues();
        try {
            v.put("nombre", o.getString("nombre"));
            v.put("ID", o.getString("ID"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return  v;
    }


    public ArrayList<JSONObject> getAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from receptores WHERE nombre NOT LIKE '%Nulo%' ", null );
        res.moveToFirst();
        ArrayList<JSONObject>  ls = new ArrayList<>();
        res.moveToFirst();
        while(!res.isAfterLast()){
            ls.add(cursorToJSON(res));
            res.moveToNext();
        }
        return ls;
    }


}
