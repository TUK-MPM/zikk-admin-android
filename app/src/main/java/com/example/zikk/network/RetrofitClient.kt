package com.example.zikk.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://3.39.234.193:8080/api/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())  // JSON -> 객체 변환을 위한 Gson 사용
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}