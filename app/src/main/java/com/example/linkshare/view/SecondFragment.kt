package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.linkshare.board.BoardWriteActivity
import com.example.linkshare.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        binding.fbAdd.setOnClickListener {
            val intent = Intent(context, BoardWriteActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}