package com.example.linkshare.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMapViewBinding
import com.google.firebase.annotations.concurrent.UiThread
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapViewBinding
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sfm = supportFragmentManager
        val mapFragment = sfm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                sfm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
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
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}