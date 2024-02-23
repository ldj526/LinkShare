package com.example.linkshare.view

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMapViewBinding
import com.example.linkshare.util.LocalInfo
import com.example.linkshare.util.LocalSearchResponse
import com.example.linkshare.util.LocalSearchService
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapViewBinding
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val marker = Marker()

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
        sheetBehavior.peekHeight = 500
        sheetBehavior.isDraggable = true

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://openapi.naver.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(LocalSearchService::class.java)

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    val call = service.searchLocal("Client_Id", "client_secret", query)
                    call.enqueue(object : Callback<LocalSearchResponse> {
                        override fun onResponse(call: Call<LocalSearchResponse>, response: Response<LocalSearchResponse>) {
                            if (response.isSuccessful) {
                                val items = response.body()?.items ?: listOf()
                                // Adapter 연결
                                val adapter = object : ArrayAdapter<LocalInfo>(this@MapViewActivity, R.layout.place_item, items) {
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
            }
        })

        binding.autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.adapter.getItem(position) as LocalInfo
            // 좌표 값 가져오기
            val latLng = LatLng(selectedItem.mapy.toDouble() / 1E7, selectedItem.mapx.toDouble() / 1E7)
            getMarker(latLng.latitude, latLng.longitude)

            // 해당 좌표로 카메라 이동
            val cameraUpdate = CameraUpdate.scrollTo(latLng)
            naverMap.moveCamera(cameraUpdate)

            val selectedTitle = selectedItem.title.replace(Regex("<[^>]*>"), "")
            binding.autoCompleteTextView.setText(selectedTitle)
            // 선택 후 키보드 숨김
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.autoCompleteTextView.windowToken, 0)
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

        naverMap.setOnMapLongClickListener { pointF, latLng ->
            getMarker(latLng.latitude, latLng.longitude)
        }
    }

    // 마커 생성
    private fun getMarker(latitude: Double, longitude: Double) {
        marker.position = LatLng(latitude, longitude)
        marker.map = naverMap

        getAddress(latitude, longitude)
    }

    // Geocoder로 주소 가져오기
    private fun getAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(applicationContext, Locale.KOREAN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude, longitude, 1
            ) { address ->
                if (address.size != 0) {
                    toast(address[0].getAddressLine(0))
                }
            }
        } else {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                toast(addresses[0].getAddressLine(0))
            }
        }
    }

    // Toast Message
    private fun toast(text: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}