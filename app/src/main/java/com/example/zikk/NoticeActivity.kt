package com.example.zikk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zikk.adapter.NoticeAdapter
import com.example.zikk.databinding.ActivityNoticeBinding
import com.example.zikk.extensions.getUserRole
import com.example.zikk.model.Notice
import com.example.zikk.network.RetrofitClient
import com.example.zikk.util.PaginationUiUtils
import com.example.zikk.util.PaginationUtils
import com.example.zikk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoticeActivity : BaseActivity() {
    private lateinit var binding: ActivityNoticeBinding
    private var currentPage = 1
    private val pageSize = 5
    private var allNotices: List<Notice> = emptyList()
    private var isDescending = true
    private var isSpinnerInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentViewWithBinding(ActivityNoticeBinding::inflate)


        initUI()
        fetchNotices()

        //binding.btnWrite.visibility =  if (isAdmin()) View.GONE else View.VISIBLE


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initUI() {
        binding.noticeRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.noticeRecyclerView.adapter = NoticeAdapter(emptyList()) {}

        binding.btnWrite.visibility = if (getUserRole() == "ROLE_ADMIN") View.VISIBLE else View.GONE

        setupSpinner()
        setupListeners()
    }

    private fun setupSpinner() {
        val items = arrayOf("최신순", "오래된순")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        binding.mySpinner.adapter = adapter
        binding.mySpinner.setSelection(0)

        binding.mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }
                isDescending = position == 0
                applySortAndLoad()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupListeners() {
        binding.back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.btnWrite.setOnClickListener{
            startActivity(Intent(this, AdminNoticeWriteActivity::class.java))
        }
    }

    private fun fetchNotices() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getNotices(
                    page = currentPage - 1, // 0-based index
                    size = pageSize
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.content ?: emptyList()
                    allNotices = content // 🔹 정렬용 전체 데이터 저장
                    val totalPages = body?.totalPages ?: 1

                    Log.d("fetchNotices", "받아온 공지 수: ${content.size}")

                    withContext(Dispatchers.Main) {
                        // 정렬: 서버에서 정렬된 상태면 생략 가능
                        val sorted = PaginationUtils.sortByDate(content, isDescending) { it.createdAt }

                        // 어댑터 설정
                        binding.noticeRecyclerView.adapter = NoticeAdapter(sorted) { notice ->
                            val intent = Intent(this@NoticeActivity, NoticeContentActivity::class.java)
                            intent.putExtra("notiId", notice.notiId)
                            startActivity(intent)
                        }

                        // 페이지 UI 설정
                        PaginationUiUtils.setupPagination(
                            context = this@NoticeActivity,
                            container = binding.paginationLayout,
                            currentPage = currentPage,
                            totalPages = totalPages,
                            onPageClick = { selectedPage ->
                                currentPage = selectedPage
                                fetchNotices()
                            }
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("서버 응답 실패: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("fetchNotices", "Exception occurred", e)
                withContext(Dispatchers.Main) {
                    showToast("페이지를 로드하는 중 오류가 발생했습니다.")
                }
            }
        }
    }

    private fun applySortAndLoad() {
        val sorted = PaginationUtils.sortByDate(allNotices, isDescending) { it.createdAt }
        loadPageSafely(1, sorted)
    }

    private fun loadPageSafely(page: Int, list: List<Notice>) {
        try {
            if (list.isEmpty()) {
                showToast("공지사항이 없습니다.")
                binding.noticeRecyclerView.adapter = NoticeAdapter(emptyList()) {}
                binding.paginationLayout.removeAllViews()
                return
            }

            currentPage = page
            val pageList = PaginationUtils.paginate(list, page, pageSize)

            binding.noticeRecyclerView.adapter = NoticeAdapter(pageList) { notice ->
                val intent = Intent(this, NoticeContentActivity::class.java)
                intent.putExtra("notiId", notice.notiId)
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
            Log.e("loadPageSafely", "예외 발생", e)
            showToast("페이지를 로드하는 중 오류가 발생했습니다.")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun isAdmin(): Boolean {
        return getUserRole() == "ROLE_ADMIN"
    }
}
