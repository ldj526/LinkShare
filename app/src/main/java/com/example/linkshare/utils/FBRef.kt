package com.example.linkshare.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {

    companion object {
        private val database = Firebase.database

        val memoList = database.getReference("memoList")
        val boardList = database.getReference("boardList")
        val commentList = database.getReference("commentList")
    }
}