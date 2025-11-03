package com.games.forestbug.vkhgm

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ForestActivity : AppCompatActivity() {
    
    private lateinit var forestView: WebView
    private lateinit var loadingLayout: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableImmersiveMode()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        setContentView(R.layout.activity_forest)
        
        forestView = findViewById(R.id.forestView)
        loadingLayout = findViewById(R.id.loadingLayout)
        
        forestView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
        }
        
        forestView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, link: String?) {
                super.onPageFinished(view, link)
                loadingLayout.visibility = View.GONE
            }
        }
        
        val link = intent.getStringExtra("LINK") ?: ""
        if (link.isNotEmpty()) {
            forestView.loadUrl(link)
        }
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (forestView.canGoBack()) {
                    forestView.goBack()
                }
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }
    
    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

