package com.example.linkshare.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.BoardRepository
import com.example.linkshare.board.BoardViewModel
import com.example.linkshare.board.BoardViewModelFactory
import com.example.linkshare.databinding.FragmentCategoryBinding
import com.example.linkshare.link.Link

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var category: String
    private val linkList = mutableListOf<Link>()
    private lateinit var boardRVAdapter: BoardRVAdapter
    private lateinit var boardViewModel: BoardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString("category", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        val boardRepository = BoardRepository()
        val boardFactory = BoardViewModelFactory(boardRepository)
        boardViewModel = ViewModelProvider(this, boardFactory)[BoardViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        loadCategoryData()
        return binding.root
    }

    // Recyclerview Setting
    private fun setupRecyclerView() {
        boardRVAdapter = BoardRVAdapter(linkList, boardViewModel)
        binding.rvCategory.adapter = boardRVAdapter
        binding.rvCategory.layoutManager = LinearLayoutManager(context)
    }

    // Observe ViewModel
    private fun observeViewModel() {
        boardViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        boardViewModel.categoryResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { links ->
                Log.d("CategoryFragment", "Links loaded successfully: $links")
                boardRVAdapter.setBoardData(links)
                binding.rvCategory.scrollToPosition(0)
            }.onFailure {
                Log.e("CategoryFragment", "Failed to load links", it)
                Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 카테고리에 맞는 데이터 가져오기
    private fun loadCategoryData() {
        boardViewModel.getEqualCategoryLinkList(category, 0, 3)
    }

    companion object {
        @JvmStatic
        fun newInstance(category: String): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putString("category", category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}