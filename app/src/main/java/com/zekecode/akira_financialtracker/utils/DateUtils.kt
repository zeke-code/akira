package com.zekecode.akira_financialtracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getCurrentYearMonth(): String {
        val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return yearMonthFormat.format(Date())
    }

    fun formatDate(timestamp: Long, pattern: String = "yyyy-MM-dd"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}
