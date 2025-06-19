package com.example.zikk.util

import android.content.Context
import android.view.ViewGroup
import com.example.zikk.util.PaginationUtils.getPageRange

object PaginationUiUtils {

    // 페이지 번호 버튼 생성
    fun createPageButton(
        context: Context,
        text: String,
        isCurrent: Boolean = false,
        onClick: () -> Unit
    ) = android.widget.TextView(context).apply {
        this.text = text
        textSize = 20f
        setPadding(12, 6, 12, 6)
        setOnClickListener { onClick() }
        setTextColor(if (isCurrent) android.graphics.Color.BLACK else android.graphics.Color.DKGRAY)
        paint.isUnderlineText = isCurrent
        setTypeface(null, if (isCurrent) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    // "..." 생략 표시 생성
    fun createDots(context: Context) = android.widget.TextView(context).apply {
        text = "..."
        textSize = 16f
        setPadding(12, 6, 12, 6)
        setTextColor(android.graphics.Color.GRAY)
    }

    // 전체 페이지네이션 UI 구성 함수
    fun setupPagination(
        context: Context,
        container: ViewGroup,
        currentPage: Int,
        totalPages: Int,
        onPageClick: (Int) -> Unit
    ) {
        container.removeAllViews()

        container.addView(createPageButton(context, "<") {
            if (currentPage > 1) onPageClick(currentPage - 1)
        })

        val pageRange = getPageRange(currentPage, totalPages)

        if (pageRange.first() > 1) {
            container.addView(createPageButton(context, "1") { onPageClick(1) })
            if (pageRange.first() > 2) container.addView(createDots(context))
        }

        for (i in pageRange) {
            container.addView(createPageButton(context, i.toString(), i == currentPage) {
                onPageClick(i)
            })
        }

        if (pageRange.last() < totalPages) {
            if (pageRange.last() < totalPages - 1) container.addView(createDots(context))
            container.addView(createPageButton(context, totalPages.toString()) { onPageClick(totalPages) })
        }

        container.addView(createPageButton(context, ">") {
            if (currentPage < totalPages) onPageClick(currentPage + 1)
        })
    }
}
