package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.databinding.FragmentLinkBinding
import com.example.linkshare.link.Link
import com.example.linkshare.link.LinkRVAdapter
import com.example.linkshare.link.LinkViewModel
import com.example.linkshare.link.NewLinkActivity
import com.example.linkshare.util.FBAuth

class LinkFragment : Fragment() {

    private var _binding: FragmentLinkBinding? = null
    private val binding get() = _binding!!
    private val linkList = mutableListOf<Link>()
    private lateinit var linkAdapter: LinkRVAdapter
    private val linkViewModel by lazy { ViewModelProvider(this)[LinkViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinkBinding.inflate(inflater, container, false)

        linkAdapter = LinkRVAdapter(linkList)
        binding.rvMemo.adapter = linkAdapter
        binding.rvMemo.layoutManager = LinearLayoutManager(context)

        val uid = FBAuth.getUid()

        linkViewModel.getUserWrittenAndSharedData(uid).observe(viewLifecycleOwner) { memos ->
            linkAdapter.setLinkData(memos)
        }

        binding.fabNewMemo.setOnClickListener {
            val intent = Intent(context, NewLinkActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}