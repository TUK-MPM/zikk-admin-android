package com.example.zikk

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zikk.databinding.ActivityReportWriteBinding
import com.example.zikk.enum.IllegalParkingLocation
import com.example.zikk.extensions.getUserPhoneNumber
import com.example.zikk.model.request.ReportRequest
import com.example.zikk.network.RetrofitClient
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import com.example.zikk.R

class ReportWriteActivity : BaseActivity() {

    private lateinit var binding: ActivityReportWriteBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val imageUriList = mutableListOf<Uri>() // 선택된 이미지 리스트

    // 사진 선택 런처
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUriList.add(it)
            addImageToLayout(it)
        }
    }

    // 사진 권한 요청 런처
    private val photoPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value }
        if (!granted) {
            Toast.makeText(this, "사진 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 위치 권한 요청 런처
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value }
        if (granted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentViewWithBinding(ActivityReportWriteBinding::inflate)
        enableEdgeToEdge()

        // 시스템 바 여백 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.zikk.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 자동으로 위치 가져오기
        getCurrentLocation()

        // 안드로이드 버전에 따라 적절한 권한 요청
        val photoPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        photoPermissionLauncher.launch(photoPermissions)

        // 로그인된 경우, SharedPreferences에서 폰 번호를 자동 입력
        val phoneNumber = getUserPhoneNumber()
        if (!phoneNumber.isNullOrEmpty()) {
            binding.etPhone.setText(phoneNumber)
        }

        // 버튼 리스너 설정
        binding.ivBack.setOnClickListener { finish() }
        binding.btnPickImage.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.btnLocateGet.setOnClickListener { getCurrentLocation() }
        binding.btnSubmit.setOnClickListener { submitReport() }
    }

    // 이미지 변환 함수
    fun createImageMultipartList(context: Context, uris: List<Uri>): List<MultipartBody.Part> {
        return uris.mapIndexed { index, uri ->
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val bytes = inputStream.readBytes()
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
            MultipartBody.Part.createFormData("images", "image$index.jpg", requestFile)
        }
    }

    // 현재 위치 받아오기 및 주소로 변환
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation() {
        // 정밀 또는 대략 위치 권한이 허용되었는지 확인
        val fineGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val coarseGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED


        // 위치 요청 설정
        if (!fineGranted && !coarseGranted) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            locationPermissionLauncher.launch(permissions)
            return
        }

        // 위치 결과 콜백 정의
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return // 마지막 위치 가져오기

                val address = try {
                    val geocoder = Geocoder(this@ReportWriteActivity, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val addrLine = addresses?.getOrNull(0)?.getAddressLine(0) ?: "주소를 변환할 수 없습니다."
                    val regex = Regex("""([가-힣]+시)\s*(.*)""")
                    val match = regex.find(addrLine)
                    if (match != null) {
                        // "시"로 끝나는 행정구역명 + 나머지 전체
                        match.groupValues[1] + " " + match.groupValues[2]
                    } else {
                        addrLine
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    "주소를 변환할 수 없습니다."
                }

                // 주소를 입력란에 설정
                binding.etLocateWrite.setText(address)

                // 위치 업데이트 중단
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // 초기화
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()


        // 위치 업데이트 요청
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // 선택한 이미지를 레이아웃에 추가
    private fun addImageToLayout(uri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(61.dp, 65.dp).apply { marginEnd = 10.dp }
            setImageURI(uri)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setOnClickListener {
                AlertDialog.Builder(this@ReportWriteActivity)
                    .setTitle("사진 삭제")
                    .setMessage("이 사진을 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        imageUriList.remove(uri)
                        binding.photoContainer.removeView(this)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
        val index = binding.photoContainer.indexOfChild(binding.btnPickImage)
        binding.photoContainer.addView(imageView, index)
    }

    // 신고 정보 전송
    private fun submitReport() {
        // 1. 휴대폰 번호 검사
        val phone = binding.etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            Toast.makeText(this, "휴대폰 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!phone.matches(Regex("^01[016789]-?\\d{3,4}-?\\d{4}$"))) {
            Toast.makeText(this, "유효한 휴대폰 번호 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 위치 텍스트 검사
        val address = binding.etLocateWrite.text.toString().trim()
        if (address.isEmpty()) {
            Toast.makeText(this, "위치를 입력하거나 위치 가져오기를 눌러주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 불법 주차 위치 유형 선택 검사
        val selectedType = when (binding.radioGroup.checkedRadioButtonId) {
            com.example.zikk.R.id.radio_crosswalk -> IllegalParkingLocation.DOT_BLOCK.name
            R.id.radio_intersection -> IllegalParkingLocation.TRAFFIC_ISLAND.name
            R.id.radio_bus_stop -> IllegalParkingLocation.PROTECTED_ZONE.name
            R.id.radio_safety_zone -> IllegalParkingLocation.WALKWAY_OTHER.name
            R.id.radio_others -> IllegalParkingLocation.OTHER.name
            else -> null
        }

        if (selectedType == null) {
            Toast.makeText(this, "불법 주차 위치 유형을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. 개인정보 동의 체크 여부
        if (!binding.cbAgree.isChecked) {
            Toast.makeText(this, "개인정보 활용에 동의해야 신고가 가능합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. (선택) 최소 1장의 이미지가 필요하다면 검사
        if (imageUriList.isEmpty()) {
            Toast.makeText(this, "사진을 1장 이상 첨부해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 유효성 검사 통과 후 전송
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. JSON request 객체를 문자열로 변환
                val request = ReportRequest(phone, address, selectedType, listOf())
                val requestJson = com.google.gson.Gson().toJson(request)
                val requestBody = requestJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())


                // 2. 이미지 파일들을 Multipart로 변환
                val imageParts = createImageMultipartList(this@ReportWriteActivity, imageUriList)

                // 3. multipart 전송
                val response = RetrofitClient.apiService.sendLocationWithImages(requestBody, imageParts)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ReportWriteActivity, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.d("ReportWrite", "신고 실패 code=${response.code()}, errorBody=${response.errorBody()?.string()}")
                        Toast.makeText(this@ReportWriteActivity, "신고 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportWrite", "Exception", e) // 추가!
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportWriteActivity, "에러: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // dp 단위 변환 확장 함수
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    // 화면 터치 시 키보드 숨김 처리
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}