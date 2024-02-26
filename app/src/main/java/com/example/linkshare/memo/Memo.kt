package com.example.linkshare.memo

data class Memo(
    val title: String = "",
    val content: String = "",
    val link: String = "",
    val location: String? = "",
    val uid: String = "",
    val time: String = ""
)
