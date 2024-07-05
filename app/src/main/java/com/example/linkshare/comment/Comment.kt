package com.example.linkshare.comment

import com.example.linkshare.util.FBAuth

data class Comment(
    var id: String? = null,
    val comment: String = "",
    val uid: String = "",
    val time: Long = FBAuth.getTimestamp(),
    val nickname: String? = null
)
