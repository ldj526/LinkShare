package com.example.linkshare.memo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.linkshare.databinding.FragmentWriteMemoBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.example.linkshare.view.MapViewActivity
import java.io.ByteArrayOutputStream

class WriteMemoFragment : Fragment() {

    private var _binding: FragmentWriteMemoBinding? = null
    private val binding get() = _binding!!
    private var isEditMode: Boolean = false
    private lateinit var key: String
    private lateinit var writeUid: String
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var category = ""
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 결과를 받아 TextView에 설정
                binding.tvMap.text = result.data?.getStringExtra("title")
                // 결과를 받아 변수에 설정
                latitude = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                longitude = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            }
        }
    private val memoViewModel by lazy { ViewModelProvider(this)[MemoViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            isEditMode = it.getBoolean("isEditMode", false)
            key = it.getString("key", "")
            Log.d("WriteMemoFragment", "key: $key")
        }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    binding.ivImage.setImageURI(uri)
                }
            }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteMemoBinding.inflate(inflater, container, false)
        if (isEditMode) {
            binding.btnSave.text = "수정"
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnSave.text = "저장"
        }

        // 저장된 메모를 불러올 경우
        if (key != "") {
            memoViewModel.getPostData(key)
            memoViewModel.memoData.observe(viewLifecycleOwner) { memo ->
                memo?.let {
                    binding.etTitle.setText(it.title)
                    binding.etLink.setText(it.link)
                    binding.etContent.setText(it.content)
                    binding.tvMap.text = it.location
                    latitude = it.latitude
                    longitude = it.longitude
                    writeUid = it.uid
                    category = it.category
                }
            }
            memoViewModel.getImageUrl(key)
            memoViewModel.imageUrl.observe(viewLifecycleOwner) { url ->
                url?.let {
                    Glide.with(this).load(it).into(binding.ivImage)
                }
            }
        }

        memoViewModel.saveStatus.observe(viewLifecycleOwner) {success ->
            if (success){
                Toast.makeText(requireContext(), "메모 저장 성공", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "메모 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSave.setOnClickListener {
            val memo = Memo(key, binding.etTitle.text.toString(),
                binding.etContent.text.toString(),
                binding.etLink.text.toString(),
                binding.tvMap.text.toString(), latitude, longitude,
                if (isEditMode) writeUid else FBAuth.getUid(), FBAuth.getTime(), "memo")

            val imageView = binding.ivImage.drawable

            val data: ByteArray? = if (imageView is BitmapDrawable) {
                val bitmap = imageView.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.toByteArray()
            } else {
                null // 이미지가 없을 경우 null로 처리
            }

            memoViewModel.saveMemo(memo, data, isEditMode)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }

        binding.ivImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.tvMap.setOnClickListener {
            val intent = Intent(activity, MapViewActivity::class.java).apply {
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("title", binding.tvMap.text)
            }
            startForResult.launch(intent)
        }

        return binding.root
    }

    // 다이얼로그 생성
    private fun showDeleteDialog() {
        val dialog = CustomDialog("삭제 하시겠습니까?", onYesClicked = {
            FBRef.memoCategory.child(key).removeValue()
            requireActivity().finish()
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "DeleteDialog")
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