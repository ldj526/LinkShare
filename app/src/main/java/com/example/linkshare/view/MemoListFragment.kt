package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.linkshare.databinding.FragmentMemolistBinding
import com.example.linkshare.memo.*

class MemoListFragment : Fragment() {

    private var _binding: FragmentMemolistBinding? = null
    private val binding get() = _binding!!
    private val memoDataList = mutableListOf<MemoModel>()
    private val memoKeyList = mutableListOf<String>()
    private lateinit var memoRVAdapter: MemoRVAdapter
    private val viewModel by lazy { ViewModelProvider(this)[MemoViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMemolistBinding.inflate(inflater, container, false)

        // ListView 연결
        memoRVAdapter = MemoRVAdapter(memoDataList)
        binding.memoRV.adapter = memoRVAdapter
        binding.memoRV.layoutManager = GridLayoutManager(context, 2)

        memoRVAdapter.setItemClickListener(object : MemoRVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, MemoUpdateActivity::class.java)
                intent.putExtra("key", getMemoKeyList()[position])
                startActivity(intent)
            }
        })

        binding.fbAdd.setOnClickListener {
            val intent = Intent(context, MemoWriteActivity::class.java)
            startActivity(intent)
        }

        getFBBoardData()

        return binding.root
    }

    // 자신이 쓴 메모만 가져오기
    private fun getFBBoardData() {
        viewModel.getFBMemoData().observe(viewLifecycleOwner, Observer {
            memoRVAdapter.setData(it)
            memoRVAdapter.notifyDataSetChanged()
        })
    }

    // 자신이 쓴 메모의 key 가져오기
    private fun getMemoKeyList(): MutableList<String> {
        return viewModel.getMemoKeyList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}