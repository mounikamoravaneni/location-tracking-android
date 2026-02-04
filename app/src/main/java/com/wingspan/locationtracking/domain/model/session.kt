package com.wingspan.locationtracking.domain.model

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)




data class TollRequest(
    val mapProvider: String,
    val polyline: String? = null,
    val path: String? = null,
    val locTimes: List<List<Long>> = emptyList(),
    val vehicle: Vehicle,
    val fuelOptions: FuelOptions? = null,
    val includeParkingFee: Boolean = true
)

data class Vehicle(val type: String)
data class FuelOptions(
    val fuelCost: FuelCost,
    val fuelEfficiency: FuelEfficiency
)
data class FuelCost(val value: Double, val currency: String, val units: String)
data class FuelEfficiency(val city: Double, val hwy: Double, val units: String)

// Response model
data class TollResponse(
    val status: String,
    val summary: Summary,
    val route: Route
)

data class Summary(val currency: String, val vehicleType: String)
data class Route(
    val hasTolls: Boolean,
    val distance: Distance,
    val costs: Costs,
    val tolls: List<Any>?,
    val parkings: List<Parking>?
)
data class Distance(val text: String, val metric: String, val value: Int)
data class Costs(val fuel: Double?)
data class Parking(
    val name: String,
    val address: String,
    val fee: Fee
)
data class Fee(val cash: Double?, val tag: Double?)