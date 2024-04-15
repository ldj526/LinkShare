package com.example.linkshare.category

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CategoryAdapter(fragment: Fragment, private val categories: Array<String>) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return CategoryFragment.newInstance(categories[position])
    }
}