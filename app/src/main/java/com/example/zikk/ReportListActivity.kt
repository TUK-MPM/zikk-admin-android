package com.example.zikk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zikk.adapter.ReportAdapter
import com.example.zikk.databinding.ActivityReportListBinding
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.extensions.getUserRole
import com.example.zikk.model.Report
import com.example.zikk.network.RetrofitClient
import com.example.zikk.util.PaginationUiUtils
import com.example.zikk.util.PaginationUtils
import com.example.zikk.util.PopupUtils
import com.example.zikk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportListActivity : BaseActivity() {

    private lateinit var binding: ActivityReportListBinding  // ViewBinding 객체
    private var currentPage = 1                              // 현재 페이지 번호
    private val pageSize = 4                                 // 한 페이지에 보여줄 아이템 수
    private var displayedList: List<Report> = emptyList()    // 필터 및 정렬된 리스트
    private var currentFilter: String? = null                // 현재 선택된 상태 필터
    private var currentSortDescending: Boolean = true        // 정렬 순서: true = 최신순
    private lateinit var token: String                       // 토큰 선언 (초기화는 나중에)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentViewWithBinding(ActivityReportListBinding::inflate)

        binding.tvReportListTitle.text = if (isAdmin()) "신고 내역" else "내 신고 내역"

        // 1. 토큰 유효성 체크
        val rawToken = getLoginToken()
        if (rawToken == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        token = "Bearer $rawToken"

        Log.d("Log", token)

        // 2. 이후 로직 초기화
        // 상태바, 네비게이션바 영역 피해서 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView 레이아웃 설정
        binding.reportRecyclerView.layoutManager = LinearLayoutManager(this)

        // 서버에서 데이터 불러오기
        fetchReports()

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener { finish() }

        // 상태 필터 팝업
        binding.btnFilter.setOnClickListener { showCustomPopup(it) }

        // 정렬 순서 팝업
        binding.btnSortStatus.setOnClickListener { showSortPopup(it) }

        // 처리중 아이콘 클릭 시 "PENDING" 필터 적용
        binding.filterProcessing.setOnClickListener {
            currentFilter = "PENDING"
            binding.btnFilter.text = "처리중만 보기"
            applyFilterAndSort()
        }

        // 승인 아이콘 클릭 시 "COMPLETED" 필터 적용
        binding.filterCompleted.setOnClickListener {
            currentFilter = "APPROVED"
            binding.btnFilter.text = "승인만 보기"
            applyFilterAndSort()
        }

        // 반려 아이콘 클릭 시 "REJECTED" 필터 적용
        binding.filterRejected.setOnClickListener {
            currentFilter = "REJECTED"
            binding.btnFilter.text = "반려만 보기"
            applyFilterAndSort()
        }

    }

    // 사진을 +로 등록하면 서버에 보내서 S3 URL을 받아서 신고할 때 보내는 느낌.
    // gpt에게 물어봐야 할 것. multipart form data을 통해서 서버에 보내느 방법을 적용시켜서 만들어라

    // 서버에서 신고 목록 API 호출
    private fun fetchReports() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getReports(
                    token = token,
                    page = 0,
                    size = 100,
                )
                if (response.isSuccessful) {
                    val data = response.body()?: emptyList()
                    // ★ 여기에 로그 추가!
                    data.forEach { report ->
                        Log.d("ReportFetch", "report: $report")
                    }
                    withContext(Dispatchers.Main) {
                        displayedList = data
                        applyFilterAndSort() // 필터링 및 정렬 적용
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReportListActivity, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportListActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 현재 필터 및 정렬 기준에 따라 리스트 갱신
    private fun applyFilterAndSort() {
        val filtered = displayedList.filter { currentFilter == null || it.status == currentFilter }
        val sorted = PaginationUtils.sortByDate(filtered, currentSortDescending) { it.createdAt }

        // 신고 내역이 없으면 안내 메시지 및 UI 정리
        if (sorted.isEmpty()) {
            Toast.makeText(this, "신고 내역이 없습니다.", Toast.LENGTH_SHORT).show()
            binding.reportRecyclerView.adapter = ReportAdapter(emptyList()) {}
            binding.paginationLayout.removeAllViews() // 페이지네이션 초기화
            return
        }

        loadPage(1, sorted)
    }

    // 페이징 처리를 위한 특정 페이지 데이터만 출력
    private fun loadPage(page: Int, list: List<Report>) {
        currentPage = page
        val pageList = PaginationUtils.paginate(list, page, pageSize)

        // 어댑터 설정 및 아이템 클릭 이벤트 처리
        binding.reportRecyclerView.adapter = ReportAdapter(pageList) { report ->
            val intent = Intent(this, ReportDetailActivity::class.java)
            intent.putExtra("reportId", report.reportId)
            startActivity(intent)
        }

        // 페이지네이션 UI 세팅
        val totalPages = PaginationUtils.getTotalPages(list.size, pageSize)
        PaginationUiUtils.setupPagination(
            context = this,
            container = binding.paginationLayout,
            currentPage = page,
            totalPages = totalPages,
            onPageClick = { selectedPage -> loadPage(selectedPage, list) }
        )
    }

    // 정렬 팝업 표시 (최신순/오래된 순)
    private fun showSortPopup(anchor: View) {
        PopupUtils.showSortPopup(this, anchor) { isDescending ->
            currentSortDescending = isDescending
            binding.btnSortStatus.text = if (isDescending) "최신순으로 나열" else "오래된 순으로 나열"
            applyFilterAndSort()
        }
    }

    // 상태 필터 팝업 표시 (전체/처리중/승인/반려)
    private fun showCustomPopup(anchor: View) {
        PopupUtils.showFilterPopup(this, anchor) { filter ->
            currentFilter = filter
            binding.btnFilter.text = when (filter) {
                null -> "전체 보기"
                "COMPLETED" -> "승인만 보기"
                "PROCESSING" -> "처리중만 보기"
                "REJECTED" -> "반려만 보기"
                else -> "전체 보기"
            }
            applyFilterAndSort()
        }
    }

    private fun isAdmin(): Boolean {
        if(getUserRole() == "ROLE_ADMIN") {
            return true
        } else {
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        fetchReports()
    }
}
