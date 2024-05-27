package com.example.linkshare.category

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityCategorySelectBinding
import com.google.android.material.chip.Chip

class CategorySelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategorySelectBinding
    private lateinit var currentSelectedCategories: ArrayList<String>
    private val selectedCategories = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategorySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSelectedCategories = intent.getStringArrayListExtra("currentSelectedCategories") ?: arrayListOf()

        val categories = resources.getStringArray(R.array.category)

        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = currentSelectedCategories.contains(category)
                chipIcon = if (isChecked) getCheckIcon() else getPlusIcon()
                checkedIcon = getCheckIcon()
                setOnCheckedChangeListener { buttonView, isChecked ->
                    toggleCategorySelection(category, buttonView as Chip, isChecked)
                }
            }
            binding.selectCategory.addView(chip)

            if (currentSelectedCategories.contains(category)) {
                selectedCategories[category] = chip
                addCategoryToBottomView(category)
            }
        }

        binding.btnSelect.setOnClickListener {
            val intent = Intent().apply {
                putStringArrayListExtra("selectedCategories", ArrayList(selectedCategories.keys))
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun getPlusIcon(): Drawable? {
        return resources.getDrawable(android.R.drawable.checkbox_off_background, null)
    }

    private fun getCheckIcon(): Drawable? {
        return resources.getDrawable(android.R.drawable.checkbox_on_background, null)
    }



    // chip의 선택 여부에 따른 list 추가/제거
    private fun toggleCategorySelection(category: String, chip: Chip, isChecked: Boolean) {
        chip.chipIcon = if (isChecked) getCheckIcon() else getPlusIcon()
        if (isChecked) {
            selectedCategories[category] = chip
            addCategoryToBottomView(category)
        } else {
            selectedCategories.remove(category)
            removeCategoryFromBottomView(category)
        }
    }

    // 선택된 항목들 표시해주는 기능
    private fun addCategoryToBottomView(category: String) {
        val chip = Chip(this).apply {
            text = category
            isCheckable = false
        }
        binding.selectedCategory.addView(chip)
    }

    // 해제한 항목들 없애는 기능
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