package com.games.forestbug.vkhgm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MenuActivity : AppCompatActivity() {
    
    private lateinit var btnLevel1: Button
    private lateinit var btnLevel2: Button
    private lateinit var btnLevel3: Button
    private lateinit var btnLevel4: Button
    private lateinit var btnLevel5: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableImmersiveMode()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        setContentView(R.layout.activity_menu)
        
        btnLevel1 = findViewById(R.id.btnLevel1)
        btnLevel2 = findViewById(R.id.btnLevel2)
        btnLevel3 = findViewById(R.id.btnLevel3)
        btnLevel4 = findViewById(R.id.btnLevel4)
        btnLevel5 = findViewById(R.id.btnLevel5)
        
        loadProgress()
        
        btnLevel1.setOnClickListener { startGame(1) }
        btnLevel2.setOnClickListener { startGame(2) }
        btnLevel3.setOnClickListener { startGame(3) }
        btnLevel4.setOnClickListener { startGame(4) }
        btnLevel5.setOnClickListener { startGame(5) }
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
        loadProgress()
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
    
    private fun loadProgress() {
        val prefs = getSharedPreferences("GameProgress", MODE_PRIVATE)
        val maxUnlockedLevel = prefs.getInt("maxUnlockedLevel", 1)
        
        btnLevel1.isEnabled = true
        btnLevel2.isEnabled = maxUnlockedLevel >= 2
        btnLevel3.isEnabled = maxUnlockedLevel >= 3
        btnLevel4.isEnabled = maxUnlockedLevel >= 4
        btnLevel5.isEnabled = maxUnlockedLevel >= 5
    }
    
    private fun startGame(level: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("START_LEVEL", level)
        startActivity(intent)
    }
}

