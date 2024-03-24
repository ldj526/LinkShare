package com.example.linkshare.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel: ViewModel() {
    private val selectedCategory = MutableLiveData<String>()

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun getSelectedCategory(): LiveData<String> = selectedCategory
}