package com.zekecode.akira_financialtracker.data.remote.models

import com.squareup.moshi.Json

data class StockInsightsResponse(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "recommendation") val recommendation: String,
    @Json(name = "targetPrice") val targetPrice: Double,
    @Json(name = "insights") val insights: List<StockInsight>
)

data class StockInsight(
    @Json(name = "type") val type: String,
    @Json(name = "detail") val detail: String
)
