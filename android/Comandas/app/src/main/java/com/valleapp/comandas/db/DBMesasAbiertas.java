package com.valleapp.comandas.db;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.valleapp.comandas.interfaces.IBaseDatos;
import com.valleapp.comandas.interfaces.IBaseSocket;


import org.json.JSONArray;
import org.json.JSONObject;

public class DBMesasAbiertas implements IBaseDatos, IBaseSocket {

     DBMesas db;

    public DBMesasAbiertas(DBMesas db){
        this.db = db;
    }


    @Override
    public JSONArray filter(String cWhere) {
        return db.filter(cWhere);
    }

    @Override
    public void rellenarTabla(JSONArray objs) {
        SQLiteDatabase sqlDb = db.getWritableDatabase();
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("abierta", 0);
            initialValues.put("num", "0");//Cerramos todas las mesas
            sqlDb.update("mesas", initialValues, null, null);

            for (int i=0; i < objs.length(); i ++) {
                JSONObject o = objs.getJSONObject(i);
                initialValues = new ContentValues();
                String id = o.getString("ID");
                initialValues.put("abierta", 1);
                initialValues.put("num", o.getString("num"));
                sqlDb.update("mesas", initialValues,"ID=?", new String[]{id});
            }
        } catch (SQLException sql){
            Log.i("ValleTPV", "Creando base de datos para mesas abiertas");
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void inicializar() {}

    @Override
    public void rm(JSONObject o) {
       db.rm(o);
    }

    @Override
    public void insert(JSONObject o) {
        try{
        }catch (Exception e){}
    }

    @Override
    public void update(JSONObject o) {
        try{
            SQLiteDatabase dbsql = db.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("abierta", o.getString("abierta"));
            values.put("num", o.getInt("num"));
            dbsql.update("mesas", values, "ID=?", new String[]{o.getString("ID")});

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
