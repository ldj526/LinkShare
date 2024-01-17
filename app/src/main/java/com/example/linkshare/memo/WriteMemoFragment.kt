package com.example.linkshare.memo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.linkshare.databinding.FragmentMemoWriteBinding
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class WriteMemoFragment : Fragment() {

    private var _binding: FragmentMemoWriteBinding? = null
    private val binding get() = _binding!!
    private var isEditMode: Boolean = false
    private lateinit var key: String
    private lateinit var writeUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            isEditMode = it.getBoolean("isEditMode", false)
            key = it.getString("key", "")
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoWriteBinding.inflate(inflater, container, false)
        if (isEditMode) {
            binding.btnSave.text = "수정"
        } else {
            binding.btnSave.text = "저장"
        }

        // 저장된 메모를 불러올 경우
        if (key != "") {
            getMemoData(key)
        }

        binding.btnSave.setOnClickListener {
            saveMemo()
            // 해당 Activity 종료
            requireActivity().finish()
        }

        return binding.root
    }

    // Firebase에서 데이터 값 가져오기
    private fun getMemoData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataModel = snapshot.getValue(Memo::class.java)

                binding.etTitle.setText(dataModel!!.title)
                binding.etLink.setText(dataModel.link)
                binding.etContent.setText(dataModel.content)
                writeUid = dataModel.uid
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MemoFragment", "실패")
            }

        }
        FBRef.memoCategory.child(key).addValueEventListener(postListener)
    }

    // 새 메모 / 수정 에 따른 메모 저장
    private fun saveMemo() {
        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()
        val link = binding.etLink.text.toString()
        val time = FBAuth.getTime()
        if (isEditMode) {
            // 수정할 때
            // key값에 맞는 Firebase database 수정
            FBRef.memoCategory.child(key).setValue(Memo(title, content, link, writeUid, time))
        } else {
            // 새 메모를 만들 때
            val uid = FBAuth.getUid()

            // Firebase database에 추가
            FBRef.memoCategory.push().setValue(Memo(title, content, link, uid, time))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(isEditMode: Boolean, key: String) =
            WriteMemoFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEditMode", isEditMode)
                    putString("key", key)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}