package com.example.linkshare.util

import com.google.firebase.Firebase
import com.google.firebase.database.database

class FBRef {
    companion object {
        private val database = Firebase.database

        val memoCategory = database.getReference("memoList")
    }
}