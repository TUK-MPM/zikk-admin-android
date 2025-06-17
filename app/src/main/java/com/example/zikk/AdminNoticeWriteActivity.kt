package com.example.zikk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zikk.databinding.ActivityAdminNoticeWriteBinding
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.model.request.CreateNoticeRequest
import com.example.zikk.model.request.NoticeUpdateRequest
import com.example.zikk.network.RetrofitClient
import com.example.zikk.R
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AdminNoticeWriteActivity : BaseActivity() {
    private lateinit var binding: ActivityAdminNoticeWriteBinding
    private var isEditMode = false
    private var noticeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = setContentViewWithBinding(ActivityAdminNoticeWriteBinding::inflate)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.back.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnComplete.setOnClickListener {
            createNotice()
        }

        intent.getStringExtra("title")?.let {
            isEditMode = true
            binding.etTitle.setText(it)
        }
        intent.getStringExtra("content")?.let {
            binding.etContent.setText(it)
        }
        noticeId = intent.getIntExtra("noticeId", -1).takeIf { it != -1 }

        binding.btnComplete.setOnClickListener {
            if (isEditMode) {
                updateNotice()
            } else {
                createNotice()
            }
        }
    }

    private fun createNotice() {
        lifecycleScope.launch {
            try {
                val token = "Bearer ${getLoginToken()!!}"
                Log.d("token", token)
                val request = CreateNoticeRequest(
                    binding.etTitle.text.toString(),
                    binding.etContent.text.toString()
                )
                val response = RetrofitClient.apiService.createNotice(
                    token,
                    request
                )

                Log.d("response", response.toString())

                if(response.isSuccessful) {
                    val noticeResponse = response.body()
                    noticeResponse?.let { responseBody ->
                        Toast.makeText(this@AdminNoticeWriteActivity, "완료", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun updateNotice() {
        lifecycleScope.launch {
            try {

                val title = binding.etTitle.text.toString()
                val content = binding.etContent.text.toString()

                val request = Gson().toJson(NoticeUpdateRequest(title, content))
                val requestBody =
                    request.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val response = RetrofitClient.apiService.updateNotice(
                    token = "Bearer " + getLoginToken()!!,
                    noticeId = noticeId!!,
                    request = requestBody
                )

                if(response.isSuccessful) {
                    Toast.makeText(applicationContext, "수정 성공", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                throw e
            }
        }
    }
}