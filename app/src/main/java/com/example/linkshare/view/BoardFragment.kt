package com.example.linkshare.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.linkshare.R
import com.example.linkshare.category.CategoryAdapter
import com.example.linkshare.category.SeeMoreActivity
import com.example.linkshare.databinding.FragmentBoardBinding
import com.google.android.material.chip.Chip

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupChips()
    }

    // viewPager setting
    private fun setupViewPager() {
        val categories = resources.getStringArray(R.array.category)
        val adapter = CategoryAdapter(this, categories)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateChipSelection(position)
            }
        })

        binding.tvSeeMore.setOnClickListener {
            seeMoreList()
        }
    }

    // chipGroup setting
    private fun setupChips() {
        val categories = resources.getStringArray(R.array.category)

        categories.forEachIndexed { idx, category ->
            val chip = Chip(context).apply {
                text = category
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                isClickable = true
                isCheckable = true
                checkedIcon = null
                id = View.generateViewId()
            }
            binding.chipGroup.addView(chip)
            chip.setOnClickListener {
                binding.viewPager.currentItem = idx
            }
        }
    }

    // chip 선택한 것과 선택하지 않은 것 구분
    private fun updateChipSelection(selectedIndex: Int) {
        for (i in 0 until binding.chipGroup.childCount) {
            val chip = binding.chipGroup.getChildAt(i) as Chip
            chip.isChecked = i == selectedIndex
            if (chip.isChecked) {
                chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.selected_chip_background_color))
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                scrollToChip(chip)
            } else {
                chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.unselected_chip_background_color))
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    // chip에 맞게 scrollview 조작
    private fun scrollToChip(chip: Chip?) {
        chip?.let {
            val scrollBounds = Rect()
            binding.horizontalScrollview.getDrawingRect(scrollBounds)
            val chipBounds = Rect()
            chip.getDrawingRect(chipBounds)
            binding.horizontalScrollview.offsetDescendantRectToMyCoords(chip, chipBounds)
            if (!scrollBounds.contains(chipBounds)) {
                binding.horizontalScrollview.smoothScrollTo(chipBounds.left - (scrollBounds.width() - chipBounds.width()) / 2, 0)
            }
        }
    }

    private fun seeMoreList() {
        val currentPageIndex = binding.viewPager.currentItem
        val currentCategory = resources.getStringArray(R.array.category)[currentPageIndex]

        val intent = Intent(context, SeeMoreActivity::class.java).apply {
            putExtra("category", currentCategory)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}