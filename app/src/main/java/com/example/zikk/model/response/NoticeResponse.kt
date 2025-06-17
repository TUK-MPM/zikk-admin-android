package com.example.zikk.model.response

import com.example.zikk.model.Notice

data class NoticeResponse(
    val content: List<Notice>,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val first: Boolean,
    val last: Boolean
)
