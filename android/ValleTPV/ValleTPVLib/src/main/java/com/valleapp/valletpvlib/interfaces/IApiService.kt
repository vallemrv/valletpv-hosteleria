package com.valleapp.valletpvlib.interfaces

import com.valleapp.valletpvlib.tools.Params
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IApiService {
    @POST("/camareros/new")
    suspend fun add_camarero(@Body params: Params): Response<JSONObject>
    @POST("/camareros/set_password")
    suspend fun set_password(@Body params: Params): Response<JSONObject>
}