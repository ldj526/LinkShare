package com.example.linkshare.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.BoardViewModel
import com.example.linkshare.databinding.FragmentCategoryBinding
import com.example.linkshare.link.Link
import com.example.linkshare.view.BoardFragment

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var category: String
    private val linkList = mutableListOf<Link>()
    private lateinit var boardRVAdapter: BoardRVAdapter
    private val boardViewModel: BoardViewModel by viewModels()

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
        setupRecyclerView()
        loadCategoryData()
        return binding.root
    }

    // Recyclerview Setting
    private fun setupRecyclerView() {
        boardRVAdapter = BoardRVAdapter(linkList)
        binding.rvCategory.adapter = boardRVAdapter
        binding.rvCategory.layoutManager = LinearLayoutManager(context)
    }

    // 카테고리에 맞는 데이터 가져오기
    private fun loadCategoryData() {
        boardViewModel.getEqualCategoryLinkList(category, 0, 3).observe(viewLifecycleOwner) { links ->
            boardRVAdapter.setBoardData(links)
            binding.rvCategory.scrollToPosition(0)
        }
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