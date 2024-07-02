package com.example.linkshare.search

data class SearchQuery(
    var query: String = "",
    val count: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)