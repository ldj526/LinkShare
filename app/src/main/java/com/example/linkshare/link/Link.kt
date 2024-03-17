package com.example.linkshare.link

data class Link(
    val key: String = "",
    val title: String = "",
    val content: String = "",
    val link: String = "",
    val location: String? = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val uid: String = "",
    val time: String = "",
    val category: String = "",
    var shareCount: Int = 0,
    val shareUid: String? = null,
    val imageUrl: String? = null
)
