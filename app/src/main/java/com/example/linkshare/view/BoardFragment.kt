package com.example.linkshare.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.R
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.BoardViewModel
import com.example.linkshare.category.CategoryActivity
import com.example.linkshare.category.CategoryViewModel
import com.example.linkshare.databinding.FragmentBoardBinding
import com.example.linkshare.link.Link

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private val linkList = mutableListOf<Link>()
    private var checkedItem = -1
    private lateinit var boardRVAdapter: BoardRVAdapter
    private val categoryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedCategory = result.data?.getStringExtra("category")
            categoryViewModel.selectCategory(selectedCategory ?: "전체보기")
        }
    }
    private val boardViewModel: BoardViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryViewModel.getSelectedCategory().observe(viewLifecycleOwner) { category ->
            loadLinkList(category)
        }

        loadLinkList("전체보기")

        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvBoard.adapter = boardRVAdapter
        binding.rvBoard.layoutManager = LinearLayoutManager(context)

        binding.btnCategory.setOnClickListener {
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            categoryResultLauncher.launch(intent)
        }

        binding.tvSort.setOnClickListener {
            showSortDialog()
        }
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
                categoryViewModel.getSelectedCategory().value?.let { category ->
                    loadLinkList(category)
                }
                dialog.dismiss()
            }
            .show()
    }

    // LinkList 가져오기
    private fun loadLinkList(category: String) {
        boardViewModel.getEqualCategoryLinkList(category, checkedItem)
            .observe(viewLifecycleOwner) { links ->
                boardRVAdapter.setBoardData(links)
                binding.rvBoard.scrollToPosition(0)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}