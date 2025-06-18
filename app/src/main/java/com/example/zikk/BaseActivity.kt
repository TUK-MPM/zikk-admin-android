package com.example.zikk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewbinding.ViewBinding
import com.example.zikk.databinding.ActivityBaseBinding
import com.example.zikk.R

abstract class BaseActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    private lateinit var baseBinding: ActivityBaseBinding // binding for base layout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DrawerDebug", "BaseActivity onCreate called")
    }

    fun <T : ViewBinding> setContentViewWithBinding(
        bindingInflater: (layoutInflater: android.view.LayoutInflater) -> T
    ): T {
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        val contentBinding = bindingInflater.invoke(layoutInflater)

        // contentBinding의 root를 baseContent에 붙이기
        baseBinding.flBaseContainer.addView(contentBinding.root)

        // drawer 레이아웃 초기화
        super.setContentView(baseBinding.root)
        drawerLayout = baseBinding.dlContainer
        setupDrawer()

        return contentBinding
    }

    override fun setContentView(layoutResID: Int) {
        // 1. base 레이아웃 inflate
        val fullLayout = layoutInflater.inflate(R.layout.activity_base, null)

        // 2. baseContent 영역에 실제 액티비티 레이아웃 inflate
        val container = fullLayout.findViewById<FrameLayout>(R.id.fl_base_container)
        layoutInflater.inflate(layoutResID, container, true)

        // 3. 화면에 최종 세팅
        super.setContentView(fullLayout)

        // 4. 드로어 초기화
        drawerLayout = findViewById(R.id.fl_drawer_container)
        setupDrawer()
    }

    private fun setupDrawer() {
        val drawerContainer = findViewById<FrameLayout>(R.id.fl_drawer_container)

        // ViewBinding 사용
        val drawerBinding = com.example.zikk.databinding.LayoutDrawerBinding.inflate(
            layoutInflater,
            drawerContainer,
            true
        )

        // 공통 버튼 클릭 이벤트 처리
        drawerBinding.btnNotice.setOnClickListener {
            startActivity(Intent(this, NoticeActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerBinding.btnInquiryList.setOnClickListener {
            startActivity(Intent(this, QuestionListActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerBinding.btnReportList.setOnClickListener {
            startActivity(Intent(this, ReportListActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

}
