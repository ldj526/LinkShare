package com.example.linkshare.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BoardViewModelFactory(private val boardRepository: BoardRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(BoardViewModel::class.java) -> {
                BoardViewModel(boardRepository) as T
            }
            modelClass.isAssignableFrom(MoreBoardViewModel::class.java) -> {
                MoreBoardViewModel(boardRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}