package com.valleapp.valletpvlib.tools

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class JSON {

    fun serializar(file: String, obj: JSONObject?, context: Context?) {
        try {
            val fos = OutputStreamWriter(context?.openFileOutput(file, Context.MODE_PRIVATE))
            fos.write(obj.toString())
            fos.close()
        } catch (e: Exception) {
            Log.e("JSON", e.toString())
        }
    }

    @Throws(JSONException::class)
    fun deserializar(file: String, context: Context?): JSONObject? {
        val strJSON = StringBuilder()

        try {
            val fos = BufferedReader(InputStreamReader(context?.openFileInput(file)))
            var tmp = fos.readLine()
            while (tmp != null) {
                strJSON.append(tmp)
                tmp = fos.readLine()
            }
            fos.close()
        } catch (e: Exception) {
            Log.e("JSON", e.toString())
        }

        return if (strJSON.isEmpty()) null else JSONObject(strJSON.toString())
    }
}
