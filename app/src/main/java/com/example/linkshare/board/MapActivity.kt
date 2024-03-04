package com.example.linkshare.board

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMapBinding
import com.google.firebase.annotations.concurrent.UiThread
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var naverMap: NaverMap
    private val marker = Marker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sfm = supportFragmentManager
        val mapFragment = sfm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                sfm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        naverMap.uiSettings.isZoomControlEnabled = false
        naverMap.uiSettings.isLocationButtonEnabled = false

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
                binding.include.btnSelect.visibility = View.GONE
            }
        }
    }

    // 마커 생성
    private fun getMarker(latitude: Double, longitude: Double) {
        marker.position = LatLng(latitude, longitude)
        marker.map = naverMap
    }
}