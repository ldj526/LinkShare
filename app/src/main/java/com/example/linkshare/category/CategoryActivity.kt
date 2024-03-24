package com.example.linkshare.category

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityCategoryBinding
import com.google.android.flexbox.FlexboxLayout

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val halfScreenWidth = screenWidth / 2

        val categories = resources.getStringArray(R.array.category)

        categories.forEachIndexed { idx, category ->
            val textView = TextView(this).apply {
                text = category
                layoutParams = FlexboxLayout.LayoutParams(
                    halfScreenWidth, FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(40, 40, 40, 40)
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
                // 현재 선택된 카테고리인지 확인하여 UI 업데이트
                if (idx % 2 == 0) {
                    setBackgroundResource(R.drawable.category_background)
                }
            }
            textView.setOnClickListener {
                val intent = Intent().apply {
                    putExtra("category", textView.text.toString())
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            binding.categoryFlexboxLayout.addView(textView)
        }
    }
}