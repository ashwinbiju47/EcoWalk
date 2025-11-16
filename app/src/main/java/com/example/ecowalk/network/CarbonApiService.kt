package com.example.ecowalk.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class CarbonEstimateRequest(
    val type: String = "vehicle",
    val distance_unit: String = "km",
    val distance_value: Double
)

data class CarbonEstimateResponse(
    val data: CarbonEstimateData
)

data class CarbonEstimateData(
    val id: String,
    val type: String,
    val attributes: CarbonAttributes
)

data class CarbonAttributes(
    val carbon_kg: Double,
    val carbon_mt: Double
)

interface CarbonApiService {

    @Headers(
        "Content-Type: application/json"
    )
    @POST("estimates")
    suspend fun createEstimate(
        @Body request: CarbonEstimateRequest
    ): CarbonEstimateResponse
}
