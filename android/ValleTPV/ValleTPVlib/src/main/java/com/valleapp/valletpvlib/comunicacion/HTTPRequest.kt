package com.valleapp.valletpvlib.comunicacion

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object HTTPRequest {

private val client = OkHttpClient()
private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

suspend fun request(strUrl: String, params: JSONObject?): Result<JSONObject> = withContext(Dispatchers.IO) {
    try {
        val url = if (!strUrl.startsWith("http://") && !strUrl.startsWith("https://")) {
            "http://$strUrl"
        } else {
            strUrl
        }

        val requestBody = params?.toString()?.toRequestBody(jsonMediaType)
                ?: "".toRequestBody(jsonMediaType) // Manejar el caso de params nulos. Enviar body vacío.

        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        return@withContext Result.success(jsonResponse)
                    } catch (e: Exception) {
                        Log.e("HTTPRequest", "Error parsing JSON response: ${e.message}")
                        return@withContext Result.failure(ParseException("Error parsing JSON")) //Customizar la exception
                    }
                }else{
                    return@withContext Result.failure(EmptyResponseException("Empty response"))
                }

            } else {
                val errorMessage = response.body?.string() ?: "HTTP error ${response.code}"
                Log.e("HTTPRequest", errorMessage)
                return@withContext Result.failure(HttpException(errorMessage,response.code))
            }
        }
    } catch (e: IOException) {
        Log.e("HTTPRequest", "Network error: ${e.message}")
        return@withContext Result.failure(NetworkException("Network error"))
    } catch (e: Exception) {
        Log.e("HTTPRequest", "General error: ${e.message}")
        return@withContext Result.failure(e) //Devolver la excepcion original.
    }
}

class ParseException(message:String): Exception(message)
class HttpException(message: String,val code:Int) : Exception(message)
class NetworkException(message:String) : Exception(message)
class EmptyResponseException(message: String) : Exception(message)
}