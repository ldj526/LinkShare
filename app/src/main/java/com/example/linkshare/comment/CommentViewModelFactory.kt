package com.example.linkshare.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CommentViewModelFactory(private val commentRepository: CommentRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
            return CommentViewModel(commentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}