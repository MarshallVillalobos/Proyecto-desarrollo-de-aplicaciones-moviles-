package com.duoc.x.remote

import com.duoc.x.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("loginrequest")
    suspend fun obtenerUsuarios(): Response<List<LoginRequest>>

    @POST("loginrequest")
    suspend fun registrarUsuario(@Body request: LoginRequest): Response<LoginRequest>
}