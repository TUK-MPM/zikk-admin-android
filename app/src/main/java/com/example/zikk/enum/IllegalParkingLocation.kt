package com.example.zikk.enum

enum class IllegalParkingLocation(
    val description: String
) {
    DOT_BLOCK("점자블록"),
    TRAFFIC_ISLAND("교통섬 내부"),
    PROTECTED_ZONE("보호구역"),
    WALKWAY_OTHER("상기 사항 외 보도"),
    OTHER("기타");
}