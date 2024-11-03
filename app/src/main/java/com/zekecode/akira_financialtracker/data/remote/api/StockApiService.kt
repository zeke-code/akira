package com.zekecode.akira_financialtracker.data.remote.api

import androidx.room.Query
import com.zekecode.akira_financialtracker.data.remote.models.StockInsightsResponse
import retrofit2.http.GET


interface StockApiService {
    @GET("stock/v3/get-insights")
    suspend fun getStockInsights(
    ): StockInsightsResponse
}