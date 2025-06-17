package com.example.zikk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zikk.databinding.ActivityAdminQuestionContentAcitivityBinding
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.model.InquiryDetail
import com.example.zikk.model.request.NoticeUpdateRequest
import com.example.zikk.network.RetrofitClient
import com.example.zikk.R
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AdminQuestionContentAcitivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminQuestionContentAcitivityBinding
    private var inquiryId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_admin_question_content_acitivity)
        binding = ActivityAdminQuestionContentAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inquiryId = intent.getLongExtra("inquiryId", -1L)
        if (inquiryId == -1L) {
            finish()
            return
        }

        binding.back.setOnClickListener {
            val intent = Intent(this, QuestionListActivity::class.java)
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, QuestionListActivity::class.java)
            startActivity(intent)
        }
        binding.btnEdit.setOnClickListener {
            createAnswer(inquiryId)
        }
        binding.btnComplete.setOnClickListener {
            createAnswer(inquiryId)
        } // 내용 옮기는 기능 넣어야 함


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchInquiryDetail(inquiryId)

    }

    private fun fetchInquiryDetail(inquiryId: Long) {
        lifecycleScope.launch {
            val token = getLoginToken()
            if (token == null) {
                Toast.makeText(applicationContext, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val bearerToken = "Bearer $token"  // ✅ 반드시 Bearer 붙이기

            try {
                val response = RetrofitClient.apiService.getInquiryDetail(bearerToken, inquiryId)
                if (response.isSuccessful) {
                    Log.d("response", response.body().toString())
                    response.body()?.let { bindInquiryDetail(it) }
                } else {
                    Log.e(
                        "InquiryDetail",
                        "실패 응답 코드: ${response.code()}, error: ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(
                        applicationContext,
                        "조회 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } catch (e: Exception) {
                Log.e("InquiryDetail", "예외 발생", e)
                Toast.makeText(
                    applicationContext,
                    "오류 발생: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun bindInquiryDetail(detail: InquiryDetail) {
        binding.tvInquireTitle.text = detail.title
        binding.tvInquireCreatedAt.text = formatDate(detail.createdAt)
        binding.tvInquireContent.text = detail.content
        binding.etInquireAnswer.setText(detail.reply)
    }

    private fun formatDate(raw: String): String {
        return raw.replace("T", " ").substring(0, 16)
    }

    private fun createAnswer(inquiryId: Long) {
        lifecycleScope.launch {
            val token = getLoginToken()
            if (token == null) {
                Toast.makeText(applicationContext, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val bearerToken = "Bearer $token"  // ✅ 반드시 Bearer 붙이기

            try {
                val reply = binding.etInquireAnswer.text.toString()
                val status = "COMPLETED"

                val request = Gson().toJson(NoticeUpdateRequest(reply, status))
                val requestBody =
                    request.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val apiResponse = RetrofitClient.apiService.updateInquiry(
                    bearerToken,
                    inquiryId.toInt(),
                    requestBody
                )

                if (apiResponse.isSuccessful) {
                    Toast.makeText(applicationContext, "수정 성공", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchInquiryDetail(inquiryId)
    }

    override fun onRestart() {
        super.onRestart()
        fetchInquiryDetail(inquiryId)
    }
}