package com.example.zikk.model

import com.google.gson.annotations.SerializedName

data class ReportDetail(
    val number: String,
    val reportId: String,
    val reportType: String,
    val address: String,
    val mediaUrls: List<String>,
    val status: String,
    val createdAt: String
)



