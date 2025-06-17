package com.example.zikk.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.zikk.R
import com.example.zikk.extensions.getLoginToken
import com.example.zikk.extensions.removeLoginToken
import com.example.zikk.extensions.removeUserPhoneNumber
import com.example.zikk.extensions.removeUserRole

class HeaderFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_header, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuButton = view.findViewById<ImageButton>(R.id.btn_menu)
        val userButton = view.findViewById<ImageButton>(R.id.btn_user)

        menuButton.setOnClickListener {
            // Activity에서 DrawerLayout 찾기
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.dl_container)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        userButton.setOnClickListener {
            val isLoggedIn = requireContext().getLoginToken()
            if (isLoggedIn == null) {
                showPhoneInputDialog()
            } else {
                showLogoutDialog()
            }
        }

        // 예시 자동완성 리스트
        val suggestions = listOf("사과", "바나나", "포도", "오렌지", "수박")


    }

    private fun showPhoneInputDialog() {
        // 이미 다이얼로그가 표시되어 있는지 확인
        val existingDialog = parentFragmentManager.findFragmentByTag("PhoneInputDialog")
        if (existingDialog != null) return

        val dialog = PhoneDialogFragment.newInstance { phoneNumber ->
            handlePhoneNumberInput(phoneNumber.toString())
        }

        dialog.show(parentFragmentManager, "PhoneInputDialog")
    }

    private fun handlePhoneNumberInput(phoneNumber: String) {
        // 전화번호 입력 완료 처리
        Toast.makeText(requireContext(), "연락처 등록: $phoneNumber", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                requireContext().removeLoginToken()
                requireContext().removeUserRole()
                requireContext().removeUserPhoneNumber()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}