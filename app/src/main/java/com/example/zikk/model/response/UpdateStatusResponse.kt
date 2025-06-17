package com.example.zikk.model.response

data class UpdateStatusResponse(
    val reportId: String,
    val status: String,
    val message: String
)
