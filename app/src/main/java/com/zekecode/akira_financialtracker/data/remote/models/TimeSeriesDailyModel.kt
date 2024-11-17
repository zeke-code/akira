package com.zekecode.akira_financialtracker.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimeSeriesDailyModel(
    @Json(name = "Meta Data")
    val metaData: MetaData,
    @Json(name = "Time Series (Daily)")
    val timeSeries: Map<String, DailyData>
)

@JsonClass(generateAdapter = true)
data class MetaData(
    @Json(name = "1. Information")
    val information: String,
    @Json(name = "2. Symbol")
    val symbol: String,
    @Json(name = "3. Last Refreshed")
    val lastRefreshed: String,
    @Json(name = "4. Output Size")
    val outputSize: String,
    @Json(name = "5. Time Zone")
    val timeZone: String
)

@JsonClass(generateAdapter = true)
data class DailyData(
    @Json(name = "1. open")
    val open: String,
    @Json(name = "2. high")
    val high: String,
    @Json(name = "3. low")
    val low: String,
    @Json(name = "4. close")
    val close: String,
    @Json(name = "5. volume")
    val volume: String
)
