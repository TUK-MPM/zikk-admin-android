package com.example.zikk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zikk.databinding.ActivityNoticeContentBinding
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.extensions.getUserRole
import com.example.zikk.model.NoticeDetail
import com.example.zikk.network.RetrofitClient
import com.example.zikk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoticeContentActivity : BaseActivity() {
    private lateinit var binding: ActivityNoticeContentBinding
    private var notiId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = setContentViewWithBinding(ActivityNoticeContentBinding::inflate)

        notiId = intent.getIntExtra("notiId", -1)
        if (notiId == -1) {
            showToast("공지 ID가 전달되지 않았습니다.")
            finish()
            return
        }

        fetchNoticeDetail(notiId)

        binding.back.setOnClickListener {
            startActivity(Intent(this, NoticeActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, NoticeActivity::class.java))
        }

        binding.btnDelete.setOnClickListener {
            deleteNotice(notiId)
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, AdminNoticeWriteActivity::class.java)
            intent.putExtra("noticeId", notiId)
            intent.putExtra("title", binding.titleTextView.text)
            intent.putExtra("content", binding.contentTextView.text)
            startActivity(intent)
        }

        binding.btnDelete.visibility = if (isAdmin()) View.VISIBLE else View.GONE
        binding.btnEdit.visibility = if (isAdmin()) View.VISIBLE else View.GONE

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchNoticeDetail(notiId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getNoticeDetail(notiId)
                if (response.isSuccessful) {
                    val noticeDetail = response.body()
                    withContext(Dispatchers.Main) {
                        if (noticeDetail != null) {
                            bindNoticeDetail(noticeDetail)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("공지 조회 실패: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("NoticeDetail", "예외 발생", e)
                withContext(Dispatchers.Main) {
                    showToast("오류: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun bindNoticeDetail(detail: NoticeDetail) {
        binding.textView.text = "공지사항 내용"
        binding.titleTextView.text = detail.title
        binding.createdAtTextView.text = formatDate(detail.createdAt)
        binding.contentTextView.text = detail.content
    }

    private fun formatDate(raw: String?): String {
        if (raw.isNullOrEmpty()) return ""
        return raw.replace("T", " ").substring(0, 16)
    }

    private fun isAdmin(): Boolean {
        return getUserRole() == "ROLE_ADMIN"
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun deleteNotice(id: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteNotice(
                    token = "Bearer " + getLoginToken()!!,
                    noticeId = id
                )
                Log.d("response", response.toString())
                if (response.isSuccessful) {
                    showToast("삭제되었습니다!")
                    startActivity(Intent(applicationContext, NoticeActivity::class.java))
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        fetchNoticeDetail(notiId)
    }
}
