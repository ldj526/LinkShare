package com.example.linkshare.board

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMoreBoardBinding
import com.example.linkshare.link.Link

class MoreBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoreBoardBinding
    private var checkedItem = -1
    private val linkList = mutableListOf<Link>()
    private lateinit var boardRVAdapter: BoardRVAdapter
    private lateinit var moreBoardViewModel: MoreBoardViewModel
    private var totalItemCount = 0 // 현재 데이터 총 개수
    private var isLoading = false
    private var isEndOfData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val boardRepository = BoardRepository()
        val boardFactory = BoardViewModelFactory(boardRepository)
        moreBoardViewModel = ViewModelProvider(this, boardFactory)[MoreBoardViewModel::class.java]

        val category = intent.getStringExtra("category")?: ""
        setupRecyclerView(category)
        setupSwipeRefreshLayout(category)
        observeViewModel()
        loadLinkList(category, initialLoad = true)

        binding.tvSort.setOnClickListener {
            showSortDialog(category)
        }
    }

    // Observe ViewModel
    private fun observeViewModel() {
        moreBoardViewModel.categoryResult.observe(this) { result ->
            result.onSuccess { links ->
                totalItemCount = links.size
                boardRVAdapter.setBoardData(links)
                binding.swipeRefreshLayout.isRefreshing = false
                isLoading = false
            }.onFailure {
                Toast.makeText(this, "카테고리 로드 실패", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
                isLoading = false
            }
        }

        moreBoardViewModel.loading.observe(this) { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        moreBoardViewModel.moreLoading.observe(this) { loading ->
            isLoading = loading
            if (loading) {
                binding.pbMoreData.visibility = View.VISIBLE
            } else {
                binding.pbMoreData.visibility = View.GONE
            }
        }

        moreBoardViewModel.isEndOfData.observe(this) { endOfData ->
            isEndOfData = endOfData
        }

        moreBoardViewModel.initialLoadComplete.observe(this) { initialLoadComplete ->
            if (initialLoadComplete) {
                binding.rvMore.scrollToPosition(0)
                moreBoardViewModel.resetPage()
            }
        }
    }

    // 정렬을 위한 Dialog
    private fun showSortDialog(category: String) {
        val sortOptions = resources.getStringArray(R.array.sort)
        if (checkedItem == -1) checkedItem = 0
        AlertDialog.Builder(this)
            .setTitle("정렬 선택")
            .setSingleChoiceItems(sortOptions, checkedItem) { dialog, which ->
                binding.tvSort.text = sortOptions[which]
                checkedItem = which
                loadLinkList(category, initialLoad = true)
                dialog.dismiss()
            }
            .show()
    }

    // LinkList 가져오기
    private fun loadLinkList(category: String, initialLoad: Boolean) {
        moreBoardViewModel.resetPage()
        moreBoardViewModel.getEqualCategoryLinkList(category, checkedItem, initialLoad)
    }

    // RecyclerView setup
    private fun setupRecyclerView(category: String) {
        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvMore.adapter = boardRVAdapter
        binding.rvMore.layoutManager = LinearLayoutManager(this)

        // 스크롤 리스너 추가
        binding.rvMore.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoading && !isEndOfData && lastVisibleItemPosition >= totalItemCount - 5 && dy > 0) {
                    moreBoardViewModel.loadMoreLinkList(category, checkedItem)
                    isLoading = true
                }
            }
        })
    }

    // SwipeRefreshLayout setup
    private fun setupSwipeRefreshLayout(category: String) {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadLinkList(category, initialLoad = false)  // 기본 카테고리 또는 사용자가 선택한 카테고리로 새로고침
        }
    }
}