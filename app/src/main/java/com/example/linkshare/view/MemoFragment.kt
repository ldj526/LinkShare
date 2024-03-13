package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.databinding.FragmentMemoBinding
import com.example.linkshare.memo.Memo
import com.example.linkshare.memo.MemoRVAdapter
import com.example.linkshare.memo.MemoViewModel
import com.example.linkshare.memo.NewMemoActivity
import com.example.linkshare.util.FBAuth

class MemoFragment : Fragment() {

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!
    private val memoList = mutableListOf<Memo>()
    private lateinit var memoAdapter: MemoRVAdapter
    private val memoViewModel by lazy { ViewModelProvider(this)[MemoViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoBinding.inflate(inflater, container, false)

        memoAdapter = MemoRVAdapter(memoList)
        binding.rvMemo.adapter = memoAdapter
        binding.rvMemo.layoutManager = LinearLayoutManager(context)

        val uid = FBAuth.getUid()
        memoViewModel.getUserWrittenAndSharedData(uid)

        memoViewModel.userWrittenAndSharedData.observe(viewLifecycleOwner) { memos ->
            memoAdapter.setMemoData(memos)
        }

        binding.fabNewMemo.setOnClickListener {
            val intent = Intent(context, NewMemoActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}