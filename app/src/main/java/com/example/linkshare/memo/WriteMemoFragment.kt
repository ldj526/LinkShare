package com.example.linkshare.memo

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
import com.example.linkshare.databinding.FragmentMemoWriteBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.CustomDialogInterface
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class WriteMemoFragment : Fragment(), CustomDialogInterface {

    private var _binding: FragmentMemoWriteBinding? = null
    private val binding get() = _binding!!
    private var isEditMode: Boolean = false
    private lateinit var key: String
    private lateinit var writeUid: String
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

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
        _binding = FragmentMemoWriteBinding.inflate(inflater, container, false)
        if (isEditMode) {
            binding.btnSave.text = "수정"
            binding.btnDelete.visibility = View.VISIBLE
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

        binding.btnDelete.setOnClickListener {
            showDialog()
        }

        binding.ivImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        return binding.root
    }

    // Firebase에서 데이터 값 가져오기
    private fun getMemoData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {   // 메모가 삭제 됐을 때 정보가 없으면 에러가 나기 때문에 예외처리
                    val dataModel = snapshot.getValue(Memo::class.java)

                    binding.etTitle.setText(dataModel!!.title)
                    binding.etLink.setText(dataModel.link)
                    binding.etContent.setText(dataModel.content)
                    writeUid = dataModel.uid
                } catch (e: Exception) {

                }
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
        if (isEditMode) {   // 수정할 때
            // key값에 맞는 Firebase database 수정
            FBRef.memoCategory.child(key).setValue(Memo(title, content, link, writeUid, time))
            imageUpload(key)
        } else {
            // 새 메모를 만들 때
            val uid = FBAuth.getUid()
            val memoKey = FBRef.memoCategory.push().key.toString()
            Log.d("WriteMemoFragment", "memoKey: $memoKey")
            // Firebase database에 추가
            FBRef.memoCategory.child(memoKey).setValue(Memo(title, content, link, uid, time))
            imageUpload(memoKey)
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

    // 다이얼로그 생성
    private fun showDialog() {
        val dialog = CustomDialog(this, "삭제 하시겠습니까?")
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "DeleteDialog")
    }

    override fun onClickYesButton() {
        FBRef.memoCategory.child(key).removeValue()
        requireActivity().finish()
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