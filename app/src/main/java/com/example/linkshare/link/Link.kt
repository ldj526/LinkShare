package com.example.linkshare.link

import com.example.linkshare.util.FBAuth

data class Link(
    val key: String = "",
    val category: List<String>? = null,
    val title: String = "",
    val content: String = "",
    val link: String = "",
    val location: String? = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val uid: String = "",
    val time: Long = FBAuth.getTimestamp(),
    val firebaseRef: String = "",
    var shareCount: Int = 0,
    val shareUid: String? = null,
    val imageUrl: String? = null,
    var viewCount: Int = 0
)
