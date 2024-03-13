package com.example.linkshare.memo

data class Memo(
    val key: String? = null,
    val title: String = "",
    val content: String = "",
    val link: String = "",
    val location: String? = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val uid: String = "",
    val time: String = "",
    val shareUid: String? = null
)
