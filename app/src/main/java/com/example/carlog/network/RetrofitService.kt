package com.example.carlog.network

import com.example.carlog.data.ModelAllTrips
import com.example.carlog.data.ModelUser
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitService {
    @POST("Login/Driver")
    suspend fun loginUser(@Body body: LoginRequestBody): ModelUser

    @POST("Trip")
    suspend fun postTrip(@Body body: TripRequestBody): Int

    @GET("Trip")
    suspend fun getAllTrips() : ModelAllTrips
}

data class LoginRequestBody(
    val email: String,
    val password: String
)

data class TripRequestBody(
    val date: String,
    val rapidAccelerationTimes: Int,
    val rapidDeclarationTimes: Int,
    val tripTime: String,
    val excessiveIdling: Int,
    val overspeedTimes: Int,
    val tripRate: Int,
    val maxSpeed: Int,
    val maxAcceleration: Int,
    val maxBraking: Int,
    val maxExcessiveIdling: Int = 0
)
