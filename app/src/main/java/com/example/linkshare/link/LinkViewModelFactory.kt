package com.example.linkshare.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LinkViewModelFactory(private val linkRepository: LinkRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LinkViewModel::class.java) -> {
                LinkViewModel(linkRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}