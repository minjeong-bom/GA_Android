package com.example.testwebview

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.view.View

class MainActivity : AppCompatActivity() {
    // 파일 업로드를 위한 요청 코드
    private val FILECHOOSER_RESULTCODE = 1

    // 파일을 선택할 때 사용할 콜백을 저장
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 상태바 아이콘 색상을 어둡게 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webview)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                return !url.startsWith("http://192.168.35.7:9000/")
            }
        }

        if (!isNetworkAvailable()) {
            AlertDialog.Builder(this)
                .setTitle("인터넷 연결 오류")
                .setMessage("인터넷 연결을 해주세요")
                .setPositiveButton("앱 종료") { dialog, which ->
                    finish() // 액티비티 종료
                }
                .setCancelable(false)
                .show()
        } else {
            // 인터넷 연결이 되어 있을 때의 로직
            val webView: WebView = findViewById(R.id.webview)
            // WebView 설정 및 기타 코드...
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
        webView.loadUrl("http://192.168.35.7:9000/")
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}
