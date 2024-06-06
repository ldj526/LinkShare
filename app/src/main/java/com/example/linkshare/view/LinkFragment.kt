package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.databinding.FragmentLinkBinding
import com.example.linkshare.link.Link
import com.example.linkshare.link.LinkRVAdapter
import com.example.linkshare.link.LinkRepository
import com.example.linkshare.link.LinkViewModel
import com.example.linkshare.link.LinkViewModelFactory
import com.example.linkshare.link.NewLinkActivity
import com.example.linkshare.util.FBAuth

class LinkFragment : Fragment() {

    private var _binding: FragmentLinkBinding? = null
    private val binding get() = _binding!!
    private val linkList = mutableListOf<Link>()
    private lateinit var linkAdapter: LinkRVAdapter
    private lateinit var linkViewModel: LinkViewModel

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

        val linkRepository = LinkRepository()
        val linkFactory = LinkViewModelFactory(linkRepository)
        linkViewModel = ViewModelProvider(this, linkFactory)[LinkViewModel::class.java]

        linkViewModel.userLinks.observe(viewLifecycleOwner) { result ->
            result.onSuccess { memos ->
                linkAdapter.setLinkData(memos)
                binding.rvMemo.scrollToPosition(0)
            }.onFailure {
                Toast.makeText(context, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }

        linkViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvMemo.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.rvMemo.visibility = View.VISIBLE
            }
        }

        binding.fabNewMemo.setOnClickListener {
            val intent = Intent(context, NewLinkActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val uid = FBAuth.getUid()
        linkViewModel.getUserWrittenAndSharedData(uid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}