package com.example.linkshare.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.BuildConfig
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMapViewBinding
import com.example.linkshare.util.LocalInfo
import com.example.linkshare.util.LocalSearchResponse
import com.example.linkshare.util.LocalSearchService
import com.example.linkshare.util.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.annotations.concurrent.UiThread
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapViewBinding
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val marker = Marker()
    private var searchHandler = Handler(Looper.getMainLooper())
    private var lastQueryString: String? = null
    private lateinit var retrofitService: LocalSearchService
    private val clientId = BuildConfig.NAVER_CLIENT_ID
    private val clientSecret = BuildConfig.NAVER_CLIENT_SECRET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sfm = supportFragmentManager
        val mapFragment = sfm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                sfm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        val sheetBehavior = BottomSheetBehavior.from(binding.include.bottomSheet)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        sheetBehavior.isDraggable = true

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        retrofitService = RetrofitClient.instance.create(LocalSearchService::class.java)

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty() && query != lastQueryString) {
                    searchHandler.removeCallbacksAndMessages(null)
                    searchHandler.postDelayed({
                        performSearch(query)
                    }, 500)
                }
            }
        })

        binding.autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.adapter.getItem(position) as LocalInfo
            // 좌표 값 가져오기
            val latLng = LatLng(selectedItem.mapy.toDouble() / 1E7, selectedItem.mapx.toDouble() / 1E7)
            getMarker(latLng.latitude, latLng.longitude)
            getAddress(latLng.latitude, latLng.longitude)

            // 해당 좌표로 카메라 이동
            val cameraUpdate = CameraUpdate.scrollTo(latLng)
            naverMap.moveCamera(cameraUpdate)

            val selectedTitle = selectedItem.title.replace(Regex("<[^>]*>"), "")
            binding.autoCompleteTextView.setText(selectedTitle)
            // 선택 후 키보드 숨김
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.autoCompleteTextView.windowToken, 0)

            sendMapDate(selectedTitle, latLng.latitude, latLng.longitude, selectedItem.roadAddress)
            binding.include.tvCategory.text = selectedItem.category
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        // 현재 위치
        naverMap.locationSource = locationSource
        // 현재 위치 버튼 기능
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.uiSettings.isZoomControlEnabled = false

        // Intent에서 경도, 위도, 제목 데이터 추출 및 처리
        intent?.run {
            getDoubleExtra("longitude", 0.0).takeIf { it != 0.0 }?.let { longitude ->
                getDoubleExtra("latitude", 0.0).takeIf { it != 0.0 }?.let { latitude ->
                    // 마커 생성 및 카메라 이동
                    getMarker(latitude, longitude)
                    naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(latitude, longitude)))
                }
            }
            getStringExtra("title")?.substringBeforeLast("(")?.trimEnd()?.let { title ->
                binding.include.tvName.text = title
                binding.include.tvCategory.visibility = View.GONE
            }
        }

        naverMap.setOnMapLongClickListener { _, latLng ->
            getMarker(latLng.latitude, latLng.longitude)
            getAddress(latLng.latitude, latLng.longitude)
        }

        // 심볼 클릭했을 때 마커 찍히는 기능
        naverMap.setOnSymbolClickListener { symbol ->
            getMarker(symbol.position.latitude, symbol.position.longitude)
            getAddress(symbol.position.latitude, symbol.position.longitude, symbol.caption)
            true
        }
    }

    // MemoActivity로 데이터 전송
    private fun sendMapDate(address: String, latitude: Double, longitude: Double, roadAddress: String = "") {
        binding.include.tvName.text = address

        // 버튼 클릭 시 업체명과 도로명주소 Memo로 데이터 전달
        binding.include.btnSelect.setOnClickListener {
            val title = if (roadAddress.isNotBlank()) "$address ($roadAddress)" else address
            val data = Intent().apply {
                putExtra("title", title)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun performSearch(query: String) {
        lastQueryString = query
        val call = retrofitService.searchLocal(clientId, clientSecret, query)
        call.enqueue(object : Callback<LocalSearchResponse> {
            override fun onResponse(call: Call<LocalSearchResponse>, response: Response<LocalSearchResponse>) {
                if (response.isSuccessful) {
                    val items = response.body()?.items ?: listOf()

                    // 장소명에 키워드가 포함된 항목 필터링
                    val titleMatches = items.filter { it.title.contains(query, true) }

                    // 도로명주소에 키워드가 포함된 항목 필터링
                    val addressMatches = items.filter { it.roadAddress.contains(query, true) && !it.title.contains(query, true) }

                    // 두 결과를 합침 (장소명 매칭 결과를 우선으로)
                    val combinedResults = titleMatches + addressMatches
                    // Adapter 연결
                    val adapter = object : ArrayAdapter<LocalInfo>(this@MapViewActivity, R.layout.place_item, combinedResults) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.place_item, parent, false)
                            val item = getItem(position)
                            view.findViewById<TextView>(R.id.tv_place_name).text = item?.title?.replace(Regex("<[^>]*>"), "")
                            view.findViewById<TextView>(R.id.tv_road_address).text = item?.roadAddress?.replace(Regex("<[^>]*>"), "")
                            return view
                        }
                    }
                    binding.autoCompleteTextView.setAdapter(adapter)
                    binding.autoCompleteTextView.showDropDown()
                }
            }

            override fun onFailure(call: Call<LocalSearchResponse>, t: Throwable) {
                // 오류 처리
            }
        })
    }

    // 마커 생성
    private fun getMarker(latitude: Double, longitude: Double) {
        marker.position = LatLng(latitude, longitude)
        marker.map = naverMap
    }

    // Geocoder로 주소 가져오기
    private fun getAddress(latitude: Double, longitude: Double, placeTitle: String = "") {
        val geocoder = Geocoder(applicationContext, Locale.KOREAN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude, longitude, 1
            ) { address ->
                if (address.size != 0) {
                    val addressString = placeTitle.ifBlank { address[0].getAddressLine(0) }
                    sendMapDate(addressString, latitude, longitude)
                    binding.include.tvCategory.visibility = View.GONE
                }
            }
        } else {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                val addressString = placeTitle.ifBlank { addresses[0].getAddressLine(0) }
                sendMapDate(addressString, latitude, longitude)
                binding.include.tvCategory.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}