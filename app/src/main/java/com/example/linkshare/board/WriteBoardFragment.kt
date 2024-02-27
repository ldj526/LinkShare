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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.linkshare.board.Board
import com.example.linkshare.databinding.FragmentWriteBoardBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.CustomDialogInterface
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.example.linkshare.view.MapViewActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class WriteBoardFragment : Fragment(), CustomDialogInterface {

    private var _binding: FragmentWriteBoardBinding? = null
    private val binding get() = _binding!!
    private var isEditMode: Boolean = false
    private lateinit var key: String
    private lateinit var writeUid: String
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
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
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteBoardBinding.inflate(inflater, container, false)
        if (isEditMode) {
            binding.btnSave.text = "수정"
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnSave.text = "저장"
        }

        // 저장된 메모를 불러올 경우
        if (key != "") {
            getBoardData(key)
            getImageData(key)
        }

        binding.btnSave.setOnClickListener {
            saveBoard()
            // 해당 Activity 종료
            requireActivity().finish()
        }

        binding.btnDelete.setOnClickListener {
            showDialog()
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

    // Firebase에서 데이터 값 가져오기
    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {   // 메모가 삭제 됐을 때 정보가 없으면 에러가 나기 때문에 예외처리
                    val dataModel = snapshot.getValue(Board::class.java)

                    binding.etTitle.setText(dataModel!!.title)
                    binding.etLink.setText(dataModel.link)
                    binding.etContent.setText(dataModel.content)
                    binding.tvMap.text = dataModel.location
                    latitude = dataModel.latitude
                    longitude = dataModel.longitude
                    writeUid = dataModel.uid
                } catch (e: Exception) {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("BoardFragment", "실패")
            }

        }
        FBRef.boardCategory.child(key).addValueEventListener(postListener)
    }

    // 새 게시글 / 수정 에 따른 게시글 저장
    private fun saveBoard() {
        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()
        val link = binding.etLink.text.toString()
        val location = binding.tvMap.text.toString()
        val time = FBAuth.getTime()
        if (isEditMode) {   // 수정할 때
            // key값에 맞는 Firebase database 수정
            FBRef.boardCategory.child(key).setValue(Memo(title, content, link, location, latitude, longitude, writeUid, time))
            imageUpload(key)
        } else {
            // 새 메모를 만들 때
            val uid = FBAuth.getUid()
            val boardKey = FBRef.boardCategory.push().key.toString()
            Log.d("WriteMemoFragment", "memoKey: $boardKey")
            // Firebase database에 추가
            FBRef.boardCategory.child(boardKey).setValue(Memo(title, content, link, location, latitude, longitude, uid, time))
            imageUpload(boardKey)
        }
    }

    // Firebase에 Image Upload
    private fun imageUpload(key: String) {
        // Get the data from an ImageView as bytes

        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference
        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("$key.png")

        val imageView = binding.ivImage
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    // Firebase에서 Image 가져오기
    private fun getImageData(key: String) {
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child("${key}.png")

        // ImageView in your Activity
        val imageViewFromFB = binding.ivImage

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful && isAdded) {
                Glide.with(requireContext())
                    .load(task.result)
                    .into(imageViewFromFB)
            } else {

            }
        })
    }

    // 다이얼로그 생성
    private fun showDialog() {
        val dialog = CustomDialog(this, "삭제 하시겠습니까?")
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "DeleteDialog")
    }

    override fun onClickYesButton() {
        FBRef.boardCategory.child(key).removeValue()
        requireActivity().finish()
    }

    companion object {
        @JvmStatic
        fun newInstance(isEditMode: Boolean, key: String) =
            WriteBoardFragment().apply {
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