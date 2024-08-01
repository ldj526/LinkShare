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
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.linkshare.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupAddressBar()
        setupScrollListener()

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

    override fun onBackPressed() {
        // Handle the back button event
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}