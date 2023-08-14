package com.valleapp.valletpvlib.tools

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object JSON {

    fun serializar(file: String, obj: JSONObject, context: Context) {
        try {
            val fos = OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE))
            fos.use {
                it.write(obj.toString())
            }
        } catch (e: Exception) {
            Log.e("JSON", e.message ?: "Error serializing JSON")
        }
    }

    @Throws(JSONException::class)
    fun deserializar(file: String, context: Context): JSONObject? {
        var strJSON = ""

        try {
            val fos = BufferedReader(InputStreamReader(context.openFileInput(file)))
            fos.use {
                var tmp = it.readLine()
                while (tmp != null) {
                    strJSON += tmp
                    tmp = it.readLine()
                }
            }
        } catch (e: Exception) {
            Log.e("JSON", e.message ?: "Error deserializing JSON")
        }

        return if (strJSON.isEmpty()) null else JSONObject(strJSON)
    }
}
