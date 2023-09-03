package com.valleapp.valletpvlib.tools

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


object ApiEndPoints {
    const val PEDIDOS_COBRAR = "cuenta/cobrar"
    const val BORRAR_MESA = "cuenta/rm"
    const val PRE_IMPRIMIR = "impresoras/preimprimir"
    const val ABRIR_CAJON = "impresoras/abircajon"
    const val PEDIDOS_ADD = "pedidos/add"
    const val DISPOSITIVO_NUEVO = "dispositivos/new"
    const val CAMAREROS_ADD = "camareros/add"
    const val CAMAREROS_SET_PASSWORD = "camareros/set_password"
    const val CAMAREROS_SET_AUTORIZADO = "camareros/set_autorizado"
    const val SYNC_DEVICES = "sync/sync_devices"
}

object ApiErrorMessages {
    const val UNAUTHORIZED = "No autorizado"
    const val NOT_FOUND = "No encontrado"
    const val SERVER_ERROR = "Fallo en el servidor"
    const val NO_CONNECTION = "No hay conexión a internet"
    const val TIMEOUT = "Tiempo de espera excedido"
    const val UNKNOWN_ERROR = "Error desconocido"
}
interface ApiService {
    @POST
    suspend fun post(@Url url: String, @Body params: Map<String, String>? = null): Response<Map<String, String>>

}


object ApiRequest {
    private lateinit var retrofit: Retrofit

    fun init(baseURL: String) {
        retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: ApiService
        get() = retrofit.create(ApiService::class.java)
}


sealed class ApiResponse<T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error<T>(val errorMessage: Any) : ApiResponse<T>()
}


suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): ApiResponse<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val responseBody = response.body() as Map<*, *>?
            if (responseBody != null){
                responseBody["success"]?.let {
                    if (it == "false") {
                        return ApiResponse.Error(responseBody["errors"] ?: ApiErrorMessages.UNKNOWN_ERROR)
                    }
                }
            }
            ApiResponse.Success(response.body()!!)
        } else {
            when (response.code()) {
                401 -> ApiResponse.Error(ApiErrorMessages.UNAUTHORIZED)
                403 -> ApiResponse.Error(ApiErrorMessages.UNAUTHORIZED)
                404 -> ApiResponse.Error(ApiErrorMessages.NOT_FOUND)
                500 -> ApiResponse.Error(ApiErrorMessages.SERVER_ERROR)
                else -> ApiResponse.Error("${ApiErrorMessages.UNKNOWN_ERROR}: ${response.code()}")
            }
        }
    } catch (e: Exception) {
        when (e) {
            is UnknownHostException -> ApiResponse.Error(ApiErrorMessages.NO_CONNECTION)
            is ConnectException -> ApiResponse.Error(ApiErrorMessages.NO_CONNECTION)
            is SocketTimeoutException -> ApiResponse.Error(ApiErrorMessages.TIMEOUT)
            else -> ApiResponse.Error(ApiErrorMessages.UNKNOWN_ERROR)
        }
    }
}
