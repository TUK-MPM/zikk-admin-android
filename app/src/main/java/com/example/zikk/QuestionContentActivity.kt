package com.example.zikk

import com.example.zikk.model.InquiryDetail
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zikk.databinding.ActivityQuestionContextBinding
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.network.RetrofitClient
import com.example.zikk.R
import kotlinx.coroutines.launch

class QuestionContentActivity : BaseActivity() {
    private lateinit var binding: ActivityQuestionContextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = setContentViewWithBinding(ActivityQuestionContextBinding::inflate)

        val inquiryId = intent.getLongExtra("inquiryId", -1L)
        if (inquiryId == -1L) {
            showToast("문의 ID가 전달되지 않았습니다.")
            finish()
            return
        }

        binding.back.setOnClickListener {
            startActivity(Intent(this, QuestionListActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, QuestionListActivity::class.java))
        }

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
                showToast("로그인이 필요합니다.")
                return@launch
            }

            val bearerToken = "Bearer $token"  // ✅ 반드시 Bearer 붙이기

            try {
                val response = RetrofitClient.apiService.getInquiryDetail(bearerToken, inquiryId)
                if (response.isSuccessful) {
                    response.body()?.let { bindInquiryDetail(it) }
                } else {
                    Log.e("InquiryDetail", "실패 응답 코드: ${response.code()}, error: ${response.errorBody()?.string()}")
                    showToast("조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("InquiryDetail", "예외 발생", e)
                showToast("오류 발생: ${e.localizedMessage}")
            }
        }
    }


    private fun bindInquiryDetail(detail: InquiryDetail) {
        binding.titleTextView.text = detail.title
        binding.createdAtTextView.text = formatDate(detail.createdAt)
        binding.contentTextView.text = detail.content
        binding.replyEditText.setText(detail.reply ?: "")
        binding.replyEditText.isEnabled = false
        binding.repliedAtTextView.text = detail.repliedAt?.let { formatDate(it) } ?: ""
    }

    private fun formatDate(raw: String): String {
        return raw.replace("T", " ").substring(0, 16)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
