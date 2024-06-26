package com.example.carlog.data

data class ModelAllTrips(
    val getList: List<ModelAllTripsItem>,
    val statuscode: StatusCode
)

data class ModelAllTripsItem(
    val date: String,
    val excessiveIdling: Int,
    val harshAcceleration: Int,
    val harshDeclaration: Int,
    val maxAcceleration: Int,
    val maxBraking: Int,
    val maxExcessiveIdling: Int,
    val maxSpeed: Int,
    val overSpeed: Int,
    val time: String,
    val tripRate: Int
)
