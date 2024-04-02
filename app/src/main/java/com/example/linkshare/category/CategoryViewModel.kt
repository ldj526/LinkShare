package com.example.linkshare.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel: ViewModel() {
    private val selectedCategory = MutableLiveData<String>("전체보기")  // 초기 상태를 전체목록으로 하기 위함.

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun getSelectedCategory(): LiveData<String> = selectedCategory
}