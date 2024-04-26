package com.example.linkshare.category

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.BoardViewModel
import com.example.linkshare.databinding.ActivitySeeMoreBinding
import com.example.linkshare.link.Link

class SeeMoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeMoreBinding
    private var checkedItem = -1
    private val linkList = mutableListOf<Link>()
    private lateinit var boardRVAdapter: BoardRVAdapter
    private val boardViewModel: BoardViewModel by viewModels()
    private var isLoading = false
    private var totalItemCount = 0 // 현재 데이터 총 개수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeeMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val category = intent.getStringExtra("category")?: ""
        Log.d("SeeMoreActivity", "category: $category")
        setupRecyclerView()
        loadLinkList(category)

        binding.tvSort.setOnClickListener {
            showSortDialog(category)
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
        boardViewModel.getEqualCategoryLinkList(category, checkedItem, 5).observe(this) { links ->
            totalItemCount += links.size
            boardRVAdapter.setBoardData(links)
            binding.rvMore.scrollToPosition(0)
            showLoading(false)
        }
    }

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
        isLoading = true
        showLoading(true)
        val category = intent.getStringExtra("category") ?: ""
        boardViewModel.getEqualCategoryLinkList(category, checkedItem, totalItemCount + 5).observe(this) { links ->
            if (links.isNotEmpty()) {
                boardRVAdapter.setBoardData(links)
                isLoading = false
                showLoading(false)
            }
        }
    }

    private fun showLoading(isVisible: Boolean) {
        binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}