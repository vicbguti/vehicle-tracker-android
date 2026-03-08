package org.onebusaway.tracker.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LocationReport(
    val vehicle_id: String,
    val trip_id: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val speed: Float,
    val accuracy: Float,
    val timestamp: Long
)

data class ApiResponse(
    val status: String,
    val error: String? = null
)

interface ApiService {
    @POST("/api/v1/locations")
    suspend fun postLocation(@Body report: LocationReport): Response<ApiResponse>
}
