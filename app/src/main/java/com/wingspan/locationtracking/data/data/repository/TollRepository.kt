package com.wingspan.locationtracking.data.data.repository

import com.wingspan.locationtracking.data.data.local.SessionDao
import com.wingspan.locationtracking.domain.model.TollRequest
import com.wingspan.locationtracking.domain.model.TollResponse
import com.wingspan.locationtracking.network.TollApiService
import javax.inject.Inject
import com.wingspan.locationtracking.utils.Result

class TollRepository @Inject constructor(
    private val api: TollApiService,    private val dao: SessionDao
) {

    suspend fun getTollCost(
        apiKey: String,
        request: TollRequest
    ): Result<TollResponse> {

        return try {
            val response = api.getTollCost(apiKey, request)

            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("API Error").toString())
            }

        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }
}
