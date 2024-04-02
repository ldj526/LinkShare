package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    private val linkViewModel: LinkViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinkBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linkAdapter = LinkRVAdapter(linkList)
        binding.rvMemo.adapter = linkAdapter
        binding.rvMemo.layoutManager = LinearLayoutManager(context)

        val uid = FBAuth.getUid()

        linkViewModel.getUserWrittenAndSharedData(uid).observe(viewLifecycleOwner) { memos ->
            linkAdapter.setLinkData(memos)
            binding.rvMemo.scrollToPosition(0)
        }

        binding.fabNewMemo.setOnClickListener {
            val intent = Intent(context, NewLinkActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}