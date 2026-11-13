package com.duoc.x.remote

import retrofit2.http.GET

interface ApiService {


    //Agregar funcion aca abajo para nuestro proyecto (funcion "autenticarUsuario" / funcion "ObtenerArticulos" y "ObtenerNormas")

    @GET(" TU PATH ")
    suspend fun Login(){

    }
    @GET(" ")
    suspend fun ObtenerNormas(){

    }

    @GET(" ")
    suspend fun ObtenerArticulos(){

    }

}