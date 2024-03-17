package com.example.linkshare.util

import com.google.firebase.Firebase
import com.google.firebase.database.database

class FBRef {
    companion object {
        private val database = Firebase.database

        val linkCategory = database.getReference("linkList")
        val sharedLinkCategory = database.getReference("sharedLinkList")
        val commentCategory = database.getReference("commentList")
    }
}