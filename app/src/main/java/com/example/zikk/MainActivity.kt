package com.example.zikk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zikk.adapter.NoticeAdapter
import com.example.zikk.model.Notice
import com.example.zikk.network.RetrofitClient
import com.example.zikk.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noticeAdapter: NoticeAdapter
    private val noticeList = mutableListOf<Notice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = setContentViewWithBinding(ActivityMainBinding::inflate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        noticeAdapter = NoticeAdapter(noticeList) { notice ->
            Toast.makeText(this@MainActivity, "${notice.title} 클릭됨", Toast.LENGTH_SHORT).show()
            Log.d("NoticeClick", "Clicked: ${notice.notiId}")
        }

        binding.btnNoticeWrite.setOnClickListener {
            val intent = Intent(applicationContext, AdminNoticeWriteActivity::class.java)
            startActivity(intent)
        }

        binding.btnInquiry.setOnClickListener {
            val intent = Intent(applicationContext, QuestionListActivity::class.java)
            startActivity(intent)
        }

        binding.btnReport.setOnClickListener {
            val intent = Intent(applicationContext, ReportListActivity::class.java)
            startActivity(intent)
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getNotices() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getNotices(
                    size = 3,
                    page = 1
                )

                Log.d("response", response.toString())

                if (response.isSuccessful) {
                    val noticeListResponse = response.body()
                    noticeListResponse?.let {
                        // NoticeResponse에서 공지사항 리스트 꺼내기
                        val newList = it.content
                        noticeList.clear()
                        noticeList.addAll(newList)
                        noticeAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "응답 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "요청 실패", Toast.LENGTH_SHORT).show()
                Log.e("getNotices", "오류", e)
            }
        }
    }
}