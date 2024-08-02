package com.example.linkshare.view

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.SafeBrowsingResponse
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.linkshare.databinding.ActivityWebViewBinding
import com.example.linkshare.history.HistoryAdapter
import com.example.linkshare.history.HistoryDao
import com.example.linkshare.history.HistoryDatabase
import com.example.linkshare.history.HistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyDao: HistoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupAddressBar()
        setupRecyclerView()
        setupScrollListener()

        val database = HistoryDatabase.getDatabase(applicationContext)
        historyDao = database.historyDao()

        observeHistory()

        val url = intent.getStringExtra("url")
        if (url != null) {
            binding.webView.loadUrl(url)
        }

        binding.btnCloseWebView.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            exitEditMode()
        }

        binding.btnClearAll.setOnClickListener {
            clearAllHistory()
        }
    }

    // WebView에 대한 설정
    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true

        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(binding.webView.settings, true)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.tvAddress.text = url

                if (url != null) {
                    addHistoryItem(url)
                }
            }

            override fun onSafeBrowsingHit(
                view: WebView?, request: WebResourceRequest?,
                threatType: Int, callback: SafeBrowsingResponse?
            ) {
                // 악성 페이지 감지 시 다이얼로그 표시
                AlertDialog.Builder(this@WebViewActivity)
                    .setTitle("안전하지 않은 웹페이지")
                    .setMessage("계속 진행하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->
                        callback?.proceed(true)
                    }
                    .setNegativeButton("아니오") { _, _ ->
                        callback?.backToSafety(true)
                    }
                    .show()
            }
        }
    }

    // 주소 바 설정
    private fun setupAddressBar() {
        binding.tvAddress.setOnClickListener {
            enterEditMode()
        }

        binding.etEditUrl.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val url = binding.etEditUrl.text.toString().trim()
                binding.webView.loadUrl(url)
                exitEditMode()
                true
            } else {
                false
            }
        }
    }

    // URL 수정모드로 들어가기
    private fun enterEditMode() {
        // 숨기고 표시할 요소들 설정
        binding.appBarLayout.visibility = View.GONE
        binding.layoutMain.visibility = View.GONE
        binding.layoutEdit.visibility = View.VISIBLE

        // EditText에 현재 URL 설정 및 포커스 설정
        binding.etEditUrl.setText(binding.tvAddress.text)
        binding.etEditUrl.requestFocus()

        // 키보드를 자동으로 보여줌
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etEditUrl, InputMethodManager.SHOW_IMPLICIT)
    }

    // URL 수정모드에서 나가기
    private fun exitEditMode() {
        // 숨기고 표시할 요소들 설정
        binding.layoutEdit.visibility = View.GONE
        binding.layoutMain.visibility = View.VISIBLE
        binding.appBarLayout.visibility = View.VISIBLE

        // 키보드를 숨김
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etEditUrl.windowToken, 0)
    }

    // Scroll에 따른 View 설정
    private fun setupScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                binding.appBarLayout.setExpanded(false, true)
            } else {
                binding.appBarLayout.setExpanded(true, true)
            }
        })
    }

    // RecyclerView setup
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(mutableListOf()) { historyItem ->
            deleteHistoryItem(historyItem)
        }

        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@WebViewActivity)
        }
    }

    // 인터넷 방문 기록 추가
    private fun addHistoryItem(url: String) {
        // 페이지 제목 가져오기
        val title = binding.webView.title ?: url

        lifecycleScope.launch(Dispatchers.IO) {
            // 먼저 해당 URL이 이미 존재하는지 확인
            val existingItem = historyDao.getHistoryItemByUrl(url)
            if (existingItem != null) {
                // 기존에 존재하는 경우, 해당 항목을 삭제
                historyDao.delete(existingItem)
            }

            // 새로운 HistoryItem 생성 및 삽입
            val historyItem = HistoryItem(title = title, url = url)
            historyDao.insert(historyItem)

            // 메인 스레드에서 UI 업데이트
            withContext(Dispatchers.Main) {
                observeHistory()
            }
        }
    }

    // 인터넷 방문 기록 선택하여 삭제
    private fun deleteHistoryItem(historyItem: HistoryItem) {
        val position = historyAdapter.historyItems.indexOf(historyItem) // 삭제할 항목의 위치를 찾습니다.
        if (position != -1) {
            lifecycleScope.launch(Dispatchers.IO) {
                historyDao.deleteByUrl(historyItem.url) // 데이터베이스에서 항목 삭제

                // UI 업데이트는 메인 스레드에서 이루어져야 하므로 UI 스레드에서 실행합니다.
                withContext(Dispatchers.Main) {
                    historyAdapter.removeItem(position) // 어댑터에서 항목 삭제
                }
            }
        }
    }

    // 인터넷 방문 기록 모두 삭제
    private fun clearAllHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            historyDao.deleteAll()

            // 모든 항목을 어댑터에서 제거함
            withContext(Dispatchers.Main) {
                historyAdapter.updateHistoryItems(emptyList())
            }
        }
    }

    private fun observeHistory() {
        historyDao.getAllHistoryItems().observe(this, Observer { historyItems ->
            historyAdapter.updateHistoryItems(historyItems)
        })
    }

    override fun onBackPressed() {
        // Handle the back button event
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}