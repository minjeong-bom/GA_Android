package com.example.testwebview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {
    // 파일 업로드를 위한 요청 코드
    private val FILECHOOSER_RESULTCODE = 1

    // 파일을 선택할 때 사용할 콜백을 저장
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webview)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                return !url.startsWith("http://169.254.49.198:9000")
            }
        }

        // 자바스크립트와 DOM 스토리지 활성화
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // WebChromeClient 설정
        webView.webChromeClient = object : WebChromeClient() {
            // 파일 선택을 처리하는 콜백 메서드
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                mUploadMessage?.onReceiveValue(null)
                mUploadMessage = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*" // 이미지만 선택하도록 설정
                }
                startActivityForResult(intent, FILECHOOSER_RESULTCODE)
                return true
            }
        }

        // 웹뷰에 URL 로드
        webView.loadUrl("http://169.254.49.198:9000/#/")
    }

    // 결과를 처리하는 메서드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage || data == null || resultCode != RESULT_OK) {
                mUploadMessage?.onReceiveValue(null)
                mUploadMessage = null
                return
            }
            val result = if (data.dataString != null) arrayOf(Uri.parse(data.dataString)) else arrayOf()
            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
