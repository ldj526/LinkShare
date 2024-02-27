package com.example.linkshare.board

data class Board(
    val title: String = "",
    val content: String = "",
    val link: String = "",
    val location: String? = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val uid: String = "",
    val time: String = ""
)
