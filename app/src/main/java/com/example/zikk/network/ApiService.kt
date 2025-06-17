package com.example.zikk.network

import ReportResponse
import com.example.zikk.enum.SortType
import com.example.zikk.model.Inquiry
import com.example.zikk.model.InquiryDetail
import com.example.zikk.model.Notice
import com.example.zikk.model.NoticeDetail
import com.example.zikk.model.request.ReportRequest
import com.example.zikk.model.request.LoginRequest
import com.example.zikk.model.response.LoginResponse
import com.example.zikk.model.Report
import com.example.zikk.model.ReportDetail
import com.example.zikk.model.Todo
import com.example.zikk.model.request.CreateNoticeRequest
import com.example.zikk.model.request.InquiryRequest
import com.example.zikk.model.request.ReportStatusRequest
import com.example.zikk.model.response.InquiryUpdateResponse
import com.example.zikk.model.response.NoticeDeleteResponse
import com.example.zikk.model.response.NoticeResponse
import com.example.zikk.model.response.NoticeUpdateResponse
import com.example.zikk.model.response.NoticeWriteResponse
import com.example.zikk.model.response.QuestionWriteResponse
import com.example.zikk.model.response.ReportStatusResponse
import com.example.zikk.model.response.ReportUpdateResponse
import com.example.zikk.model.response.StatisticsResponse
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("todos")
    suspend fun getTodos(): Response<List<Todo>>

    // 유저/관리자 로그인
    @POST("admin/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // 신고 작성
    @Multipart
    @POST("report")
    suspend fun sendLocationWithImages(
        @Part("request") request: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<Unit>

    // 메인페이지 처리 사례 조회
    @GET("report/examples")
    suspend fun getReportExamples(): Response<List<Report>>

    // 신고 내역 전체 조회
    @GET("report")
    suspend fun getReports(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<List<Report>>

    // 신고 내역 상세 조회
    @GET("report/{id}")
    suspend fun getReportDetail(
        @Header("Authorization") token: String,
        @Path("id") reportId: String,
    ): Response<ReportDetail>

    // 신고 내역 수정
    @Multipart
    @PATCH("report/{reportId}")
    suspend fun updateReportWithImages(
        @Header("Authorization") token: String,
        @Path("reportId") reportId: Long,
        @Part("request") request: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Response<ReportResponse>

    // 문의 내역 전체 조회
    @GET("inquiries")
    suspend fun getInquiries(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<List<Inquiry>>

    // 문의 내역 상세 조회
    @GET("inquiries/{id}")
    suspend fun getInquiryDetail(
        @Header("Authorization") token: String,
        @Path("id") inquiryId: Long
    ): Response<InquiryDetail>

    //문의 작성
    @POST("inquiries")
    suspend fun createInquiry(
        @Header("Authorization") token: String,
        @Body request: InquiryRequest
    ): Response<QuestionWriteResponse>

    // 관리자 통계 조회
    @GET("statistics")
    suspend fun getStatistics(): Response<StatisticsResponse>

    @PATCH("report/status/{id}")
    suspend fun updateReportStatus(
        @Path("id") reportId: String,
        @Body status: String
    )

    // 공지사항 조회
    @GET("notice")
    suspend fun getNotices(
        @Query("size") size: Int,
        @Query("page") page: Int,
    ): Response<NoticeResponse>

    // 공지사항 상세 조회
    @GET("notice/{id}")
    suspend fun getNoticeDetail(
        @Path("id") notiId: Int
    ): Response<NoticeDetail>

    @POST("notice")
    suspend fun createNotice(
        @Header("Authorization") token: String,
        @Body request: CreateNoticeRequest
    ): Response<NoticeWriteResponse>

    @Multipart
    @PATCH("report/{id}")
    suspend fun processReportStatus(
        @Header("Authorization") token: String,
        @Path("id") reportId: Int,
        @Part("request") request: RequestBody
    ): Response<ReportStatusResponse>

    @DELETE("notice/{id}")
    suspend fun deleteNotice(
        @Header("Authorization") token: String,
        @Path("id") noticeId: Int
    ): Response<NoticeDeleteResponse>

    @PATCH("notice/{id}")
    suspend fun updateNotice(
        @Header("Authorization") token: String,
        @Path("id") noticeId: Int,
        @Body request: RequestBody
    ) : Response<NoticeUpdateResponse>

    @PATCH("inquiries/{id}")
    suspend fun updateInquiry(
        @Header("Authorization") token: String,
        @Path("id") inquiryId: Int,
        @Body request: RequestBody
    ) : Response<InquiryUpdateResponse>
}

