import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @POST
    suspend fun post(@Url url: String, @Body params: Any): Response<Any>
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
    data class Error<T>(val errorMessage: String) : ApiResponse<T>()
}

suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): ApiResponse<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            ApiResponse.Success(response.body()!!)
        } else {
            when (response.code()) {
                403 -> ApiResponse.Error("No autorizado")
                404 -> ApiResponse.Error("No encontrado")
                500 -> ApiResponse.Error("Fallo en el servidor")
                else -> ApiResponse.Error("Error desconocido: ${response.code()}")
            }
        }
    } catch (e: Exception) {
        ApiResponse.Error("Error de conexión: ${e.message}")
    }
}
