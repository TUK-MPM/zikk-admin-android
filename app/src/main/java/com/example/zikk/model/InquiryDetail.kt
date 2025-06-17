package com.example.zikk.model
import com.google.gson.annotations.SerializedName

data class InquiryDetail(
    @SerializedName("inquId")
    val inquiryId: Long,

    val title: String,
    val content: String,

    @SerializedName("response")
    val reply: String?, // 서버에선 'response'로 주고 있음

    val status: String,

    val createdAt: String,

    @SerializedName("respondedAt")
    val repliedAt: String?, // 서버는 respondedAt, 우리는 repliedAt로 사용

    val message: String?
)
