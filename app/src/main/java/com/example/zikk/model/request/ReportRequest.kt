package com.example.zikk.model.request

data class ReportRequest(
    val phone: String,             // 휴대폰 번호 (필수)
    val address: String,           // 위치 텍스트 (필수)
    val reportType: String,              // 불법 주차 위치 ENUM 값 (필수)
    val imageUrls: List<String> = emptyList() // 이미지 URL 리스트 (선택)
)