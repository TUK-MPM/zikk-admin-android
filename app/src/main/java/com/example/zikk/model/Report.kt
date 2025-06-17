package com.example.zikk.model

import com.example.zikk.enum.IllegalParkingLocation
import com.example.zikk.enum.Status
import com.google.gson.annotations.SerializedName

data class Report(
    val reportId: String,
    @SerializedName("reason") val where: String, // ← 이걸로 서버의 reason을 where에 매핑
    val status: String,
    val createdAt: String,
    val address: String
)