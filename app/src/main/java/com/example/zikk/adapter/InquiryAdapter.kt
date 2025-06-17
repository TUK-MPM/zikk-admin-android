package com.example.zikk.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zikk.R
import com.example.zikk.databinding.ItemQuestionBinding
import com.example.zikk.model.Inquiry

class InquiryAdapter(
    private val inquiries: List<Inquiry>,
    private val onItemClick: (Inquiry) -> Unit
) : RecyclerView.Adapter<InquiryAdapter.InquiryViewHolder>() {

    inner class InquiryViewHolder(private val binding: ItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(inquiry: Inquiry) {
            binding.topText.text = inquiry.title

            val formattedTime = inquiry.createdAt
                .replace("T", " ")
                .substring(0, 10)

            binding.bottomText.text = formattedTime
            binding.root.setOnClickListener {
                Log.d("InquiryAdapter", "클릭된 inquiryId: ${inquiry.inquiryId}")
                onItemClick(inquiry)
            }

            // 배경색 조건 설정 (선택)
            val bgColorRes = when (inquiry.status) {
                "WAITING" -> R.color.qusetion_yellow
                "COMPLETED" -> R.color.qusetion_blue
                else -> R.color.background_gray
            }
            binding.root.setBackgroundResource(R.drawable.round_box_medium)
            binding.root.backgroundTintList = binding.root.context.getColorStateList(bgColorRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InquiryViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InquiryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InquiryViewHolder, position: Int) {
        holder.bind(inquiries[position])
    }

    override fun getItemCount(): Int = inquiries.size
}
