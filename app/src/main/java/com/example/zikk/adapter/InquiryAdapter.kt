package com.example.zikk.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
                "WAITING" -> R.color.milk
                "COMPLETED" -> R.color.deap_blue
                else -> R.color.background_gray
            }
            binding.root.setBackgroundResource(R.drawable.round_box_medium)
            binding.root.backgroundTintList = binding.root.context.getColorStateList(bgColorRes)

            if (bgColorRes == R.color.deap_blue) {
                binding.topText.setTextColor(binding.root.context.getColor(R.color.white))
                binding.bottomText.setTextColor(binding.root.context.getColor(R.color.white))
            } else {
                // 기본 글자색으로 변경 (필요하면)
                binding.topText.setTextColor(binding.root.context.getColor(R.color.black))
                binding.bottomText.setTextColor(binding.root.context.getColor(R.color.black))
            }
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
