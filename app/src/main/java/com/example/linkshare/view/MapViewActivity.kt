package com.example.linkshare.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMapViewBinding
import com.google.firebase.annotations.concurrent.UiThread
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapViewBinding

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
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
    }
}