package com.example.linkshare.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.linkshare.R
import com.example.linkshare.category.CategoryActivity
import com.example.linkshare.category.CategoryViewModel
import com.example.linkshare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var backKeyPressedTime = 0L
    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedCategory = result.data?.getStringExtra("category")
                categoryViewModel.selectCategory(selectedCategory ?: "")
            }
        }

        // BottomNavigation 연결
        val bottomNavigationView = binding.bottomNavigationView
        val navController = findNavController(R.id.fragment_container_view)
        bottomNavigationView.setupWithNavController(navController)

        binding.btnSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.btnCategory.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            categoryResultLauncher.launch(intent)
        }

        // 뒤로가기 2번 클릭 시 앱 종료
        onBackPressedDispatcher.addCallback {
            if (System.currentTimeMillis() - backKeyPressedTime >= BACK_PRESSED_DURATION) {
                backKeyPressedTime = System.currentTimeMillis()
                Toast.makeText(applicationContext, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else {
                finishAffinity()
            }
        }
    }
    companion object {
        private const val BACK_PRESSED_DURATION = 2000L
    }
}