package com.valleapp.valletpvlib.tools;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class JSON {

    public void serializar(String file, JSONObject obj, Context context){
        try{
            OutputStreamWriter fos = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
            fos.write(obj.toString());
            fos.close();
        }catch(Exception e){
            Log.e("JSON", e.toString());
        }
    }

    public JSONObject deserializar(String file, Context context) throws JSONException {
        StringBuilder strJSON = new StringBuilder();

        try{
            BufferedReader fos = new BufferedReader(new InputStreamReader(context.openFileInput(file)));
            String tmp = fos.readLine();
            while (tmp!=null) {
                strJSON.append(tmp);
                tmp = fos.readLine();
            }
            fos.close();
        }catch(Exception e){
            Log.e("JSON", e.toString());
        }

        if (strJSON.length() == 0) return null;
        else {
            return new JSONObject(strJSON.toString());
        }

    }
}
