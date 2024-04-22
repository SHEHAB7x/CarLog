package com.example.carlog.data

data class ModelUser(
    val address: String,
    val expiration: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val statusCode: StatusCode,
    val token: String,
    val userEmail: String,
    val userID: Int
)
data class StatusCode(
    val statusCode: Int
)