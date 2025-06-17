package com.example.zikk.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PaginationUtils {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // 제너릭 정렬 함수: Report, Inquiry 모두 지원
    fun <T> sortByDate(list: List<T>, descending: Boolean = true, getDate: (T) -> String): List<T> {
        return if (descending) {
            list.sortedByDescending { LocalDateTime.parse(getDate(it), formatter) }
        } else {
            list.sortedBy { LocalDateTime.parse(getDate(it), formatter) }
        }
    }

    // 제너릭 페이지네이션
    fun <T> paginate(list: List<T>, page: Int, pageSize: Int): List<T> {
        val fromIndex = (page - 1) * pageSize
        val toIndex = (fromIndex + pageSize).coerceAtMost(list.size)
        return if (fromIndex in 0 until list.size) list.subList(fromIndex, toIndex) else emptyList()
    }

    fun getTotalPages(listSize: Int, pageSize: Int): Int {
        return (listSize + pageSize - 1) / pageSize
    }

    fun getPageRange(currentPage: Int, totalPages: Int, range: Int = 2): List<Int> {
        val startPage = (currentPage - range).coerceAtLeast(1)
        val endPage = (currentPage + range).coerceAtMost(totalPages)
        return (startPage..endPage).toList()
    }
}
