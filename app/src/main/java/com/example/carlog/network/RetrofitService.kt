package com.example.carlog.network

import com.example.carlog.data.ModelUser
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {
    @FormUrlEncoded
    @POST("login")
    suspend fun loginUser(
        @Field("email") email : String,
        @Field("password") password: String,
        @Field("device_token") deviceToken: String
    ) : ModelUser

}