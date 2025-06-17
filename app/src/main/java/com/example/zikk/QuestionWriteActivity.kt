package com.example.zikk

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zikk.databinding.ActivityQuestionWriteBinding
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.model.request.InquiryRequest
import com.example.zikk.network.RetrofitClient
import com.example.zikk.R
import kotlinx.coroutines.launch

class QuestionWriteActivity : BaseActivity() {
    private lateinit var binding: ActivityQuestionWriteBinding
    private lateinit var token: String                       // 토큰 선언 (초기화는 나중에)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = setContentViewWithBinding(ActivityQuestionWriteBinding::inflate)

        binding.back.setOnClickListener { finish() }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnComplete.setOnClickListener { createInquiry() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun createInquiry() {
        lifecycleScope.launch {
            try {
                val rawToken = getLoginToken()
                if (rawToken == null) {
                    Toast.makeText(this@QuestionWriteActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val token = "Bearer $rawToken"
                val request = InquiryRequest(
                    binding.titleEditText.text.toString(),
                    binding.contentEditText.text.toString()
                )

                val response = RetrofitClient.apiService.createInquiry(token, request)

                if (response.isSuccessful) {
                    Toast.makeText(this@QuestionWriteActivity, "문의 등록 완료", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@QuestionWriteActivity, "문의 등록 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@QuestionWriteActivity, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                Log.e("InquiryError", "문의 등록 중 오류", e)
            }
        }
    }

}