package com.wingspan.locationtracking.network

import com.wingspan.locationtracking.domain.model.TollRequest
import com.wingspan.locationtracking.domain.model.TollResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface TollApiService {
    @Headers("Content-Type: application/json")
    @POST("polyline-map-matching")
    suspend fun getTollCost(
        @Header("x-api-key") apiKey: String,
        @Body request: TollRequest
    ): Response<TollResponse>
}


