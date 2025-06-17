package com.example.zikk.ui

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.auth0.android.jwt.JWT
import com.example.zikk.R
import com.example.zikk.extensions.removeLoginToken
import com.example.zikk.extensions.removeUserPhoneNumber
import com.example.zikk.extensions.removeUserRole
import com.example.zikk.extensions.saveLoginToken
import com.example.zikk.extensions.saveUserPhoneNumber
import com.example.zikk.extensions.saveUserRole
import com.example.zikk.model.request.LoginRequest
import com.example.zikk.network.RetrofitClient
import kotlinx.coroutines.launch

class PhoneDialogFragment : DialogFragment() {

    private var onPhoneEnteredListener: ((String) -> Unit)? = null
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return dialog
    }

    private fun initViews(view: View) {
        etPhone = view.findViewById(R.id.et_phone)
        etPassword = view.findViewById(R.id.et_password)
        btnConfirm = view.findViewById(R.id.btn_confirm)

        // 초기 상태에서 확인 버튼 비활성화
        btnConfirm.isEnabled = false
    }

    private fun setupListeners() {
        // 전화번호 입력 감지
        etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phoneNumber = s.toString().trim()
                // 전화번호가 입력되었을 때만 확인 버튼 활성화
                btnConfirm.isEnabled = phoneNumber.isNotEmpty() && isValidPhoneNumber(phoneNumber)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 확인 버튼 클릭 리스너
        btnConfirm.setOnClickListener {
            val phoneNumber = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (phoneNumber.isEmpty()) {
                Toast.makeText(requireContext(), "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(requireContext(), "올바른 전화번호 형식이 아닙니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 콜백 실행
            onPhoneEnteredListener?.invoke(phoneNumber)
//
            performLogin(phoneNumber, password)
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // 한국 전화번호 형식 검증
        // 010, 011, 016, 017, 018, 019로 시작하는 10-11자리 숫자
        val phonePattern = "^01([0|1|6|7|8|9])[0-9]{7,8}$".toRegex()
        val cleanPhoneNumber = phoneNumber.replace("-", "").replace(" ", "")
        return cleanPhoneNumber.length >= 10 && phonePattern.matches(cleanPhoneNumber)
    }

    private fun performLogin(phoneNumber: String, password: String) {
        // 버튼 비활성화하여 중복 클릭 방지
        btnConfirm.isEnabled = false
        btnConfirm.text = "로그인 중..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(phone = phoneNumber, password = password)
                val response = RetrofitClient.apiService.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val decodeJWT: JWT = JWT(loginResponse.token)
                    val role = decodeJWT.getClaim("role").asString()!!

                    // SharedPreferences에 토큰 저장
                    saveTokenToSharedPreferences(loginResponse.token, phoneNumber, role)
                    if (role == "ROLE_ADMIN") {
                        Toast.makeText(
                            requireContext(),
                            "관리자 로그인 성공!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "로그인 성공!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                    Log.d("Login", "로그인 성공 - UserId: ${loginResponse.userId}")

                    // 콜백 실행 (필요한 경우)
                    onPhoneEnteredListener?.invoke(phoneNumber)

                    // 다이얼로그 닫기
                    dismiss()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "로그인 실패: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.e("Login", "로그인 실패 - Code: ${response.code()}")

                    // 버튼 다시 활성화
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "확인"
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

                Log.e("Login", "네트워크 오류", e)

                // 버튼 다시 활성화
                btnConfirm.isEnabled = true
                btnConfirm.text = "확인"
            }
        }
    }

    private fun saveTokenToSharedPreferences(token: String, phoneNumber: String, role: String) {
        requireContext().removeLoginToken()
        requireContext().removeUserPhoneNumber()
        requireContext().removeUserRole()

        requireContext().saveLoginToken(token)
        requireContext().saveUserPhoneNumber(phoneNumber)
        requireContext().saveUserRole(role)
    }

    companion object {
        @JvmStatic
        fun newInstance(param: (Any) -> Unit) =
            PhoneDialogFragment().apply {

            }
    }
}