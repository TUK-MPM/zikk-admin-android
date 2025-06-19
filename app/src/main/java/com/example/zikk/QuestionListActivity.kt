package com.example.zikk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.zikk.util.PopupUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zikk.adapter.InquiryAdapter
import com.example.zikk.databinding.ActivityQuestionListBinding
import com.example.zikk.databinding.ActivityReportDetailBinding
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.model.Inquiry
import com.example.zikk.network.RetrofitClient
import com.example.zikk.util.PaginationUiUtils
import com.example.zikk.util.PaginationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.zikk.extensions.getUserRole

class QuestionListActivity : BaseActivity() {

    private lateinit var binding: ActivityQuestionListBinding
    private var currentPage = 1
    private val pageSize = 5
    private var allInquiries: List<Inquiry> = emptyList()
    private var isDescending = true
    private lateinit var token: String
    private var displayedList: List<Inquiry> = emptyList()    // 필터 및 정렬된 리스트
    private var currentFilter: String? = null                // 현재 선택된 상태 필터
    private var currentSortDescending: Boolean = true        // 정렬 순서: true = 최신순

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentViewWithBinding(ActivityQuestionListBinding::inflate)
        binding.btnSortStatus.setOnClickListener { showSortPopup(it) }

        val rawToken = getLoginToken()
        if (rawToken == null) {
            showToast("로그인이 필요합니다.")
            finish()
            return
        }
        token = "Bearer $rawToken"

        initUI()
        fetchInquiries()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initUI() {
        binding.inquiryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.inquiryRecyclerView.adapter = InquiryAdapter(emptyList()) {}

        setupListeners()
    }


    private fun setupListeners() {
        binding.back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnWrite.setOnClickListener {
            startActivity(Intent(this, QuestionWriteActivity::class.java))
        }
    }

    private fun fetchInquiries() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getInquiries(
                    token = token,
                    page = 0,
                    size = 100,
                )
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    Log.d("fetchInquiries", "받아온 문의 수: ${data.size}")
                    withContext(Dispatchers.Main) {
                        allInquiries = data
                        applySortAndLoad()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("서버 응답 실패: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("fetchInquiries", "Exception occurred", e)
                withContext(Dispatchers.Main) {
                    showToast("페이지를 로드하는 중 오류가 발생했습니다.")
                }
            }
        }
    }

    private fun applySortAndLoad() {
        val sorted = PaginationUtils.sortByDate(allInquiries, isDescending) { it.createdAt }
        loadPageSafely(1, sorted)
    }

    private fun loadPageSafely(page: Int, list: List<Inquiry>) {
        try {
            if (list.isEmpty()) {
                showToast("문의 내역이 없습니다.")
                binding.inquiryRecyclerView.adapter = InquiryAdapter(emptyList()) {}
                binding.paginationLayout.removeAllViews()
                return
            }

            currentPage = page
            val pageList = PaginationUtils.paginate(list, page, pageSize)

            binding.inquiryRecyclerView.adapter = InquiryAdapter(pageList) { inquiry ->
                val intent: Intent

                if(isAdmin()) {
                    intent =  Intent(this, AdminQuestionContentAcitivity::class.java)
                } else {
                    intent = Intent(this, QuestionContentActivity::class.java)
                }

                intent.putExtra("inquiryId", inquiry.inquiryId)
                startActivity(intent)
            }

            val totalPages = PaginationUtils.getTotalPages(list.size, pageSize)
            PaginationUiUtils.setupPagination(
                context = this,
                container = binding.paginationLayout,
                currentPage = page,
                totalPages = totalPages,
                onPageClick = { selectedPage ->
                    loadPageSafely(selectedPage, list)
                }
            )
        } catch (e: Exception) {
            Log.e("loadPage", "페이징 처리 오류", e)
            showToast("페이지를 로드하는 중 오류가 발생했습니다.")
        }
    }

    private fun applyFilterAndSort() {
        // allInquiries에서 필터 적용
        val filtered = if (currentFilter == null) {
            allInquiries
        } else {
            allInquiries.filter { it.status == currentFilter } // Inquiry 객체에 status가 있다고 가정
        }

        val sorted = PaginationUtils.sortByDate(filtered, currentSortDescending) { it.createdAt }

        if (sorted.isEmpty()) {
            Toast.makeText(this, "문의 사항이 없습니다.", Toast.LENGTH_SHORT).show()
            binding.inquiryRecyclerView.adapter = InquiryAdapter(emptyList()) {}
            binding.paginationLayout.removeAllViews()
            return
        }

        displayedList = sorted
        loadPageSafely(1, sorted)
    }


    private fun showSortPopup(anchor: View) {
        PopupUtils.showSortPopup(this, anchor) { isDescending ->
            currentSortDescending = isDescending
            binding.btnSortStatus.text = if (isDescending) "최신순으로 나열" else "오래된 순으로 나열"
            applyFilterAndSort()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun isAdmin(): Boolean {
        return getUserRole() == "ROLE_ADMIN"
    }

    override fun onResume() {
        super.onResume()
        fetchInquiries()
    }
}
