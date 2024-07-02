package com.example.linkshare.search

data class SearchQuery(
    val query: String = "",
    val timestamp: Long = System.currentTimeMillis()
)