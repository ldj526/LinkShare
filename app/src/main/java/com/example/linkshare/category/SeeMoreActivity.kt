package com.example.linkshare.category

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
        boardViewModel.getEqualCategoryLinkList(category, checkedItem, 10).observe(this) { links ->
                boardRVAdapter.setBoardData(links)
                binding.rvMore.scrollToPosition(0)
            }
    }

    private fun setupRecyclerView() {
        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvMore.adapter = boardRVAdapter
        binding.rvMore.layoutManager = LinearLayoutManager(this)
    }
}