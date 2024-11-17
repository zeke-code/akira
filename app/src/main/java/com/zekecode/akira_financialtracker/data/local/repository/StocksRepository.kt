package com.zekecode.akira_financialtracker.data.local.repository

import com.zekecode.akira_financialtracker.data.remote.api.AlphaVentureService
import com.zekecode.akira_financialtracker.data.remote.models.TimeSeriesDailyModel
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StocksRepository @Inject constructor(
    private val alphaVentureService: AlphaVentureService
) {
    // Fetch daily time series data for a specific stock
    suspend fun getDailyTimeSeries(symbol: String, apiKey: String): Result<TimeSeriesDailyModel> {
        return try {
            val response = alphaVentureService.getDailyTimeSeries(symbol = symbol, apiKey = apiKey)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }
}
