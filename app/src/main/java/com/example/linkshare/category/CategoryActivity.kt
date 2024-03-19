package com.example.linkshare.category

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityCategoryBinding
import com.google.android.flexbox.FlexboxLayout

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var currentSelectedCategories: ArrayList<String>
    private val selectedCategories = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSelectedCategories = intent.getStringArrayListExtra("currentSelectedCategories") ?: arrayListOf()

        val categories = resources.getStringArray(R.array.category)

        categories.forEach { category ->
            val textView = TextView(this).apply {
                text = category
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(5, 5, 5, 5)
                }
                // 현재 선택된 카테고리인지 확인하여 UI 업데이트
                setBackgroundResource(
                    if (currentSelectedCategories.contains(category)) R.drawable.category_selected_background
                    else R.drawable.category_unselected_background
                )

                setOnClickListener {
                    toggleCategorySelection(category, this)
                }
            }
            binding.selectCategory.addView(textView)
            // 초기 선택 상태를 selectedCategories에도 반영
            if (currentSelectedCategories.contains(category)) {
                selectedCategories[category] = textView
                addCategoryToBottomView(category) // 초기 선택된 카테고리 하단에 추가
            }
        }

        binding.btnSelect.setOnClickListener {
            val intent = Intent().apply {
                putStringArrayListExtra("selectedCategories", ArrayList(selectedCategories.keys))
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun toggleCategorySelection(category: String, textView: TextView) {
        if (selectedCategories.containsKey(category)) {
            textView.setBackgroundResource(R.drawable.category_unselected_background)
            selectedCategories.remove(category)
            removeCategoryFromBottomView(category) // 하단 뷰에서 제거
        } else {
            textView.setBackgroundResource(R.drawable.category_selected_background)
            selectedCategories[category] = textView
            addCategoryToBottomView(category) // 하단 뷰에 추가
        }
    }

    private fun addCategoryToBottomView(category: String) {
        val textView = TextView(this).apply {
            text = category
            layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5, 5, 5, 5)
            }
            setBackgroundResource(R.drawable.category_selected_background)
        }
        binding.selectedCategory.addView(textView)
    }

    private fun removeCategoryFromBottomView(category: String) {
        for (i in 0 until binding.selectedCategory.childCount) {
            val view = binding.selectedCategory.getChildAt(i)
            if (view is TextView && view.text == category) {
                binding.selectedCategory.removeViewAt(i)
                break
            }
        }
    }
}