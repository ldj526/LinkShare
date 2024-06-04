package com.example.linkshare.board

import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val boardRepository = BoardRepository()
        val boardFactory = BoardViewModelFactory(boardRepository)
        moreBoardViewModel = ViewModelProvider(this, boardFactory)[MoreBoardViewModel::class.java]

        observeViewModel()

        val category = intent.getStringExtra("category")?: ""
        Log.d("SeeMoreActivity", "category: $category")
        setupRecyclerView()
        loadLinkList(category)

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
                binding.rvMore.scrollToPosition(0)
            }.onFailure {
                Toast.makeText(this, "카테고리 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }

        moreBoardViewModel.moreDataResult.observe(this) { result ->
            result.onSuccess { links ->
                boardRVAdapter.addBoardData(links)
                totalItemCount += links.size
                isLoading = false
            }.onFailure {
                Toast.makeText(this, "더 많은 데이터 로드 실패", Toast.LENGTH_SHORT).show()
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

        moreBoardViewModel.moreDataLoading.observe(this) { moreDataLoading ->
            if (moreDataLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        moreBoardViewModel.noMoreData.observe(this) { noMoreData ->
            if (noMoreData) {
                Toast.makeText(this, "더 이상 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
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
                loadLinkList(category)
                dialog.dismiss()
            }
            .show()
    }

    // LinkList 가져오기
    private fun loadLinkList(category: String) {
        moreBoardViewModel.getEqualCategoryLinkList(category, checkedItem, 30)
    }

    // RecyclerView setup
    private fun setupRecyclerView() {
        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvMore.adapter = boardRVAdapter
        binding.rvMore.layoutManager = LinearLayoutManager(this)

        // 스크롤 리스너 추가
        binding.rvMore.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && totalItemCount <= (firstVisibleItemPosition + visibleItemCount)) {
                    // 데이터 더 로드
                    loadMoreData()
                }
            }
        })
    }

    // 추가 데이터 로드
    private fun loadMoreData() {
        val category = intent.getStringExtra("category") ?: ""
        if (!isLoading) {
            isLoading = true
            moreBoardViewModel.loadMoreData(category, checkedItem, totalItemCount, 5)
        }
    }
}