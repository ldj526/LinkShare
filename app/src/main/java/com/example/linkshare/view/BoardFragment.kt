package com.example.linkshare.view

import android.app.Activity
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
    private lateinit var boardRVAdapter: BoardRVAdapter
    private val categoryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedCategory = result.data?.getStringExtra("category")
            categoryViewModel.selectCategory(selectedCategory ?: "")
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
            boardViewModel.getEqualCategoryListData(category)
            boardViewModel.categoryList.observe(viewLifecycleOwner) { links ->
                boardRVAdapter.setBoardData(links)
            }
        }

        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvBoard.adapter = boardRVAdapter
        binding.rvBoard.layoutManager = LinearLayoutManager(context)

        boardViewModel.allUserWrittenData.observe(viewLifecycleOwner) { memos ->
            boardRVAdapter.setBoardData(memos)
        }

        binding.btnCategory.setOnClickListener {
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            categoryResultLauncher.launch(intent)
        }

        binding.btnAll.setOnClickListener {
            boardViewModel.allUserWrittenData.value?.let { memos ->
                boardRVAdapter.setBoardData(memos)
            }
        }

        binding.btnMy.setOnClickListener {
            boardViewModel.userWrittenData.value?.let { memos ->
                boardRVAdapter.setBoardData(memos)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}