package com.example.linkshare.link

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.linkshare.category.CategorySelectActivity
import com.example.linkshare.databinding.FragmentWriteLinkBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FireBaseCollection
import com.example.linkshare.view.MainActivity
import com.example.linkshare.view.MapViewActivity
import com.google.android.material.chip.Chip
import java.io.ByteArrayOutputStream

class WriteLinkFragment : Fragment() {

    private var _binding: FragmentWriteLinkBinding? = null
    private val binding get() = _binding!!
    private var isEditMode: Boolean = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var firebaseRef = ""
    private var shareCnt: Int = 0
    private var selectedCategories: List<String>? = null
    private lateinit var key: String
    private var writeUid: String = FBAuth.getUid()
    private var time: Long = 0L
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private val linkDataResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 결과를 받아 TextView에 설정
            binding.tvMap.text = result.data?.getStringExtra("title")
            // 결과를 받아 변수에 설정
            latitude = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            longitude = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
        }
    }
    private val selectedCategoryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedCategories = result.data?.getStringArrayListExtra("selectedCategories")
            updateCategoriesView(selectedCategories)
        }
    }
    private lateinit var linkViewModel: LinkViewModel
    private val prefix = "https://"

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            isEditMode = it.getBoolean("isEditMode", false)
            key = it.getString("key", "")
        }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    binding.ivImage.setImageURI(uri)
                }
            }

        val linkRepository = LinkRepository()
        val linkFactory = LinkViewModelFactory(linkRepository)
        linkViewModel = ViewModelProvider(this, linkFactory)[LinkViewModel::class.java]

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteLinkBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isEditMode) {
            binding.btnSave.text = "수정"
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnSave.text = "저장"
        }

        updateCategoriesView(selectedCategories)
        setupEditText()

        if (key.isNotEmpty()) {
            linkViewModel.getPostData(writeUid, key)
            linkViewModel.linkData.observe(viewLifecycleOwner) { result ->
                result.onSuccess { link ->
                    loadLink(link)
                }.onFailure {
                    Toast.makeText(requireContext(), "데이터 로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
            linkViewModel.getImageUrl(key)
            linkViewModel.imageUrl.observe(viewLifecycleOwner) { url ->
                url?.let {
                    Glide.with(this).load(it).into(binding.ivImage)
                }
            }
        }

        linkViewModel.saveStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(requireContext(), "메모 저장 성공", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "메모 저장 실패", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(requireContext(), "메모 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSave.setOnClickListener {
            val (link, data: ByteArray?) = saveLink()
            linkViewModel.saveLink(link, data, isEditMode)
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
            linkDataResultLauncher.launch(intent)
        }

        binding.tvCategory.setOnClickListener {
            val intent = Intent(activity, CategorySelectActivity::class.java).apply {
                selectedCategories?.let {
                    putStringArrayListExtra("currentSelectedCategories", ArrayList(it))
                }
            }
            selectedCategoryResultLauncher.launch(intent)
        }
    }

    // Link 입력 시 'https://' 자동으로 입력
    private fun setupEditText() {
        binding.etLink.apply {
            setText(prefix)
            setSelection(prefix.length)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.toString().startsWith(prefix)) {
                        setText(prefix)
                        setSelection(prefix.length)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    if (!text.toString().startsWith(prefix)) {
                        setText(prefix)
                    }
                    setSelection(prefix.length)
                }
            }
        }
    }

    private fun updateCategoriesView(categories: List<String>?) {
        binding.categoryChipGroup.removeAllViews() // 기존에 추가된 뷰들 제거
        categories?.forEach { category ->
            val chip = Chip(requireActivity()).apply {
                text = category
                isCheckable = false
                isClickable = false
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun saveLink(): Pair<Link, ByteArray?> {
        val link = Link(
            key = key, category = selectedCategories, title = binding.etTitle.text.toString(),
            content = binding.etContent.text.toString(),
            link = binding.etLink.text.toString(),
            location = binding.tvMap.text.toString(), latitude = latitude, longitude = longitude,
            uid = writeUid,
            time = if (isEditMode) time else FBAuth.getTimestamp(), firebaseRef = "link",
            shareCount = if (isEditMode) shareCnt else 0
        )

        val imageView = binding.ivImage.drawable

        val data: ByteArray? = if (imageView is BitmapDrawable) {
            val bitmap = imageView.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            baos.toByteArray()
        } else {
            null // 이미지가 없을 경우 null로 처리
        }
        return Pair(link, data)
    }

    private fun loadLink(link: Link?) {
        link?.let {
            selectedCategories = it.category
            it.category?.forEach { category ->
                val chip = Chip(requireActivity()).apply {
                    text = category
                    isCheckable = false
                    isClickable = false
                }
                binding.categoryChipGroup.addView(chip)
            }
            binding.etTitle.setText(it.title)
            binding.etLink.setText(it.link)
            binding.etContent.setText(it.content)
            binding.tvMap.text = it.location
            latitude = it.latitude ?: 0.0
            longitude = it.longitude ?: 0.0
            writeUid = it.uid
            time = it.time
            firebaseRef = it.firebaseRef
            shareCnt = it.shareCount
        }
    }

    // 다이얼로그 생성
    private fun showDeleteDialog() {
        val dialog = CustomDialog("삭제 하시겠습니까?", onYesClicked = {
            if (firebaseRef == "link") linkViewModel.deleteMemo(writeUid, key)
            else if (firebaseRef == "sharedLink") linkViewModel.deleteMemo(writeUid, key)
            requireActivity().finish()
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "DeleteDialog")
    }

    companion object {
        @JvmStatic
        fun newInstance(isEditMode: Boolean, key: String) =
            WriteLinkFragment().apply {
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