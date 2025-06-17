package com.example.zikk.model

import com.google.gson.annotations.SerializedName

data class Inquiry(
    @SerializedName("inquId")       // ✅ 서버의 필드명과 매핑
    val inquiryId: Long,            // ✅ Long 타입으로 선언
    val title: String,
    val status: String,      // "PENDING", "COMPLETED"
    val createdAt: String    // ISO 8601 문자열
)
