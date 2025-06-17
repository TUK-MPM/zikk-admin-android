package com.example.zikk.model.response

import com.example.zikk.model.ReportType

data class StatisticsResponse(
    val totalReportCount: Int,
    val pendingReportCount: Int,
    val totalInquiryCount: Int,
    val pendingInquiryCount: Int,
    val reportTypes: ReportType
)
