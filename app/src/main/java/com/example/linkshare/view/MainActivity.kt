package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var backKeyPressedTime = 0L
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BottomNavigation 연결
        val bottomNavigationView = binding.bottomNavigationView
        navController = findNavController(R.id.fragment_container_view)
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

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        intent.extras?.let {
            when {
                it.getBoolean("navigateToSettingFragment", false) -> {
                    navController.navigate(R.id.setting_fragment)
                    clearIntentExtras(intent)
                }
                it.getBoolean("navigateToLinkFragment", false) -> {
                    navController.navigate(R.id.link_fragment)
                    clearIntentExtras(intent)
                }
            }
        }
    }

    private fun clearIntentExtras(intent: Intent) {
        Log.d("MainActivity", "intent 값: $intent")
        intent.removeExtra("navigateToSettingFragment")
        intent.removeExtra("navigateToLinkFragment")
        Log.d("MainActivity", "intent 값: $intent")
        // Optionally clear other extras if needed
        setIntent(Intent()) // Clear the intent to avoid reusing the extras
        Log.d("MainActivity", "intent 값: $intent")
    }
    companion object {
        private const val BACK_PRESSED_DURATION = 2000L
    }
}