package com.duoc.x.remote

import com.duoc.x.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("loginrequest")
    suspend fun obtenerUsuarios(): Response<List<LoginRequest>>

    @POST("loginrequest")
    suspend fun registrarUsuario(@Body request: LoginRequest): Response<LoginRequest>

    @GET("norma")
    suspend fun obtenerNormas(): Response<List<NormaItem>>

    @GET("articulo")
    suspend fun obtenerArticulos(): Response<List<ArticuloItem>>

    //Funciones para el developer

    @DELETE("loginrequest/{loginrequest_id}")
    suspend fun eliminarUsuario(@Path("loginrequest_id") id: Int): Response<Unit>

    @POST("articulo")
    suspend fun crearArticulo(@Body articulo: ArticuloItem): Response<ArticuloItem>

    @PUT("articulo/{articulo_id}")
    suspend fun actualizarArticulo(@Path("articulo_id") id: Int, @Body articulo: ArticuloItem): Response<ArticuloItem>

    @DELETE("articulo/{articulo_id}")
    suspend fun eliminarArticulo(@Path("articulo_id") id: Int): Response<Unit>

}