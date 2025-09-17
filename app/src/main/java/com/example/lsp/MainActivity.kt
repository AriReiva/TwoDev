package com.example.lsp

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.lsp.ui.theme.LSPTheme

class MainActivity : ComponentActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

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
                    url = "https://asessment24.site/twodev-fe/auth/login",
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
                            finish()
                        }
                    }
                }
            }
        }
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

                    // 🔹 WebViewClient intercept PDF
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Pull-to-refresh selesai
                            isRefreshing = false
                            swipeRefreshLayout.isRefreshing = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url.toString()

                            // Jika PDF, pakai intent Android
                            if (url.endsWith(".pdf")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(Uri.parse(url), "application/pdf")
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    fallback.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(fallback)
                                }
                                return true
                            }

                            // Kalau bukan PDF, biarkan WebView yang load
                            return false
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
