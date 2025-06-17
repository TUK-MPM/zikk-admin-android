package com.example.zikk.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zikk.R
import com.example.zikk.model.Notice

class NoticeAdapter(
    private var notices: List<Notice>,
    private val onItemClick: (Notice) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_notice_title)
        val dateTextView: TextView = itemView.findViewById(R.id.tv_notice_created_at)

        fun bind(notice: Notice) {
            titleTextView.text = notice.title

            // createdAt이 "2025-06-07T11:36:16.774078" 형식일 경우, 앞의 "YYYY-MM-DD"까지만 표시
            val date = if (notice.createdAt.length >= 16) {
                notice.createdAt.replace("T", " ").substring(0, 10)
            } else {
                notice.createdAt.replace("T", " ")
            }
            dateTextView.text = date


            // ✅ ID 로그 찍기
            Log.d("NoticeAdapter", "바인딩된 공지 ID: ${notice.notiId}, 제목: ${notice.title}")

            itemView.setOnClickListener {
                Log.d("NoticeAdapter", "${notice.title} 클릭됨")
                onItemClick(notice)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun getItemCount(): Int = notices.size

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(notices[position])
    }

    // 외부에서 데이터 교체 및 갱신 시 사용
    fun updateData(newNotices: List<Notice>) {
        this.notices = newNotices
        notifyDataSetChanged()
    }

    // (선택) RecyclerView 성능 최적화를 위한 ID 제공
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
