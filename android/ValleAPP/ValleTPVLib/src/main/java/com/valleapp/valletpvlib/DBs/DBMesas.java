package com.valleapp.valletpvlib.DBs;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;


/**
 * Created by valle on 13/10/14.
 */
public class DBMesas extends DBBase {

    public DBMesas(Context context) {
        super(context, "mesas");
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS mesas " +
                "(ID INTEGER PRIMARY KEY, Nombre TEXT, RGB TEXT, " +
                "abierta TEXT,  IDZona INTEGER, " +
                "num INTEGER, Orden INTEGER)");
    }



    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject obj = new JSONObject();
        try {
            int num = res.getInt(res.getColumnIndex("num"));
            String RGB = res.getString(res.getColumnIndex("RGB"));
            String nom = res.getString(res.getColumnIndex("Nombre"));
            obj.put("Nombre", nom == null ? "" : nom);
            obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
            obj.put("IDZona", res.getString(res.getColumnIndex("IDZona")));
            obj.put("RGB", num <= 0 ? RGB : "255,0,0");
            obj.put("abierta", res.getString(res.getColumnIndex("abierta")));
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("num", num);
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues values = new ContentValues();
        try {
            String abierta = o.getString("abierta").toLowerCase(Locale.ROOT);
            if (abierta.equals("true")) abierta = "1";
            else if (abierta.equals("false")) abierta = "0";
            values.put("ID", o.getInt("ID"));
            values.put("Nombre", o.getString("Nombre"));
            values.put("IDZona", o.getInt("IDZona"));
            values.put("RGB", o.getString("RGB"));
            values.put("abierta", abierta);
            values.put("num", o.getInt("num"));
            values.put("Orden", o.getInt("Orden"));
        }catch (Exception e){
            e.printStackTrace();
        }

        return  values;
    }

    @SuppressLint("Range")
    public JSONArray getAll(String id)
    {
        return filter("IDZona = "+id);
    }


    public void abrirMesa(String idm) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE mesas SET abierta='1', num=0 WHERE ID="+idm);
    }

    public void cerrarMesa(String idm) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE mesas SET abierta='0', num=0 WHERE ID="+idm);
    }

    public JSONArray getAllMenosUna(String id, String idm) {
        return filter("IDZona = "+id+" AND ID !=  "+idm);
    }


    @SuppressLint("Range")
    @Override
    public JSONArray filter(String cWhere) {
        JSONArray lista = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        String strWhere = "";
        if (cWhere != null){
            strWhere = " WHERE "+cWhere;
        }

        Cursor res =  db.rawQuery( "SELECT * FROM mesas "+strWhere+" ORDER BY Orden DESC", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            lista.put(cursorToJSON(res));
            res.moveToNext();
        }
        return lista;
    }

    public void marcarRojo(String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("num", "1");
        db.update("mesas", v, "ID = ?", new String[]{id});
    }
}
