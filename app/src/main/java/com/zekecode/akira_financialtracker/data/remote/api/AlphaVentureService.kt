package com.zekecode.akira_financialtracker.data.remote.api

import com.zekecode.akira_financialtracker.data.remote.models.StockQuoteModel
import com.zekecode.akira_financialtracker.data.remote.models.TimeSeriesDailyModel
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVentureService {
    @GET("/query")
    suspend fun getDailyTimeSeries(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): TimeSeriesDailyModel

    @GET("/query")
    suspend fun getStockQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): StockQuoteModel
}
