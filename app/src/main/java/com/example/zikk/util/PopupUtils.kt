package com.example.zikk.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.example.zikk.R

object PopupUtils {

    fun showSortPopup(
        context: Context,
        anchor: View,
        onSortSelected: (descending: Boolean) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.sort_popup_filter, null)
        val popup = PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        view.findViewById<TextView>(R.id.item_latest).setOnClickListener {
            onSortSelected(true)
            popup.dismiss()
        }

        view.findViewById<TextView>(R.id.item_oldest).setOnClickListener {
            onSortSelected(false)
            popup.dismiss()
        }

        popup.elevation = 8f
        popup.showAsDropDown(anchor)
    }

    fun showFilterPopup(
        context: Context,
        anchor: View,
        onFilterSelected: (filter: String?) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.state_popup_filter, null)
        val popup = PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        view.findViewById<TextView>(R.id.item_all).setOnClickListener {
            onFilterSelected(null)
            popup.dismiss()
        }

        view.findViewById<TextView>(R.id.item_completed).setOnClickListener {
            onFilterSelected("APPROVED")
            popup.dismiss()
        }

        view.findViewById<TextView>(R.id.item_processing).setOnClickListener {
            onFilterSelected("PENDING")
            popup.dismiss()
        }

        view.findViewById<TextView>(R.id.item_rejected).setOnClickListener {
            onFilterSelected("REJECTED")
            popup.dismiss()
        }

        popup.elevation = 8f
        popup.showAsDropDown(anchor)
    }
}