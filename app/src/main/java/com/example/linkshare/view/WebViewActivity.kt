package com.example.linkshare.view

import android.os.Bundle
import android.util.Log
import android.webkit.SafeBrowsingResponse
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.linkshare.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.settings.javaScriptEnabled = true

        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(binding.webView.settings, true)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
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

        val url = intent.getStringExtra("url")
        if (url != null) {
            binding.webView.loadUrl(url)
        }

        binding.btnCloseWebView.setOnClickListener {
            finish()
        }
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