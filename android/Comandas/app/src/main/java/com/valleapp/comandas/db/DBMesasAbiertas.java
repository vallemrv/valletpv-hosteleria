package com.valleapp.comandas.db;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.valleapp.comandas.interfaces.IBaseDatos;

import org.json.JSONArray;
import org.json.JSONObject;

public class DbMesasAbiertas implements IBaseDatos {

    DbMesas db;

    public DbMesasAbiertas(DbMesas db){
        this.db = db;
    }


    @Override
    public void resetFlag(int id) {
        db.resetFlag(id);
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
        db.close();
    }
}
