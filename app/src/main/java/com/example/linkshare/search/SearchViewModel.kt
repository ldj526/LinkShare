package com.example.linkshare.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.link.Link
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {
    private val _latestSearchQueries = MutableLiveData<List<SearchQuery>>()
    val latestSearchQueries: LiveData<List<SearchQuery>> get() = _latestSearchQueries

    private val _searchResult = MutableLiveData<Result<MutableList<Link>>>()
    val searchResult: LiveData<Result<MutableList<Link>>> get() = _searchResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _queries = mutableListOf<SearchQuery>()
    private val delayTime = 500L

    // 검색된 링크 가져오기
    fun getSearchedLinks(searchText: String, searchOption: String) {
        viewModelScope.launch {
            val job = launch {// 빠르게 처리되면 progressBar 안나타나게 하기 위함
                delay(delayTime)
                _loading.value = true
            }
            val result = repository.searchLinks(searchText, searchOption)
            _searchResult.value = result
            job.cancel()
            _loading.value = false
        }
    }

    fun saveSearchQuery(query: String) {
        viewModelScope.launch {
            val searchQuery = SearchQuery(query)
            val result = repository.saveSearchQuery(query)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            } else {
                _queries.add(0, searchQuery)
                _latestSearchQueries.value = _queries.toList()
            }
        }
    }

    fun deleteSearchQuery(searchQuery: SearchQuery) {
        viewModelScope.launch {
            val result = repository.deleteSearchQuery(searchQuery)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            } else {
                _queries.remove(searchQuery)
                _latestSearchQueries.value = _queries.toList()
            }
        }
    }

    fun fetchLatestSearchQueries() {
        viewModelScope.launch {
            val result = repository.getLatestSearchQueries()
            if (result.isSuccess) {
                _queries.clear()
                _queries.addAll(result.getOrNull() ?: emptyList())
                _latestSearchQueries.value = _queries.toList()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}