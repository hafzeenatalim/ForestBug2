package com.games.forestbug.vkhgm

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView
    private lateinit var btnPauseSmall: ImageView
    private lateinit var btnPauseIcon: ImageView
    private lateinit var pauseLayout: android.view.ViewGroup
    private lateinit var btnRestart: Button
    private lateinit var btnMenu: Button
    private lateinit var btnMenuPause: Button
    private lateinit var gameOverButtons: android.view.ViewGroup
    private var isPaused = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableImmersiveMode()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        setContentView(R.layout.activity_main)
        
        gameView = findViewById(R.id.gameView)
        btnPauseSmall = findViewById(R.id.btnPauseSmall)
        btnPauseIcon = findViewById(R.id.btnPauseIcon)
        pauseLayout = findViewById(R.id.pauseLayout)
        btnRestart = findViewById(R.id.btnRestart)
        btnMenu = findViewById(R.id.btnMenu)
        btnMenuPause = findViewById(R.id.btnMenuPause)
        gameOverButtons = findViewById(R.id.gameOverButtons)
        
        val startLevel = intent.getIntExtra("START_LEVEL", 1)
        gameView.setStartLevel(startLevel)
        
        gameView.post {
            gameView.startGame()
        }
        
        btnPauseSmall.setOnClickListener {
            gameView.pauseGame()
            btnPauseSmall.visibility = View.GONE
            pauseLayout.visibility = View.VISIBLE
            isPaused = true
        }
        
        btnPauseIcon.setOnClickListener {
            gameView.resumeGame()
            pauseLayout.visibility = View.GONE
            btnPauseSmall.visibility = View.VISIBLE
            isPaused = false
        }
        
        btnMenuPause.setOnClickListener {
            finish()
        }
        
        btnRestart.setOnClickListener {
            gameView.startGame()
            gameOverButtons.visibility = View.GONE
            btnPauseSmall.visibility = View.VISIBLE
            pauseLayout.visibility = View.GONE
            isPaused = false
        }
        
        btnMenu.setOnClickListener {
            finish()
        }
        
        gameView.onScoreChanged = { score ->
        }
        
        gameView.onLevelChanged = { level ->
        }
        
        gameView.onNewLevelUnlocked = { level ->
            runOnUiThread {
                Toast.makeText(this, "New level $level unlocked!", Toast.LENGTH_LONG).show()
            }
        }
        
        gameView.onGameOver = {
            runOnUiThread {
                btnPauseSmall.visibility = View.GONE
                pauseLayout.visibility = View.GONE
                gameOverButtons.visibility = View.VISIBLE
                isPaused = false
                vibrateOnGameOver()
            }
        }
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isPaused && gameView.isGameRunning() && gameOverButtons.visibility != View.VISIBLE) {
                    gameView.pauseGame()
                    btnPauseSmall.visibility = View.GONE
                    pauseLayout.visibility = View.VISIBLE
                    isPaused = true
                }
            }
        })
    }
    
    override fun onPause() {
        super.onPause()
        if (!isPaused) {
            gameView.pauseGame()
            btnPauseSmall.visibility = View.GONE
            pauseLayout.visibility = View.VISIBLE
            isPaused = true
        }
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
    
    private fun vibrateOnGameOver() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}
