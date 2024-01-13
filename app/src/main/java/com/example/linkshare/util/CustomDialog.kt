package com.example.linkshare.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.linkshare.databinding.FragmentCustomDialogBinding

class CustomDialog(customDialogInterface: CustomDialogInterface, text: String) : DialogFragment() {

    private var _binding: FragmentCustomDialogBinding? = null
    private val binding get() = _binding!!
    private var text: String? = null
    private var customDialogInterface: CustomDialogInterface? = null

    init {
        this.text = text
        this.customDialogInterface = customDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        // 배경 투명하게
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.tvDialog.text = text

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {
            this.customDialogInterface?.onClickYesButton()
            dismiss()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

interface CustomDialogInterface {
    fun onClickYesButton()
}