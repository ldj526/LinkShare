package com.example.linkshare.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.R
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.BoardRepository
import com.example.linkshare.board.BoardViewModel
import com.example.linkshare.board.BoardViewModelFactory
import com.example.linkshare.databinding.FragmentSearchBinding
import com.example.linkshare.link.Link

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var boardRVAdapter: BoardRVAdapter
    private var selectedSearchItem: String? = null
    private var searchText = ""
    private val linkList = mutableListOf<Link>()
    private var checkedItem = -1
    private lateinit var boardViewModel: BoardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvSearchFragment.adapter = boardRVAdapter
        binding.rvSearchFragment.layoutManager = LinearLayoutManager(context)

        val boardRepository = BoardRepository()
        val boardFactory = BoardViewModelFactory(boardRepository)
        boardViewModel = ViewModelProvider(this, boardFactory)[BoardViewModel::class.java]

        observeViewModel()

        val searchItems = resources.getStringArray(R.array.search_list)
        val searchAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, searchItems)
        binding.spinnerSearch.adapter = searchAdapter

        binding.spinnerSearch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSearchItem = searchItems[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.searchView.isSubmitButtonEnabled = true
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchText = query ?: ""
                if (searchText.isEmpty()) {
                    Toast.makeText(requireActivity(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    performSearch(searchText)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 실시간으로 검색을 원하면 여기에 검색 로직을 추가
                return false
            }
        })

        binding.tvSort.setOnClickListener {
            showSortDialog()
        }
    }

    private fun observeViewModel() {
        boardViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        boardViewModel.searchResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { links ->
                sortAndDisplayLinks(links)
            }.onFailure {
                Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 검색어에 따라 list 업데이트
    private fun performSearch(searchText: String) {
        boardViewModel.getSearchedLinks(searchText, selectedSearchItem.toString())
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