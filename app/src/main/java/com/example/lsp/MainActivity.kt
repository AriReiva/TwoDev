package com.example.lsp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.lsp.ui.theme.LSPTheme

class MainActivity : ComponentActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var webView: WebView? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (filePathCallback == null) return@registerForActivityResult
        val resultUris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(resultUris)
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LSPTheme {

                val webViewState = remember { mutableStateOf<WebView?>(null) }
                WebViewScreen(
                    url = "http://10.197.1.105:5173/auth/login",
                    modifier = Modifier.fillMaxSize(),
                    webViewState = webViewState,
                    onFileChooser = { callback, intent ->
                        filePathCallback = callback
                        fileChooserLauncher.launch(intent)
                    }
                )
                BackHandler(enabled = true) {
                    webViewState.value?.let { webView ->
                        if (webView.canGoBack()) {
                            webView.goBack()
                        } else {
                            finish() // keluar activity kalau tidak bisa back lagi
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        webView?.let {
            if (it.canGoBack()) {
                it.goBack()
            } else {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    modifier: Modifier = Modifier,
    webViewState: MutableState<WebView?>,
    onFileChooser: (ValueCallback<Array<Uri>>, Intent) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val swipeRefreshLayout = SwipeRefreshLayout(context)

                val webView = WebView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isRefreshing = false
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>,
                            fileChooserParams: FileChooserParams
                        ): Boolean {
                            onFileChooser(filePathCallback, fileChooserParams.createIntent())
                            return true
                        }
                    }

                    setBackgroundColor(android.graphics.Color.WHITE)
                    loadUrl(url)
                }

                webViewState.value = webView

                swipeRefreshLayout.addView(webView)
                swipeRefreshLayout.setOnRefreshListener {
                    isRefreshing = true
                    webView.reload()
                }

                swipeRefreshLayout
            }
        )
    }
}