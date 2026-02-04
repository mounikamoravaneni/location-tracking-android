package com.wingspan.locationtracking.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wingspan.locationtracking.data.data.repository.TollRepository
import com.wingspan.locationtracking.domain.model.FuelCost
import com.wingspan.locationtracking.domain.model.FuelEfficiency
import com.wingspan.locationtracking.domain.model.FuelOptions
import com.wingspan.locationtracking.domain.model.GpsPoint
import com.wingspan.locationtracking.domain.model.TollRequest
import com.wingspan.locationtracking.domain.model.TollResponse
import com.wingspan.locationtracking.domain.model.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.wingspan.locationtracking.utils.Result
import kotlinx.coroutines.launch


@HiltViewModel
class TollViewModel @Inject constructor(
    private val repository: TollRepository
) : ViewModel() {

    private val _tollData = MutableStateFlow<TollResponse?>(null)
    val tollData: StateFlow<TollResponse?> = _tollData

    fun fetchToll(points:List<GpsPoint>, apk: String, polyencode: String) {

        Log.d("TollVM", "fetchToll called")
        Log.d("TollVM", "Polyline length = ${points}")

        viewModelScope.launch {

            Log.d("TollVM", "Coroutine started")
            val fuelOptions = FuelOptions(
                fuelCost = FuelCost(36.08, "CZK", "Kč/liter"),
                fuelEfficiency = FuelEfficiency(10.1, 7.9, "L/100km")
            )

            // locTimes mapping
            val locTimes = points.mapIndexed { index, p ->
                listOf(index.toLong(), p.timestamp)
            }

            val request = TollRequest(
                mapProvider = "osm",
                polyline = polyencode,
                locTimes = locTimes,
                vehicle = Vehicle(type = "4AxlesTruck"),
                fuelOptions = fuelOptions,
                includeParkingFee = true
            )

            Log.d("TollVM", "Request created: $request")

            val result = repository.getTollCost(
                apiKey = apk,
                request = request
            )

            Log.d("TollVM", "Repository response received")

            when (result) {

                is Result.Success -> {
                    Log.d("TollVM", "Success: Toll data received")
                    Log.d("TollVM", "Toll Summary = ${result.data.summary}")

                    _tollData.value = result.data
                }

                is Result.Error -> {
                    Log.e("TollVM", "Error: ${result.message}")
                }

                Result.Loading -> {
                    Log.d("TollVM", "Loading state")
                }
            }
        }
    }



}
