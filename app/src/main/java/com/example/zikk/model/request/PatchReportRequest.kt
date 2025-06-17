package com.example.zikk.model.request

import com.google.gson.annotations.SerializedName

data class PatchReportRequest(
    @SerializedName("number") val phone: String,
    val address: String,
    @SerializedName("reportType") val type: String,
    @SerializedName("mediaUrls") val imageUrls: List<String>
)


