package com.example.linkshare.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var backKeyPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BottomNavigation 연결
        val bottomNavigationView = binding.bottomNavigationView
        val navController = findNavController(R.id.fragment_container_view)
        bottomNavigationView.setupWithNavController(navController)

        // 뒤로가기 2번 클릭 시 앱 종료
        onBackPressedDispatcher.addCallback {
            if (System.currentTimeMillis() - backKeyPressedTime >= BACK_PRESSED_DURATION) {
                backKeyPressedTime = System.currentTimeMillis()
                Toast.makeText(applicationContext, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else {
                finishAffinity()
            }
        }

        // SettingFragment에서 Activity이동 후 Activity 종료 시 SettingFragment로 View 이동
        if (intent.getBooleanExtra("navigateToSettingFragment", false)) {
            findNavController(R.id.fragment_container_view).navigate(R.id.setting_fragment)
        }

        // SettingFragment에서 Activity이동 후 Activity 종료 시 LinkFragment로 View 이동
        if (intent.getBooleanExtra("navigateToLinkFragment", false)) {
            findNavController(R.id.fragment_container_view).navigate(R.id.link_fragment)
        }
    }
    companion object {
        private const val BACK_PRESSED_DURATION = 2000L
    }
}