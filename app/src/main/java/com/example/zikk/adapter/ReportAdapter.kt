package com.example.zikk.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.zikk.R
import com.example.zikk.databinding.ItemReportBinding
import com.example.zikk.enum.IllegalParkingLocation
import com.example.zikk.model.Report

class ReportAdapter(
    private val items: List<Report>,
    private val onItemClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(report: Report) {

            Log.d("ReportAdapter", "report.where = ${report.where}")

            // 위치 enum 변환
            val whereDesc = try {
                IllegalParkingLocation.valueOf(report.where).description
            } catch (e: Exception) {
                "기타"
            }

            // "시/구/동"까지만 추출
            val addressToShow = if (!report.address.isNullOrEmpty()) {
                Regex("""([가-힣]+시\s)?([가-힣]+구\s)?([가-힣0-9]+동)""")
                    .find(report.address)?.value?.trim() ?: report.address
            } else {
                "주소 정보 없음"  // 또는 ""
            }
            binding.txtAddress.text = "신고위치: $addressToShow"

            binding.txtStatus.text = "신고유형: $whereDesc"

            // 분단위 까지만
            val formattedDate = report.createdAt.replace("T", " ")
            val dateToShow = if (formattedDate.length >= 16) {
                formattedDate.substring(0, 16) // "2024-06-07 10:20"
            } else {
                formattedDate
            }
            binding.txtDate.text = "신고시간: $dateToShow"

            // 배경색 조건 설정
            val bgColorRes = when (report.status) {
                "PENDING" -> R.color.milk
                "APPROVED" -> R.color.navi
                "REJECTED" -> R.color.deap_orange
                else -> R.color.background_gray
            }
            binding.root.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, bgColorRes))

            // 텍스트 색 조건 설정
            val textColorRes = when (report.status) {
                "APPROVED" -> R.color.white
                else -> R.color.black
            }
            val context = binding.root.context
            binding.txtStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
            binding.txtAddress.setTextColor(ContextCompat.getColor(context, textColorRes))
            binding.txtDate.setTextColor(ContextCompat.getColor(context, textColorRes))


            // 화살표 표시 여부
            binding.imageArrow.visibility = if (report.status == "REJECTED" || report.status == "APPROVED") View.GONE else View.VISIBLE

            binding.root.setOnClickListener { onItemClick(report) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        android.util.Log.d("ReportAdapter", "bind position=$position: ${items[position]}")
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
