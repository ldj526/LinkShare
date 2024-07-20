package com.example.linkshare.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.R
import com.example.linkshare.board.BoardRepository
import com.example.linkshare.board.BoardViewModel
import com.example.linkshare.board.BoardViewModelFactory
import com.example.linkshare.databinding.FragmentMainViewBinding
import com.example.linkshare.main.MainViewModel
import com.example.linkshare.main.MainViewModelFactory
import com.example.linkshare.main.MainViewRepository
import com.example.linkshare.main.TopLinksAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MainViewFragment : Fragment() {

    private var _binding: FragmentMainViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var boardViewModel: BoardViewModel
    private lateinit var topViewLinksAdapter: TopLinksAdapter
    private lateinit var topShareLinksAdapter: TopLinksAdapter
    private var currentViewTimeRange = MainViewRepository.TimeRange.MONTHLY
    private var currentShareTimeRange = MainViewRepository.TimeRange.MONTHLY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainViewRepository = MainViewRepository()
        val mainFactory = MainViewModelFactory(mainViewRepository)
        mainViewModel = ViewModelProvider(this, mainFactory)[MainViewModel::class.java]

        val boardRepository = BoardRepository()
        val boardFactory = BoardViewModelFactory(boardRepository)
        boardViewModel = ViewModelProvider(this, boardFactory)[BoardViewModel::class.java]

        topViewLinksAdapter = TopLinksAdapter(mutableListOf(), boardViewModel)
        topShareLinksAdapter = TopLinksAdapter(mutableListOf(), boardViewModel)

        binding.rvTopViews.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTopShares.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.rvTopViews.adapter = topViewLinksAdapter
        binding.rvTopShares.adapter = topShareLinksAdapter

        setupChips()
        observeViewModel()

        // 초기 선택 상태 설정 및 데이터 로드
        binding.chipGroupViewTimeRange.check(R.id.chip_view_monthly)
        binding.chipGroupShareTimeRange.check(R.id.chip_share_monthly)
        loadTopViewLinks(currentViewTimeRange)
        loadTopShareLinks(currentShareTimeRange)
    }

    private fun setupChips() {
        binding.chipViewMonthly.setOnClickListener { onChipClick(R.id.chip_view_monthly, isView = true) }
        binding.chipViewWeekly.setOnClickListener { onChipClick(R.id.chip_view_weekly, isView = true) }
        binding.chipViewDaily.setOnClickListener { onChipClick(R.id.chip_view_daily, isView = true) }

        binding.chipShareMonthly.setOnClickListener { onChipClick(R.id.chip_share_monthly, isView = false) }
        binding.chipShareWeekly.setOnClickListener { onChipClick(R.id.chip_share_weekly, isView = false) }
        binding.chipShareDaily.setOnClickListener { onChipClick(R.id.chip_share_daily, isView = false) }

        binding.chipGroupViewTimeRange.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentViewTimeRange = getTimeRangeFromCheckedId(checkedIds[0])
                updateChipStyles(group, checkedIds[0])
                loadTopViewLinks(currentViewTimeRange)
            }
        }

        binding.chipGroupShareTimeRange.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentShareTimeRange = getTimeRangeFromCheckedId(checkedIds[0])
                updateChipStyles(group, checkedIds[0])
                loadTopShareLinks(currentShareTimeRange)
            }
        }
    }

    private fun onChipClick(chipId: Int, isView: Boolean) {
        if (isView) {
            binding.chipGroupViewTimeRange.check(chipId)
        } else {
            binding.chipGroupShareTimeRange.check(chipId)
        }
    }

    private fun getTimeRangeFromCheckedId(checkedId: Int): MainViewRepository.TimeRange {
        return when (checkedId) {
            R.id.chip_view_monthly, R.id.chip_share_monthly -> MainViewRepository.TimeRange.MONTHLY
            R.id.chip_view_weekly, R.id.chip_share_weekly -> MainViewRepository.TimeRange.WEEKLY
            R.id.chip_view_daily, R.id.chip_share_daily -> MainViewRepository.TimeRange.DAILY
            else -> MainViewRepository.TimeRange.DAILY
        }
    }

    private fun updateChipStyles(group: ChipGroup, selectedChipId: Int) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            if (chip.id == selectedChipId) {
                chip.setChipBackgroundColorResource(R.color.chip_selected_background)
                chip.setTextColor(resources.getColor(R.color.chip_selected_text, null))
            } else {
                chip.setChipBackgroundColorResource(R.color.chip_unselected_background)
                chip.setTextColor(resources.getColor(R.color.chip_unselected_text, null))
            }
        }
    }

    private fun loadTopViewLinks(timeRange: MainViewRepository.TimeRange) {
        mainViewModel.getTopViewLinks(timeRange)
    }

    private fun loadTopShareLinks(timeRange: MainViewRepository.TimeRange) {
        mainViewModel.getTopShareLinks(timeRange)
    }

    private fun observeViewModel() {
        mainViewModel.topViewLinks.observe(viewLifecycleOwner) { result ->
            result.onSuccess { links ->
                topViewLinksAdapter.updateLinks(links)
            }.onFailure {
                Toast.makeText(context, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }

        mainViewModel.topShareLinks.observe(viewLifecycleOwner) { result ->
            result.onSuccess { links ->
                topShareLinksAdapter.updateLinks(links)
            }.onFailure {
                Toast.makeText(context, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }

        mainViewModel.viewLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.topViewProgressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        mainViewModel.shareLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.topShareProgressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}