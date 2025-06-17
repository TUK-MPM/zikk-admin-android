package com.example.zikk.model.response

data class ReportStatusResponse(
    val reportId: Int,
    val status: String,
    val message: String
)
