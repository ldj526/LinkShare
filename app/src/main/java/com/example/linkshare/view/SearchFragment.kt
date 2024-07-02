package com.example.linkshare.view

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.R
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.databinding.FragmentSearchBinding
import com.example.linkshare.link.Link
import com.example.linkshare.search.PopularSearchAdapter
import com.example.linkshare.search.SearchQuery
import com.example.linkshare.search.SearchRepository
import com.example.linkshare.search.SearchViewModel
import com.example.linkshare.search.SearchViewModelFactory
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var boardRVAdapter: BoardRVAdapter
    private lateinit var popularSearchAdapter: PopularSearchAdapter
    private var selectedSearchItem: String? = null
    private var searchText = ""
    private val linkList = mutableListOf<Link>()
    private var checkedItem = -1
    private lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        checkLoginAndInitialize()
        observeViewModel()
        setupSpinner()
        setupSearchView()
        binding.tvSort.setOnClickListener {
            showSortDialog()
        }
    }

    private fun setupSearchView() {
        binding.searchView.isSubmitButtonEnabled = true
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchText = query ?: ""
                if (searchText.isEmpty()) {
                    Toast.makeText(requireContext(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    searchViewModel.saveSearchQuery(searchText)
                    performSearch(searchText)
                    binding.searchView.clearFocus()
                    binding.recentPopularLayout.visibility = View.GONE
                    binding.linkListLayout.visibility = View.VISIBLE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 실시간으로 검색을 원하면 여기에 검색 로직을 추가
                if (newText.isNullOrEmpty()) {
                    binding.recentPopularLayout.visibility = View.VISIBLE
                    binding.linkListLayout.visibility = View.GONE
                    searchViewModel.fetchPopularSearchQueries()
                    searchViewModel.fetchLatestSearchQueries()
                }
                return false
            }
        })
    }

    private fun setupSpinner() {
        val searchItems = resources.getStringArray(R.array.search_list)
        val searchAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, searchItems)
        binding.spinnerSearch.adapter = searchAdapter

        binding.spinnerSearch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSearchItem = searchItems[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvSearchFragment.adapter = boardRVAdapter
        binding.rvSearchFragment.layoutManager = LinearLayoutManager(context)

        popularSearchAdapter= PopularSearchAdapter()
        binding.rvPopularSearch.adapter = popularSearchAdapter
        binding.rvPopularSearch.layoutManager = LinearLayoutManager(context)
    }

    private fun checkLoginAndInitialize() {
        val userUid = getCurrentUserUid()
        if (userUid != null) {
            initializeViewModels(userUid)
        }
    }

    private fun initializeViewModels(userEmail: String) {
        val firestore = FirebaseFirestore.getInstance()
        val repository = SearchRepository(userEmail, firestore)
        searchViewModel = ViewModelProvider(this, SearchViewModelFactory(repository))[SearchViewModel::class.java]

        searchViewModel.fetchLatestSearchQueries()
        searchViewModel.fetchPopularSearchQueries()
    }

    private fun observeViewModel() {
        searchViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        searchViewModel.searchResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { links ->
                sortAndDisplayLinks(links)
            }.onFailure {
                Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_SHORT).show()
            }
        }

        searchViewModel.latestSearchQueries.observe(viewLifecycleOwner) { queries ->
            setupChips(queries)
        }

        searchViewModel.popularSearchQueries.observe(viewLifecycleOwner) { queries ->
            popularSearchAdapter.updateItems(queries)
        }
    }

    // chipGroup setting
    private fun setupChips(queries: List<SearchQuery>) {
        binding.chipGroup.removeAllViews()
        queries.forEach { query ->
            val chip = Chip(requireContext()).apply {
                text = query.query
                textSize = 12f
                isCloseIconVisible = true
                typeface = Typeface.DEFAULT_BOLD
                isClickable = true
                id = View.generateViewId()
                setOnClickListener {
                    binding.searchView.setQuery(query.query, false)
                    performSearch(query.query)
                    binding.searchView.clearFocus()
                    binding.recentPopularLayout.visibility = View.GONE
                    binding.linkListLayout.visibility = View.VISIBLE
                }
                setOnCloseIconClickListener {
                    searchViewModel.deleteSearchQuery(query.query)
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    // uid 가져오기
    private fun getCurrentUserUid(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid
    }

    // 검색어에 따라 list 업데이트
    private fun performSearch(searchText: String) {
        searchViewModel.getSearchedLinks(searchText, selectedSearchItem.toString())
    }

    private fun sortAndDisplayLinks(links: MutableList<Link>) {
        val sortedLinks = when (checkedItem) {
            0 -> links.sortedByDescending { it.time } // 최신순 정렬
            1 -> links.sortedWith(compareByDescending<Link> { it.shareCount }.thenByDescending { it.time }) // 공유 많은 순 정렬
            else -> links
        }.toMutableList()
        boardRVAdapter.setBoardData(sortedLinks)
        binding.rvSearchFragment.scrollToPosition(0)
    }

    // 정렬을 위한 Dialog
    private fun showSortDialog() {
        val sortOptions = resources.getStringArray(R.array.sort)
        if (checkedItem == -1) checkedItem = 0
        AlertDialog.Builder(requireActivity())
            .setTitle("정렬 선택")
            .setSingleChoiceItems(sortOptions, checkedItem) { dialog, which ->
                binding.tvSort.text = sortOptions[which]
                checkedItem = which
                performSearch(searchText)
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}