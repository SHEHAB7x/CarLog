package com.example.carlog.network

import com.example.carlog.data.ModelUser
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {
    @POST("Login/Driver")
    suspend fun loginUser(@Body body: LoginRequestBody): ModelUser
}

data class LoginRequestBody(
    val email: String,
    val password: String
)
